package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF616161)
private val TextSecondary = Color(0xFFAEAEAE)
private val Danger        = Color(0xFFDC3A2C)

@Composable
fun Profile(nav: NavHostController) {
    // Datos temporales internos (para no pasar props)
    val name = "Ivan Serrano de León"
    val email = "ivandl@beneficio.com"
    val appVersion = "1.0.01"
    var selectedTab by remember { mutableStateOf(BJTab.Perfil) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BJBottomBar(
                selected = selectedTab,
                onSelect = { tab ->
                    selectedTab = tab
                    // Navegación básica; ajusta las rutas si cambian
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

                // Header
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

                // Avatar
                Image(
                    painter = painterResource(id = R.drawable.user_icon),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50))
                )

                Spacer(Modifier.height(16.dp))

                // Nombre y correo
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

                // Opciones
                ProfileItemCard(
                    icon = Icons.Outlined.PersonOutline,
                    title = "Editar Perfil",
                    subtitle = "Actualiza tu información personal",
                    onClick = { /* nav.navigate(...) */ }
                )
                ProfileItemCard(
                    icon = Icons.Outlined.MonitorHeart,
                    title = "Historial",
                    subtitle = "Actividad reciente de cupones",
                    onClick = { /* nav.navigate(...) */ }
                )
                ProfileItemCard(
                    icon = Icons.Outlined.Settings,
                    title = "Configuración",
                    subtitle = "Preferencias y notificaciones",
                    onClick = { /* nav.navigate(...) */ }
                )
                ProfileItemCard(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    title = "Ayuda y Soporte",
                    subtitle = "Preguntas frecuentes y contacto",
                    onClick = { /* nav.navigate(...) */ }
                )
                ProfileItemCard(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = "Cerrar Sesión",
                    subtitle = "Hasta la próxima :)",
                    onClick = { /* nav to login & limpiar sesión en el futuro */ },
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
                        Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFF9AA1AA), modifier = Modifier.size(20.dp))
        }
    }
}

/* --- Preview con nav falso --- */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfilePreview() {
    BeneficioJuventudTheme(darkTheme = false) {
        val nav = rememberNavController()
        Profile(nav = nav)
    }
}
