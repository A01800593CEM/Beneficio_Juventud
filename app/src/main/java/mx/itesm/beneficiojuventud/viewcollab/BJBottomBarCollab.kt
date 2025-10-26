package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import mx.itesm.beneficiojuventud.components.GradientText
import mx.itesm.beneficiojuventud.view.GradientIcon
import mx.itesm.beneficiojuventud.view.Screens

/** Tabs del panel colaborador (los 4 de la barra). El 5º ícono es el botón central QR. */
enum class CollabTab(val label: String, val icon: ImageVector, val rootRoute: String) {
    Menu("Menú",       Icons.Outlined.Home,       Screens.HomeScreenCollab.route),
    Stats("Stats",     Icons.Outlined.Equalizer,  Screens.StatsScreen.route),
    Promotions("Promos", Icons.Outlined.Discount, Screens.PromotionsScreen.route),
    Profile("Perfil",  Icons.Outlined.Person,     Screens.ProfileCollab.route)
}

/** Navegación entre tabs que:
 * - Hace pop hasta HomeScreenCollab (el inicio del panel colaborador)
 * - Evita duplicados (singleTop)
 * - NO restaura estado para evitar acumulación de navegaciones
 */
private fun NavHostController.navigateToTabRoot(route: String) {
    navigate(route) {
        popUpTo(Screens.HomeScreenCollab.route) {
            inclusive = (route == Screens.HomeScreenCollab.route)
            saveState = false
        }
        launchSingleTop = true
        restoreState = false
    }
}

@Composable
fun BJBottomBarCollab(
    nav: NavHostController,
    branchId: Int? = null,
    containerColor: Color = Color(0xFFF6F6F6),
    activeBrush: Brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
    inactiveIconColor: Color = Color(0xFF616161),
    inactiveTextColor: Color = Color(0xFF616161),
    iconSize: Dp = 28.dp
) {
    val labelBase = MaterialTheme.typography.labelSmall

    // Inset inferior real
    val bottomInset = WindowInsets.navigationBars
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues()
        .calculateBottomPadding()

    // Ruta/stack actual para marcar seleccionado correctamente
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Column(Modifier.fillMaxWidth()) {
        // Barra + botón central
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            NavigationBar(
                containerColor = containerColor,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                ) {
                    @Composable
                    fun Label(text: String, isSelected: Boolean) {
                        val mod = Modifier.offset(y = (-4).dp)
                        if (isSelected) {
                            GradientText(text, activeBrush, modifier = mod)
                        } else {
                            Text(
                                text = text,
                                style = labelBase.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = inactiveTextColor,
                                modifier = mod
                            )
                        }
                    }

                    @Composable
                    fun IconC(icon: ImageVector, isSelected: Boolean) {
                        val mod = Modifier
                            .size(iconSize)
                            .offset(y = 2.dp)
                        if (isSelected) GradientIcon(icon, activeBrush, modifier = mod)
                        else Icon(icon, null, tint = inactiveIconColor, modifier = mod)
                    }

                    @Composable
                    fun Item(tab: CollabTab) {
                        // Considera seleccionado si cualquier destino en la jerarquía coincide con la ruta raíz del tab.
                        val isSel = currentDestination
                            ?.hierarchy
                            ?.any { dest -> dest.route == tab.rootRoute } == true

                        NavigationBarItem(
                            selected = isSel,
                            onClick = { nav.navigateToTabRoot(tab.rootRoute) },
                            icon = { IconC(tab.icon, isSel) },
                            label = { Label(tab.label, isSel) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                            modifier = Modifier.padding(vertical = 0.dp)
                        )
                    }

                    // Lado izquierdo
                    Item(CollabTab.Menu)
                    Item(CollabTab.Stats)

                    // Espacio para el botón central
                    Spacer(modifier = Modifier.weight(1f))

                    // Lado derecho
                    Item(CollabTab.Promotions)
                    Item(CollabTab.Profile)
                }

            // Botón central (QR)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 4.dp)
                    .size(62.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(activeBrush)
                    .clickable {
                        // Si el lector QR es pantalla aparte, evita apilar duplicados:
                        // Only navigate if we have a valid branchId
                        branchId?.let { id ->
                            nav.navigate(Screens.QrScanner.createRoute(id)) {
                                launchSingleTop = true
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DocumentScanner,
                    contentDescription = "Escanear",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (bottomInset > 0.dp) {
            Spacer(Modifier.height(bottomInset))
        }
    }
}
