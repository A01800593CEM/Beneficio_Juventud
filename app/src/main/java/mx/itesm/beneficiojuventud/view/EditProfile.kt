package mx.itesm.beneficiojuventud.view

import android.net.Uri
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
import androidx.compose.material.icons.outlined.ChevronRight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.utils.ImageStorageManager
import mx.itesm.beneficiojuventud.utils.AmplifyUserHelper
import com.amplifyframework.core.Amplify
import android.util.Log
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfile(nav: NavHostController, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(BJTab.Perfil) }
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
    var isUploadingImage by remember { mutableStateOf(false) }
    var currentUserProfileImageKey by remember { mutableStateOf<String?>(null) }

    // Cargar imagen existente al abrir la pantalla
    LaunchedEffect(Unit) {
        val userId = AmplifyUserHelper.getCurrentUserId()
        if (userId != null) {
            // Usar directamente el path estándar del usuario
            val userImagePath = "profile-images/$userId.jpg"
            Log.d("EditProfile", "Intentando cargar imagen del usuario: $userImagePath")

            val result = ImageStorageManager.getProfileImageUrl(userImagePath)
            result.fold(
                onSuccess = { url ->
                    profileImageUrl = url
                    currentUserProfileImageKey = userImagePath
                    Log.d("EditProfile", "Imagen del usuario cargada: $userImagePath")
                },
                onFailure = { error ->
                    Log.d("EditProfile", "No hay imagen de perfil para el usuario: ${error.message}")
                }
            )
        }
    }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri
            profileImageUrl = null // Limpiar URL anterior al seleccionar nueva imagen
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
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = "Volver",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Editar Perfil",
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
                        BJTab.Menu      -> nav.navigate(Screens.MainMenu.route)
                        BJTab.Cupones   -> { /* nav.navigate(...) */ }
                        BJTab.Favoritos -> { /* nav.navigate(...) */ }
                        BJTab.Perfil    -> Unit
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
                            if (!isUploadingImage) {
                                pickImage.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploadingImage -> {
                            // Mostrar loading mientras se sube la imagen
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = Color(0xFF008D96)
                            )
                        }
                        avatarUri != null -> {
                            // Imagen seleccionada localmente
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(CircleShape)
                            )
                        }
                        profileImageUrl != null -> {
                            // Imagen desde S3
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(CircleShape)
                            )
                        }
                        else -> {
                            // Imagen por defecto
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
                    text = if (isUploadingImage) "Guardando..." else "Guardar Cambios",
                    onClick = {
                        scope.launch {
                            try {
                                var newImageKey: String? = null

                                // Si hay una nueva imagen seleccionada, subirla primero
                                if (avatarUri != null) {
                                    isUploadingImage = true

                                    // Obtener el ID del usuario autenticado
                                    val userId = AmplifyUserHelper.getCurrentUserId()
                                    if (userId == null) {
                                        snackbarHostState.showSnackbar("Error: Usuario no autenticado")
                                        return@launch
                                    }

                                    val uploadResult = ImageStorageManager.uploadProfileImage(
                                        context = context,
                                        imageUri = avatarUri!!,
                                        userId = userId
                                    )

                                    uploadResult.fold(
                                        onSuccess = { imageKey ->
                                            newImageKey = imageKey
                                            currentUserProfileImageKey = imageKey
                                            avatarUri = null // Limpiar URI local

                                            // La imagen se guarda automáticamente con el path del usuario
                                            Log.d("EditProfile", "Imagen subida y guardada exitosamente: $imageKey")

                                            // Cargar la nueva URL para mostrar inmediatamente
                                            scope.launch {
                                                val urlResult = ImageStorageManager.getProfileImageUrl(imageKey)
                                                urlResult.fold(
                                                    onSuccess = { url ->
                                                        profileImageUrl = url
                                                        Log.d("EditProfile", "URL de imagen obtenida: $url")
                                                    },
                                                    onFailure = { urlError ->
                                                        Log.e("EditProfile", "Error obteniendo URL: ${urlError.message}")
                                                    }
                                                )
                                            }
                                        },
                                        onFailure = { error ->
                                            Log.e("EditProfile", "Error subiendo imagen: ${error.message}")
                                        }
                                    )

                                    isUploadingImage = false
                                }

                                snackbarHostState.showSnackbar("Cambios guardados correctamente.")

                            } catch (e: Exception) {
                                isUploadingImage = false
                                Log.e("EditProfile", "Error guardando cambios: ${e.message}")
                                snackbarHostState.showSnackbar("Error al guardar cambios: ${e.message}")
                            }
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


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditProfilePreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        EditProfile(nav = nav)
    }
}
