package mx.itesm.beneficiojuventud.viewcollab

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.view.Screens
import mx.itesm.beneficiojuventud.view.StatusType
import java.util.concurrent.Executors

private const val TAG = "QRScannerScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    nav: NavHostController,
    branchId: Int,
    collaboratorId: String? = null,
    viewModel: QRScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isProcessing by viewModel.isProcessing.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val error by viewModel.error.collectAsState()

    // Request camera permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Handle scan result - Navigate to Status screen
    LaunchedEffect(scanResult) {
        scanResult?.let {
            Log.d(TAG, "Scan successful, navigating to Status screen")
            viewModel.clearResult()
            nav.navigate(
                Screens.Status.createRoute(
                    StatusType.QR_SCAN_SUCCESS,
                    Screens.HomeScreenCollab.route
                )
            ) {
                // Limpia la pila de navegación para que no se pueda volver atrás
                popUpTo(Screens.HomeScreenCollab.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    // Handle errors - Navigate to Status screen
    LaunchedEffect(error) {
        error?.let {
            Log.e(TAG, "Scan error: $it")
            errorMessage = it
            viewModel.clearError()
            nav.navigate(
                Screens.Status.createRoute(
                    StatusType.QR_SCAN_ERROR,
                    Screens.HomeScreenCollab.route
                )
            ) {
                // Limpia la pila de navegación para que no se pueda volver atrás
                popUpTo(Screens.HomeScreenCollab.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        topBar = {
            BJTopHeader(title = "Escanear QR", nav = nav)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onQRCodeScanned = { qrData ->
                        if (!isProcessing) {
                            Log.d(TAG, "QR Code scanned, processing with branchId: $branchId, collaboratorId: $collaboratorId")
                            viewModel.processQRCode(qrData, branchId, collaboratorId)
                        }
                    }
                )

                // Overlay with scanning frame
                ScanningOverlay(
                    modifier = Modifier.fillMaxSize(),
                    isProcessing = isProcessing
                )
            } else {
                // Permission denied message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Permiso de cámara requerido",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Para escanear códigos QR, necesitamos acceso a tu cámara.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(24.dp))
                    MainButton(
                        text = "Conceder Permiso",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQRCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    var lastScannedTime by remember { mutableStateOf(0L) }
    val scanCooldown = 2000L // 2 seconds cooldown between scans

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastScannedTime < scanCooldown) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    @ExperimentalGetImage
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                        barcode.rawValue?.let { qrData ->
                                            lastScannedTime = currentTime
                                            onQRCodeScanned(qrData)
                                            Log.d(TAG, "QR Code scanned: $qrData")
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Barcode scanning failed", e)
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }
}

@Composable
fun ScanningOverlay(
    modifier: Modifier = Modifier,
    isProcessing: Boolean
) {
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Scanning frame
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
            ) {
                // Corner markers
                Canvas(modifier = Modifier.fillMaxSize()) { /* draw corners */ }
            }

            Spacer(Modifier.height(32.dp))

            if (isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Procesando...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Escanea el código QR",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Coloca el código dentro del marco",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
