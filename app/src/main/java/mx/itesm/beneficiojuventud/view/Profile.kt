package mx.itesm.beneficiojuventud.view

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StoragePath
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import java.io.File

private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF616161)
private val TextSecondary = Color(0xFFAEAEAE)
private val Danger        = Color(0xFFDC3A2C)

/**
 * Pantalla de perfil del usuario.
 * Lee name y email desde userViewModel.userState (perfil del backend),
 * descarga avatar de S3 usando el cognitoId actual y permite cerrar sesión.
 */
@Composable
fun Profile(
    nav: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel            // ← se recibe desde el NavGraph
) {
    // --- Estado de usuario (backend) ---
    val user by userViewModel.userState.collectAsState()
    val name = user.name?.takeIf { it.isNotBlank() } ?: "Usuario"
    val email = user.email?.takeIf { it.isNotBlank() } ?: (authViewModel.getCurrentUserName() ?: "—")

    val appVersion = "1.0.01"
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(BJTab.Profile) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    // Asegura tener el usuario actual de Cognito (para el sub)
    LaunchedEffect(Unit) { authViewModel.getCurrentUser() }

    val currentUserId by authViewModel.currentUserId.collectAsState()
    val actualUserId = currentUserId ?: "anonymous"
    Log.d("Profile", "Current User ID: $actualUserId")

    // Descarga avatar usando el sub como nombre de archivo
    LaunchedEffect(actualUserId) {
        try {
            downloadProfileImageForDisplay(
                context = context,
                userId = actualUserId,
                onSuccess = { url -> profileImageUrl = url },
                onError = { Log.d("Profile", "Storage not configured yet: $it") },
                onLoading = { loading -> isLoadingImage = loading }
            )
        } catch (_: Exception) {
            Log.d("Profile", "Storage not configured yet, skipping image download")
        }
    }

    // --- Estado de auth para manejar sign-out y errores ---
    val authState by authViewModel.authState.collectAsState()
    var signOutRequested by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState.isLoading, authState.error, signOutRequested) {
        if (signOutRequested && !authState.isLoading) {
            if (authState.error == null) {
                authViewModel.clearState()
                nav.navigate(Screens.LoginRegister.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                errorMsg = authState.error
                signOutRequested = false
            }
        }
    }

    if (authState.isLoading) {
        LoadingDialog()
    }

    if (errorMsg != null) {
        AlertDialog(
            onDismissRequest = { errorMsg = null },
            confirmButton = { TextButton(onClick = { errorMsg = null }) { Text("OK") } },
            title = { Text("Error") },
            text = { Text(errorMsg!!) }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_beneficio_joven),
                        contentDescription = "Logo Beneficio Joven",
                        modifier = Modifier.size(28.dp)
                    )
                    IconButton(onClick = { /* nav a notificaciones si aplica */ }) {
                        Icon(
                            Icons.Outlined.NotificationsNone,
                            contentDescription = "Notificaciones",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoadingImage -> {
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
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.user_icon),
                                contentDescription = "Avatar",
                                modifier = Modifier.size(90.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = name,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = email,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                GradientDivider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                Spacer(Modifier.height(16.dp))

                ProfileItemCard(
                    icon = Icons.Outlined.PersonOutline,
                    title = "Editar Perfil",
                    subtitle = "Actualiza tu información personal",
                    onClick = { nav.navigate(Screens.EditProfile.route) }
                )
                ProfileItemCard(
                    icon = Icons.Outlined.MonitorHeart,
                    title = "Historial",
                    subtitle = "Actividad reciente de cupones",
                    onClick = { nav.navigate(Screens.History.route) }
                )
                ProfileItemCard(
                    icon = Icons.Outlined.Settings,
                    title = "Configuración",
                    subtitle = "Preferencias y notificaciones",
                    onClick = { nav.navigate(Screens.Settings.route) }
                )
                ProfileItemCard(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = "Ayuda y Soporte",
                    subtitle = "Preguntas frecuentes y contacto",
                    onClick = { nav.navigate(Screens.Help.route) }
                )
                ProfileItemCard(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = "Cerrar Sesión",
                    subtitle = "Hasta la próxima :)",
                    onClick = {
                        signOutRequested = true
                        authViewModel.signOut(globalSignOut = true)
                    },
                    isLogout = true
                )

                Spacer(Modifier.height(16.dp))
            }

            Text(
                text = "Versión $appVersion",
                color = TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Diálogo modal de carga mostrado durante el proceso de cierre de sesión.
 * Bloquea la interacción hasta que finaliza la operación.
 */
@Composable
private fun LoadingDialog() {
    AlertDialog(
        onDismissRequest = { /* bloqueado mientras carga */ },
        confirmButton = {},
        title = { Text("Cerrando Sesión...") },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(strokeWidth = 3.dp)
                Text("Por favor espera…")
            }
        }
    )
}

/**
 * Ítem de la lista de opciones del perfil con icono, título y subtítulo.
 * Ajusta estilos y color de acento cuando representa la acción de cerrar sesión.
 */
@Composable
private fun ProfileItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLogout: Boolean = false
) {
    val textColor = if (isLogout) Danger else TextPrimary
    val iconColor = if (isLogout) Danger else Color(0xFF616161)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(10.dp), clip = false)
            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = title, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF9AA1AA), modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * Descarga la imagen de perfil desde S3 (ruta public/profile-images/{userId}.jpg) y la guarda en caché local para mostrarla.
 * Gestiona callbacks de éxito, error y estado de carga.
 */
fun downloadProfileImageForDisplay(
    context: Context,
    userId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    try {
        onLoading(true)
        Log.d("ProfileDisplay", "Starting download for user: $userId")

        val storagePath = StoragePath.fromString("public/profile-images/$userId.jpg")
        Log.d("ProfileDisplay", "Storage path: public/profile-images/$userId.jpg")
        val localFile = File(context.cacheDir, "displayed_profile_$userId.jpg")

        Amplify.Storage.downloadFile(
            storagePath,
            localFile,
            { result ->
                Log.d("ProfileDisplay", "Download completed: ${result.file.path}")
                onLoading(false)
                onSuccess(localFile.absolutePath)
            },
            { error ->
                Log.e("ProfileDisplay", "Download failed", error)
                onLoading(false)
                onError(error.message ?: "Error desconocido")
            }
        )
    } catch (e: Exception) {
        Log.e("ProfileDisplay", "Exception during download", e)
        onLoading(false)
        onError(e.message ?: "Error desconocido")
    }
}

/**
 * Previsualiza la pantalla de perfil con un NavController de prueba.
 * (Para la preview no hay userViewModel real; se usa un fake básico).
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfilePreview() {
    BeneficioJuventudTheme(darkTheme = false) {
        val nav = rememberNavController()
        // Para preview únicamente, creamos un VM local con viewModel(),
        // en la app real pásalo desde AppContent/AppNav.
        val authVm: AuthViewModel = viewModel()
        val userVm: UserViewModel = viewModel()
        Profile(nav = nav, authViewModel = authVm, userViewModel = userVm)
    }
}
