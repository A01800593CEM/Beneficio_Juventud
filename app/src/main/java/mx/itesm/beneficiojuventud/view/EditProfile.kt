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
import androidx.compose.material.icons.outlined.ChevronLeft
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
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Pantalla para editar el perfil del usuario.
 * Permite cambiar nombre, correo, teléfono, fecha de nacimiento y actualizar la foto de perfil en Amplify Storage.
 * Carga el ID del usuario activo y recupera la imagen de perfil al iniciar.
 * @param nav Controlador de navegación para moverse entre pantallas.
 * @param modifier Modificador para ajustar el contenedor de la pantalla.
 * @param authViewModel ViewModel de autenticación usado para obtener el usuario actual.
 */
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

    // Carga el usuario al abrir la pantalla
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
            uploadProfileImage(
                context,
                uri,
                actualUserId,
                onSuccess = { url ->
                    profileImageUrl = url
                    scope.launch { snackbarHostState.showSnackbar("Foto de perfil actualizada correctamente") }
                },
                onError = { error ->
                    scope.launch { snackbarHostState.showSnackbar("Error al subir la imagen: $error") }
                },
                onLoading = { loading -> isUploading = loading }
            )
        }
    }

    // Intenta descargar la foto de perfil existente
    LaunchedEffect(actualUserId) {
        try {
            downloadProfileImage(
                context,
                actualUserId,
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
            EditProfileTopBar(
                title = "Editar Perfil",
                nav = nav
            )
        },
        bottomBar = {
            BJBottomBar(
                selected = selectedTab,
                onSelect = { tab ->
                    selectedTab = tab
                    when (tab) {
                        BJTab.Home      -> nav.navigate(Screens.Home.route)
                        BJTab.Coupons   -> nav.navigate(Screens.Coupons.route)
                        BJTab.Favorites -> nav.navigate(Screens.Favorites.route)
                        BJTab.Profile   -> nav.navigate(Screens.Profile.route)
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
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
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
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        if (!isUploading) {
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
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

                MainButton(
                    text = "Guardar Cambios",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Cambios guardados correctamente.")
                        }
                    }
                )

                Spacer(Modifier.height(80.dp))
            }

            // Versión anclada al fondo
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

/**
 * Barra superior reutilizable para la pantalla de edición de perfil.
 * Muestra logo centrado, botón atrás y acceso a notificaciones.
 * @param title Título de la pantalla.
 * @param nav Controlador de navegación usado por el botón de regreso.
 */
@Composable
private fun EditProfileTopBar(
    title: String,
    nav: NavHostController
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo_beneficio_joven),
                contentDescription = "Logo",
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(
                    nav = nav,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF616161)
                )
            }
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = "Notificaciones",
                tint = Color(0xFF008D96),
                modifier = Modifier.size(26.dp)
            )
        }

        GradientDivider(
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Campo de texto estilizado para datos de perfil.
 * Aplica ícono inicial, borde personalizado y opciones de teclado según el tipo.
 * @param value Valor actual del campo.
 * @param onValueChange Callback invocado cuando cambia el texto.
 * @param label Etiqueta mostrada como hint/label.
 * @param leadingIcon Ícono a la izquierda del campo.
 * @param keyboardType Tipo de teclado para la entrada.
 */
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

/**
 * Sube la imagen de perfil del usuario a Amplify Storage.
 * Crea un archivo temporal a partir del URI, lo sube y obtiene la URL de descarga.
 * @param context Contexto para resolver el contenido y el caché.
 * @param imageUri URI de la imagen seleccionada.
 * @param userId Identificador del usuario; se usa para nombrar el archivo en el storage.
 * @param onSuccess Callback con la URL pública o firmada de la imagen subida.
 * @param onError Callback con el mensaje de error en caso de fallo.
 * @param onLoading Callback que expone el estado de carga durante la operación.
 */
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
                tempFile.delete()
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

/**
 * Descarga la imagen de perfil desde Amplify Storage a un archivo local.
 * Útil cuando no se dispone de una URL pública o se prefiere caché local.
 * @param context Contexto para crear el archivo en caché.
 * @param userId Identificador del usuario; determina la ruta del archivo en el storage.
 * @param onSuccess Callback con la ruta local absoluta del archivo descargado.
 * @param onError Callback con el mensaje de error en caso de fallo.
 * @param onLoading Callback que expone el estado de carga durante la operación.
 */
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

/**
 * Obtiene la URL de descarga para una imagen previamente subida a Amplify Storage.
 * @param storagePath Ruta del archivo en el storage.
 * @param onSuccess Callback con la URL generada por el proveedor de almacenamiento.
 * @param onError Callback con el mensaje de error en caso de fallo.
 */
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

/**
 * Vista previa de la pantalla de edición de perfil bajo el tema de la app.
 * Permite validar el layout en el inspector sin ejecutar en dispositivo.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditProfilePreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        EditProfile(nav = nav)
    }
}
