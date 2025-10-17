package mx.itesm.beneficiojuventud.view

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.model.users.UserProfile
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var selectedTab by remember { mutableStateOf(BJTab.Profile) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appVersion = "1.0.01"

    // ====== UI state (editable) ======
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastNamePat by rememberSaveable { mutableStateOf("") }
    var lastNameMat by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var birthDisplay by rememberSaveable { mutableStateOf("") } // dd/MM/yyyy

    var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    // Flag para detectar confirmación de guardado
    var justSaved by remember { mutableStateOf(false) }

    // ====== Load current Cognito user ======
    LaunchedEffect(Unit) { authViewModel.getCurrentUser() }
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val actualUserId = currentUserId ?: "anonymous"
    Log.d("EditProfile", "Current User ID: $actualUserId")

    // ====== Load user profile from backend ======
    val backendUser by userViewModel.userState.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMsg by userViewModel.error.collectAsState()

    // Cargar datos cuando tenemos el cognitoId
    LaunchedEffect(actualUserId) {
        if (actualUserId != "anonymous") {
            userViewModel.getUserById(actualUserId)
            // Intenta descargar la foto de perfil existente
            runCatching {
                downloadProfileImage(
                    context,
                    actualUserId,
                    onSuccess = { url -> profileImageUrl = url },
                    onError = { Log.d("EditProfile", "Storage not configured yet: $it") },
                    onLoading = { loading -> isDownloading = loading }
                )
            }
        }
    }

    // Sincroniza el estado de UI cuando llegan datos del backend
    LaunchedEffect(backendUser) {
        backendUser.name?.let { firstName = it }
        backendUser.lastNamePaternal?.let { lastNamePat = it }
        backendUser.lastNameMaternal?.let { lastNameMat = it }
        backendUser.email?.let { email = it }
        backendUser.phoneNumber?.let { phone = it }
        backendUser.birthDate?.let { birthDisplay = isoToDisplay(it) }
    }

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            BJTopHeader(title = "Editar Perfil", nav = nav)
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ===== Avatar =====
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploading || isDownloading || isLoading -> {
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
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                            )
                        }
                        avatarUri != null -> {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
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
                        if (!isUploading && actualUserId != "anonymous") {
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                // ===== Campos =====
                ProfileTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "Nombre",
                    leadingIcon = Icons.Outlined.Person
                )
                ProfileTextField(
                    value = lastNamePat,
                    onValueChange = { lastNamePat = it },
                    label = "Apellido Paterno",
                    leadingIcon = Icons.Outlined.Person
                )
                ProfileTextField(
                    value = lastNameMat,
                    onValueChange = { lastNameMat = it },
                    label = "Apellido Materno",
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

                // === Campo Fecha (abre DatePicker del sistema al tocar cualquier parte) ===
                DatePickerField(
                    value = birthDisplay,
                    label = "Fecha de Nacimiento (dd/MM/yyyy)",
                    leadingIcon = Icons.Outlined.CalendarMonth,
                    onDateSelected = { newDisplay ->
                        birthDisplay = newDisplay
                    }
                )

                Spacer(Modifier.height(24.dp))

                MainButton(
                    text = if (isLoading) "Guardando..." else "Guardar Cambios",
                    onClick = {
                        if (actualUserId == "anonymous") {
                            scope.launch { snackbarHostState.showSnackbar("Inicia sesión para guardar cambios.") }
                            return@MainButton
                        }

                        val birthIso = displayToIsoOrNull(birthDisplay)
                        if (birthDisplay.isNotBlank() && birthIso == null) {
                            scope.launch { snackbarHostState.showSnackbar("Fecha inválida. Usa formato dd/MM/yyyy.") }
                            return@MainButton
                        }

                        val update = UserProfile(
                            name = firstName.ifBlank { null },
                            lastNamePaternal = lastNamePat.ifBlank { null },
                            lastNameMaternal = lastNameMat.ifBlank { null },
                            email = email.ifBlank { null },
                            phoneNumber = phone.ifBlank { null },
                            birthDate = birthIso
                        )

                        justSaved = true
                        userViewModel.updateUser(actualUserId, update)

                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss() // cierra cualquier snackbar previo
                            snackbarHostState.showSnackbar(
                                message = "Guardando cambios…",
                                withDismissAction = false,
                                duration = SnackbarDuration.Indefinite
                            )
                        }
                    },
                    enabled = !isLoading
                )

                if (!errorMsg.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorMsg ?: "",
                        color = Color(0xFFB00020),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(80.dp))
            }

            // Versión
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

    // Feedback de éxito y navegación a Profile cuando termine el guardado sin error
    LaunchedEffect(isLoading, errorMsg, backendUser) {
        if (justSaved && !isLoading) {
            if (errorMsg.isNullOrBlank()) {
                // Éxito: cerrar "Guardando…" y mostrar "Cambios guardados" sin bloquear
                snackbarHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Cambios guardados",
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )
                }

                justSaved = false

                // Navegar inmediatamente (sin esperar al snackbar)
                nav.navigate(Screens.Profile.route) {
                    popUpTo(Screens.Profile.route) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                // Error: sustituir snackbar y NO bloquear
                snackbarHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMsg ?: "Error al guardar",
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                }
                justSaved = false
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
    leadingIcon: ImageVector,
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

// =================== Campo de Fecha con DatePicker del sistema ===================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    value: String,
    label: String,
    leadingIcon: ImageVector,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val interaction = remember { MutableInteractionSource() }

    fun showSystemDatePicker() {
        val initial = runCatching { LocalDate.parse(value.trim(), displayFormatter) }
            .getOrElse { LocalDate.now() }

        DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val picked = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                onDateSelected(picked.format(displayFormatter))
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    OutlinedTextField(
        value = value,
        onValueChange = { /* read-only visual */ },
        readOnly = true,
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
                modifier = Modifier
                    .size(35.dp)
                    .clickable(
                        interactionSource = interaction,
                        indication = null
                    ) { showSystemDatePicker() }
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable( // <-- ahora toca en cualquier parte del campo
                interactionSource = interaction,
                indication = null
            ) { showSystemDatePicker() },
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

// =================== Helpers de fecha ===================

private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

/** Convierte ISO que puede venir como "2025-10-02" o "2025-10-02T00:00:00.000Z" a "dd/MM/yyyy". */
private fun isoToDisplay(iso: String): String {
    val trimmed = iso.trim()
    return try {
        if (trimmed.contains("T")) {
            val odt = OffsetDateTime.parse(trimmed)
            odt.toLocalDate().format(displayFormatter)
        } else {
            LocalDate.parse(trimmed, isoDateFormatter).format(displayFormatter)
        }
    } catch (e: DateTimeParseException) {
        runCatching {
            LocalDate.parse(trimmed.substring(0, 10), isoDateFormatter).format(displayFormatter)
        }.getOrElse { trimmed }
    }
}

/** Convierte "dd/MM/yyyy" a "yyyy-MM-dd" para el backend; si está vacío o inválido, regresa null. */
private fun displayToIsoOrNull(display: String): String? {
    val clean = display.trim()
    if (clean.isBlank()) return null
    return try {
        LocalDate.parse(clean, displayFormatter).format(isoDateFormatter)
    } catch (_: Exception) {
        null
    }
}

// =================== Storage helpers (sin cambios) ===================

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
