package mx.itesm.beneficiojuventud.viewcollab

import mx.itesm.beneficiojuventud.view.Screens
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
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StoragePath
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import java.io.File

private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF616161)
private val TextSecondary = Color(0xFFAEAEAE)
private val Danger = Color(0xFFDC3A2C)

/**
 * Perfil COLABORADOR: muestra datos desde CollabViewModel.collabState (Collaborator).
 */
@Composable
fun ProfileCollab(
    nav: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    val context = LocalContext.current
    val collab by collabViewModel.collabState.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val collabId = collab.cognitoId ?: currentUserId

    val displayName = collab.businessName?.takeIf { it.isNotBlank() } ?: "Colaborador"
    val displayEmail = collab.email?.takeIf { it.isNotBlank() } ?: (authViewModel.getCurrentUserName() ?: "—")
    val displaySub = collab.representativeName?.takeIf { it.isNotBlank() }

    val appVersion = "1.0.01"
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    // Cargar colaborador por ID actual
    LaunchedEffect(currentUserId) {
        currentUserId?.let { id ->
            runCatching { collabViewModel.getCollaboratorById(id) }
                .onFailure { Log.e("ProfileCollab", "Error loading collaborator: ${it.message}") }
        }
    }

    // Cargar logo/avatar
    LaunchedEffect(collabId, collab.logoUrl) {
        profileImageUrl = null
        when {
            !collab.logoUrl.isNullOrBlank() -> profileImageUrl = collab.logoUrl
            !collabId.isNullOrBlank() -> runCatching {
                downloadProfileImageForDisplay(
                    context = context,
                    userId = collabId,
                    onSuccess = { localPath -> profileImageUrl = localPath },
                    onError = { /* ignore */ },
                    onLoading = { loading -> isLoadingImage = loading }
                )
            }
        }
    }

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
        AlertDialog(onDismissRequest = {}, confirmButton = {},
            title = { Text("Cerrando Sesión...") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                    Text("Por favor espera…")
                }
            }
        )
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
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = { BJBottomBarCollab(nav = nav) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_beneficio_joven),
                        contentDescription = "Logo",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoadingImage -> CircularProgressIndicator(color = Color(0xFF008D96))
                        profileImageUrl != null -> AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Logo del negocio",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize().clip(CircleShape)
                        )
                        else -> Image(
                            painter = painterResource(id = R.drawable.user_icon),
                            contentDescription = "Avatar",
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(displayName, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                if (!displaySub.isNullOrBlank()) {
                    Text(displaySub, color = TextSecondary, fontSize = 14.sp)
                }
                Text(displayEmail, color = TextSecondary, fontSize = 14.sp)

                Spacer(Modifier.height(16.dp))
                GradientDivider(thickness = 2.dp, modifier = Modifier.fillMaxWidth().padding(8.dp))
                Spacer(Modifier.height(16.dp))

                ProfileItemCard(Icons.Outlined.PersonOutline, "Editar Perfil", "Actualiza la información del negocio") {
                    nav.navigate(Screens.EditProfileCollab.route)
                }
                ProfileItemCard(Icons.Outlined.Store, "Gestionar Sucursales", "Ver y editar tus sucursales") {
                    nav.navigate(Screens.BranchManagement.route)
                }
                ProfileItemCard(Icons.Outlined.Settings, "Configuración", "Preferencias de ubicación") {
                    nav.navigate(Screens.SettingsCollab.route)
                }
                ProfileItemCard(Icons.AutoMirrored.Outlined.HelpOutline, "Ayuda y Soporte", "Preguntas frecuentes") {
                    nav.navigate(Screens.Help.route)
                }
                ProfileItemCard(Icons.Outlined.Info, "Créditos", "Conoce al equipo de desarrollo") {
                    nav.navigate(Screens.Credits.route)
                }
                ProfileItemCard(Icons.AutoMirrored.Outlined.Logout, "Cerrar Sesión", "Hasta la próxima :)", isLogout = true) {
                    signOutRequested = true
                    authViewModel.signOut(globalSignOut = true)
                }

                Spacer(Modifier.height(16.dp))
            }

            Text("Versión $appVersion", color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ProfileItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isLogout: Boolean = false,
    onClick: () -> Unit
) {
    val textColor = if (isLogout) Danger else TextPrimary
    val iconColor = if (isLogout) Danger else Color(0xFF616161)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(10.dp))
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
                    Text(title, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    if (subtitle.isNotEmpty()) {
                        Text(
                            subtitle,
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

/** Descarga imagen de perfil desde S3 a caché local para mostrarla. */
fun downloadProfileImageForDisplay(
    context: Context,
    userId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    try {
        onLoading(true)
        val storagePath = StoragePath.fromString("public/profile-images/$userId.jpg")
        val localFile = File(context.cacheDir, "displayed_profile_$userId.jpg")

        Amplify.Storage.downloadFile(
            storagePath,
            localFile,
            {
                onLoading(false)
                onSuccess(localFile.absolutePath)
            },
            {
                onLoading(false)
                onError(it.message ?: "Error desconocido")
            }
        )
    } catch (e: Exception) {
        onLoading(false)
        onError(e.message ?: "Error desconocido")
    }
}
