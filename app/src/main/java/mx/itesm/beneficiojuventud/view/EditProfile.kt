package mx.itesm.beneficiojuventud.view

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StoragePath
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var selectedTab by remember { mutableStateOf(BJTab.Profile) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appVersion = "1.0.01"

    var name by rememberSaveable { mutableStateOf("Ivan Serrano de León") }
    var email by rememberSaveable { mutableStateOf("ivandil@beneficio.com") }
    var phone by rememberSaveable { mutableStateOf("+52 55 1234 5678") }
    var birth by rememberSaveable { mutableStateOf("18 / 09 / 2004") }

    var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    // Load user info when screen opens
    LaunchedEffect(Unit) {
        authViewModel.getCurrentUser()
    }

    val currentUserId by authViewModel.currentUserId.collectAsState()
    val actualUserId = currentUserId ?: "anonymous"
    Log.d("EditProfile", "Current User ID: $actualUserId")

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri
            uploadProfileImage(context, uri, actualUserId,
                onSuccess = { url ->
                    profileImageUrl = url
                    scope.launch {
                        snackbarHostState.showSnackbar("Foto de perfil actualizada correctamente")
                    }
                },
                onError = { error ->
                    scope.launch {
                        snackbarHostState.showSnackbar("Error al subir la imagen: $error")
                    }
                },
                onLoading = { loading -> isUploading = loading }
            )
        }
    }

    // Load profile image on startup
    LaunchedEffect(actualUserId) {
        try {
            downloadProfileImage(context, actualUserId,
                onSuccess = { url -> profileImageUrl = url },
                onError = { Log.d("EditProfile", "Storage not configured yet: $it") },
                onLoading = { loading -> isDownloading = loading }
            )
        } catch (e: Exception) {
            Log.d("EditProfile", "Storage not configured yet, skipping image download")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                // Logo centrado arriba
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_beneficio_joven),
                        contentDescription = "Logo",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Fila con back, título y campana
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Editar Perfil",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            fontSize = 20.sp,
                            color = Color(0xFF616161)
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Notificaciones",
                        tint = Color(0xFF008D96)
                    )
                }

                // Divisor con gradiente debajo del encabezado
                GradientDivider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        bottomBar = {
            BJBottomBar(
                selected = selectedTab,
                onSelect = { tab ->
                    selectedTab = tab
                    when (tab) {
                        BJTab.Home      -> nav.navigate(Screens.Home.route)
                        BJTab.Coupons   -> { /* nav.navigate(...) */ }
                        BJTab.Favorites -> { /* nav.navigate(...) */ }
                        BJTab.Profile    -> Unit
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Contenido principal con scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen de perfil
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (!isUploading) {
                                pickImage.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploading || isDownloading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color(0xFF008D96)
                            )
                        }
                        profileImageUrl != null -> {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(CircleShape)
                            )
                        }
                        avatarUri != null -> {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(CircleShape)
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.user_icon),
                                contentDescription = "Avatar",
                                modifier = Modifier.size(90.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Cambiar Foto",
                    color = Color(0xFF008D96),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(20.dp))

                ProfileTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre Completo",
                    leadingIcon = Icons.Outlined.Person
                )
                ProfileTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo Electrónico",
                    leadingIcon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email
                )
                ProfileTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Teléfono",
                    leadingIcon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone
                )
                ProfileTextField(
                    value = birth,
                    onValueChange = { birth = it },
                    label = "Fecha de Nacimiento",
                    leadingIcon = Icons.Outlined.CalendarMonth
                )

                Spacer(Modifier.height(24.dp))

                // Botón principal
                MainButton(
                    text = "Guardar Cambios",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Cambios guardados correctamente.")
                        }
                    }
                )

                Spacer(Modifier.height(80.dp)) // Espacio adicional para que el scroll no tape el botón
            }

            // Texto de versión anclado dinámicamente al fondo (sin align)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Versión $appVersion",
                    color = Color(0xFFAEAEAE),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = Color(0xFFAEAEAE),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color(0xFF616161),
                modifier = Modifier.size(35.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF008D96),
            unfocusedBorderColor = Color(0xFFD3D3D3),
            cursorColor = Color(0xFF008D96),
            focusedLabelColor = Color(0xFF008D96)
        ),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF616161)
        )
    )
}


// Upload profile image to Amplify Storage
fun uploadProfileImage(
    context: Context,
    imageUri: Uri,
    userId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    try {
        onLoading(true)
        Log.d("ProfileUpload", "Starting upload for user: $userId")

        // Create a temporary file from the URI
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val tempFile = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        val storagePath = StoragePath.fromString("public/profile-images/$userId.jpg")
        Log.d("ProfileUpload", "Storage path: public/profile-images/$userId.jpg")

        Amplify.Storage.uploadFile(
            storagePath,
            tempFile,
            { result ->
                Log.d("ProfileUpload", "Upload completed: ${result.path}")
                onLoading(false)
                // Clean up temp file
                tempFile.delete()
                // Get the download URL
                getProfileImageUrl(storagePath, onSuccess, onError)
            },
            { error ->
                Log.e("ProfileUpload", "Upload failed", error)
                onLoading(false)
                tempFile.delete()
                onError(error.message ?: "Error desconocido")
            }
        )
    } catch (e: Exception) {
        Log.e("ProfileUpload", "Exception during upload", e)
        onLoading(false)
        onError(e.message ?: "Error desconocido")
    }
}

// Download profile image from Amplify Storage
fun downloadProfileImage(
    context: Context,
    userId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    try {
        onLoading(true)
        Log.d("ProfileDownload", "Starting download for user: $userId")

        val storagePath = StoragePath.fromString("public/profile-images/$userId.jpg")
        Log.d("ProfileDownload", "Storage path: public/profile-images/$userId.jpg")
        val localFile = File(context.cacheDir, "downloaded_profile_$userId.jpg")

        Amplify.Storage.downloadFile(
            storagePath,
            localFile,
            { result ->
                Log.d("ProfileDownload", "Download completed: ${result.file.path}")
                onLoading(false)
                onSuccess(localFile.absolutePath)
            },
            { error ->
                Log.e("ProfileDownload", "Download failed", error)
                onLoading(false)
                onError(error.message ?: "Error desconocido")
            }
        )
    } catch (e: Exception) {
        Log.e("ProfileDownload", "Exception during download", e)
        onLoading(false)
        onError(e.message ?: "Error desconocido")
    }
}

// Get download URL for uploaded image
private fun getProfileImageUrl(
    storagePath: StoragePath,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    Amplify.Storage.getUrl(
        storagePath,
        { result ->
            Log.d("ProfileDownload", "Got URL: ${result.url}")
            onSuccess(result.url.toString())
        },
        { error ->
            Log.e("ProfileDownload", "Failed to get URL", error)
            onError(error.message ?: "Error obteniendo URL")
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditProfilePreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        EditProfile(nav = nav)
    }
}
