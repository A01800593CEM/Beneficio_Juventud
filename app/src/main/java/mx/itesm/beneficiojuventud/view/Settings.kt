package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

/**
 * Pantalla de Configuración con preferencias de notificaciones, correo, ubicación y accesos a acciones de cuenta.
 * Incluye top bar con regreso, divider con gradiente y bottom bar con tabs de la app.
 * @param nav Controlador de navegación para cambiar de pestañas o abrir pantallas relacionadas.
 * @param modifier Modificador externo para composición/pruebas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(nav: NavHostController, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(BJTab.Profile) }
    val appVersion = "1.0.01"

    // Estados de switches (idealmente elevados a ViewModel si se persisten)
    var pushEnabled by remember { mutableStateOf(false) }
    var emailEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
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
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BackButton(nav = nav)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Configuración",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SettingItemSwitch(
                        icon = Icons.Outlined.NotificationsActive,
                        title = "Notificaciones Push",
                        subtitle = "Recibe alertas de nuevos cupones",
                        checked = pushEnabled,
                        onCheckedChange = { pushEnabled = it }
                    )
                }
                item {
                    SettingItemSwitch(
                        icon = Icons.Outlined.Email,
                        title = "Ofertas por Email",
                        subtitle = "Promociones exclusivas en tu correo",
                        checked = emailEnabled,
                        onCheckedChange = { emailEnabled = it }
                    )
                }
                item {
                    SettingItemSwitch(
                        icon = Icons.Outlined.LocationOn,
                        title = "Ubicación",
                        subtitle = "Ofertas cerca de ti",
                        checked = locationEnabled,
                        onCheckedChange = { locationEnabled = it }
                    )
                }
                item {
                    SettingItemNavigable(
                        icon = Icons.Outlined.Lock,
                        title = "Cambiar Contraseña",
                        subtitle = "Actualizar credenciales de acceso",
                        onClick = { /* nav.navigate(Screens.ChangePassword.route) */ }
                    )
                }
                item {
                    SettingItemNavigable(
                        icon = Icons.Outlined.AllInclusive,
                        title = "Prueba de IA (esto se moverá)",
                        subtitle = "Test",
                        onClick = { nav.navigate(Screens.GenerarPromocion.route)}
                    )
                }
                item {
                    DangerItem(
                        icon = Icons.Outlined.DeleteOutline,
                        title = "Eliminar Cuenta",
                        onClick = { /* TODO: confirmar y eliminar */ }
                    )
                }
            }

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

// ---------- Componentes reutilizables ----------

/**
 * Contenedor estilizado para ítems de configuración con borde sutil y padding interno.
 * @param content Contenido en una Row que compone el ítem.
 */
@Composable
private fun SettingSurface(content: @Composable RowScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD3D3D3))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Ítem de configuración con interruptor para activar/desactivar una preferencia.
 * @param icon Icono principal del ítem.
 * @param title Título de la preferencia.
 * @param subtitle Descripción corta de la preferencia.
 * @param checked Estado actual del switch.
 * @param onCheckedChange Callback con el nuevo estado.
 */
@Composable
private fun SettingItemSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingSurface {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF616161),
            modifier = Modifier
                .size(40.dp)
                .padding(end = 8.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF616161)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color(0xFFAEAEAE)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(1f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF008D96),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}

/**
 * Ítem navegable de configuración que abre otra pantalla o flujo al tocarse.
 * @param icon Icono principal del ítem.
 * @param title Título de la acción.
 * @param subtitle Descripción corta de la acción.
 * @param onClick Acción al tocar la fila.
 */
@Composable
private fun SettingItemNavigable(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE6E6E6))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF616161),
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF616161)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFFAEAEAE)
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFBDBDBD)
            )
        }
    }
}

/**
 * Ítem de acción peligrosa (destructiva) como eliminar cuenta.
 * @param icon Icono con color de alerta.
 * @param title Texto de la acción.
 * @param onClick Acción al tocar la fila.
 */
@Composable
private fun DangerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE6E6E6))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = title,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Vista previa de la pantalla de Configuración con tema y sistema UI visibles.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Settings(nav = nav)
    }
}
