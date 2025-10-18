package mx.itesm.beneficiojuventud.viewcollab

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
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarDuration
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StoragePath
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileCollab(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val appVersion = "1.0.01"
    val context = LocalContext.current // 👈 toma el contexto UNA vez, fuera de effects

    // ===== UI state =====
    var businessName by rememberSaveable { mutableStateOf("") }
    var representativeName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var postalCode by rememberSaveable { mutableStateOf("") }
    var rfc by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var logoUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // ===== Auth / Collab =====
    LaunchedEffect(Unit) { authViewModel.getCurrentUser() }
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val collabId = currentUserId ?: "anonymous"

    val collabState by collabViewModel.collabState.collectAsState()

    // Carga de colaborador (suspend OK dentro de LaunchedEffect)
    LaunchedEffect(collabId) {
        if (collabId != "anonymous") {
            try {
                collabViewModel.getCollaboratorById(collabId)
            } catch (t: Throwable) {
                Log.e("EditProfileCollab", "Error load collab", t)
            }

            // Descarga logo (usa el 'context' obtenido arriba, NO LocalContext.current aquí)
            downloadCollabLogo(
                context = context,
                collabId = collabId,
                onSuccess = { path -> logoUrl = path },
                onError = { msg -> Log.d("EditProfileCollab", "No logo yet: $msg") },
                onLoading = { loading -> isDownloading = loading }
            )
        }
    }

    // Sincroniza campos cuando llegan datos del backend
    LaunchedEffect(collabState) {
        businessName = collabState.businessName ?: ""
        representativeName = collabState.representativeName ?: ""
        email = collabState.email ?: ""
        phone = collabState.phone ?: ""
        address = collabState.address ?: ""
        postalCode = collabState.postalCode ?: ""
        rfc = collabState.rfc ?: ""
        description = collabState.description ?: ""
        if (!collabState.logoUrl.isNullOrBlank()) logoUrl = collabState.logoUrl
    }

    // Picker de imagen
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && collabId != "anonymous") {
            avatarUri = uri
            uploadCollabLogo(
                context = context,
                imageUri = uri,
                collabId = collabId,
                onSuccess = { url ->
                    logoUrl = url
                    scope.launch { snackbarHostState.showSnackbar("Logo actualizado correctamente") }
                },
                onError = { error ->
                    scope.launch { snackbarHostState.showSnackbar("Error al subir el logo: $error") }
                },
                onLoading = { loading -> isUploading = loading }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = { BJTopHeader(title = "Editar Perfil de Colaborador", nav = nav) },
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
                // ===== Logo =====
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
                        logoUrl != null -> {
                            AsyncImage(
                                model = logoUrl,
                                contentDescription = "Logo del colaborador",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                            )
                        }
                        avatarUri != null -> {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Logo del colaborador",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.user_icon),
                                contentDescription = "Logo",
                                modifier = Modifier.size(90.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Cambiar Logo",
                    color = Color(0xFF008D96),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        if (!isUploading && collabId != "anonymous") {
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                // ===== Campos =====
                ProfileTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = "Nombre del Negocio",
                    leadingIcon = Icons.Outlined.Home
                )
                ProfileTextField(
                    value = representativeName,
                    onValueChange = { representativeName = it },
                    label = "Nombre del Representante",
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
                    value = address,
                    onValueChange = { address = it },
                    label = "Dirección",
                    leadingIcon = Icons.Outlined.Place
                )
                ProfileTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = "Código Postal",
                    leadingIcon = Icons.Outlined.Place,
                    keyboardType = KeyboardType.Number
                )
                ProfileTextField(
                    value = rfc,
                    onValueChange = { rfc = it.uppercase() },
                    label = "RFC",
                    leadingIcon = Icons.Outlined.Badge
                )
                ProfileTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    leadingIcon = Icons.Outlined.Description
                )

                Spacer(Modifier.height(24.dp))

                MainButton(
                    text = if (isSaving) "Guardando..." else "Guardar Cambios",
                    onClick = {
                        if (collabId == "anonymous") {
                            scope.launch { snackbarHostState.showSnackbar("Inicia sesión para guardar cambios.") }
                            return@MainButton
                        }
                        if (businessName.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("El nombre del negocio es obligatorio.") }
                            return@MainButton
                        }

                        val update = Collaborator(
                            businessName = businessName.ifBlank { null },
                            representativeName = representativeName.ifBlank { null },
                            email = email.ifBlank { null },
                            phone = phone.ifBlank { null },
                            address = address.ifBlank { null },
                            postalCode = postalCode.ifBlank { null },
                            rfc = rfc.ifBlank { null },
                            description = description.ifBlank { null },
                            logoUrl = logoUrl
                        )

                        isSaving = true
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = "Guardando cambios…",
                                withDismissAction = false,
                                duration = SnackbarDuration.Indefinite
                            )

                            try {
                                collabViewModel.updateCollaborator(collabId, update)
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("Cambios guardados", withDismissAction = true)
                                nav.popBackStack()
                            } catch (e: Exception) {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    "Error al guardar: ${e.message ?: "desconocido"}",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Long
                                )
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving
                )

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
}

/** Reusa el estilo del campo de perfil del app de usuarios */
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

/* =================== Storage helpers específicos de COLLAB =================== */

fun uploadCollabLogo(
    context: Context,
    imageUri: Uri,
    collabId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    try {
        onLoading(true)
        Log.d("CollabLogoUpload", "Starting upload for collab: $collabId")

        val inputStream = context.contentResolver.openInputStream(imageUri)
        val tempFile = File(context.cacheDir, "collab_logo_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)
        inputStream?.use { input -> outputStream.use { out -> input.copyTo(out) } }

        val storagePath = StoragePath.fromString("public/collab-logos/$collabId.jpg")
        Log.d("CollabLogoUpload", "Storage path: public/collab-logos/$collabId.jpg")

        Amplify.Storage.uploadFile(
            storagePath,
            tempFile,
            { result ->
                Log.d("CollabLogoUpload", "Upload completed: ${result.path}")
                onLoading(false)
                tempFile.delete()
                getCollabLogoUrl(storagePath, onSuccess, onError)
            },
            { error ->
                Log.e("CollabLogoUpload", "Upload failed", error)
                onLoading(false)
                tempFile.delete()
                onError(error.message ?: "Error desconocido")
            }
        )
    } catch (e: Exception) {
        Log.e("CollabLogoUpload", "Exception during upload", e)
        onLoading(false)
        onError(e.message ?: "Error desconocido")
    }
}

fun downloadCollabLogo(
    context: Context,
    collabId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    try {
        onLoading(true)
        Log.d("CollabLogoDownload", "Starting download for collab: $collabId")

        val storagePath = StoragePath.fromString("public/collab-logos/$collabId.jpg")
        Log.d("CollabLogoDownload", "Storage path: public/collab-logos/$collabId.jpg")
        val localFile = File(context.cacheDir, "downloaded_collab_logo_$collabId.jpg")

        Amplify.Storage.downloadFile(
            storagePath,
            localFile,
            { result ->
                Log.d("CollabLogoDownload", "Download completed: ${result.file.path}")
                onLoading(false)
                onSuccess(localFile.absolutePath)
            },
            { error ->
                Log.e("CollabLogoDownload", "Download failed", error)
                onLoading(false)
                onError(error.message ?: "Error desconocido")
            }
        )
    } catch (e: Exception) {
        Log.e("CollabLogoDownload", "Exception during download", e)
        onLoading(false)
        onError(e.message ?: "Error desconocido")
    }
}

private fun getCollabLogoUrl(
    storagePath: StoragePath,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    Amplify.Storage.getUrl(
        storagePath,
        { result ->
            Log.d("CollabLogoDownload", "Got URL: ${result.url}")
            onSuccess(result.url.toString())
        },
        { error ->
            Log.e("CollabLogoDownload", "Failed to get URL", error)
            onError(error.message ?: "Error obteniendo URL")
        }
    )
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun EditProfileCollabPreview() {
    EditProfileCollab(nav = rememberNavController())
}
