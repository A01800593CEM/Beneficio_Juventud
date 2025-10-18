package mx.itesm.beneficiojuventud.view

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.guava.await
import android.Manifest
import com.google.accompanist.permissions.PermissionStatus


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onClose: () -> Unit,
    onResult: (String) -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Pide el permiso al entrar si está negado
    LaunchedEffect(cameraPermission.status) {
        if (cameraPermission.status is PermissionStatus.Denied) {
            cameraPermission.launchPermissionRequest()
        }
    }

    if (cameraPermission.status is PermissionStatus.Granted) {
        QrScannerContent(onClose = onClose, onResult = onResult)
    } else {
        PermissionRationale(onClose = onClose)
    }
}

@Composable
private fun PermissionRationale(onClose: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Se requiere permiso de cámara para escanear el código.")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onClose) { Text("Cerrar") }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun QrScannerContent(
    onClose: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context).apply {
        // Opcional: el performance suele ser mejor con surfaceView en algunos devices
        // implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }}
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var handled by remember { mutableStateOf(false) }

    val options = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_DATA_MATRIX
            )
            .build()
    }
    val scanner = remember { BarcodeScanning.getClient(options) }

    // Guardamos referencias para cerrarlas en DisposableEffect
    var analysisUseCase by remember { mutableStateOf<ImageAnalysis?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    LaunchedEffect(previewView, scanner) {
        val cameraProvider = cameraProviderFuture.await()
        cameraProviderRef = cameraProvider

        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val selector = CameraSelector.DEFAULT_BACK_CAMERA

        val analysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysisUseCase = it }

        val executor = ContextCompat.getMainExecutor(context)

        analysis.setAnalyzer(executor) { imageProxy ->
            if (handled) {
                imageProxy.close()
                return@setAnalyzer
            }
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val first = barcodes.firstOrNull { it.rawValue != null }
                        if (first != null && !handled) {
                            handled = true
                            val value = first.rawValue.orEmpty()
                            imageProxy.close()
                            onResult(value)
                            return@addOnSuccessListener
                        }
                    }
                    .addOnFailureListener {
                        // log si quieres
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else {
                imageProxy.close()
            }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                analysis
            )
        } catch (_: Exception) {
            // Manejo básico de errores de cámara
        }
    }

    // 🔻 Importante: liberar recursos al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            try {
                analysisUseCase?.clearAnalyzer()
            } catch (_: Exception) {}
            try {
                cameraProviderRef?.unbindAll()
            } catch (_: Exception) {}
            try {
                scanner.close()
            } catch (_: Exception) {}
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )
        TopAppBarOverlay(onClose = onClose)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp)
                .padding(2.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                    shape = MaterialTheme.shapes.medium
                )
        )
    }
}

@Composable
private fun TopAppBarOverlay(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        FilledIconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar")
        }
    }
}
