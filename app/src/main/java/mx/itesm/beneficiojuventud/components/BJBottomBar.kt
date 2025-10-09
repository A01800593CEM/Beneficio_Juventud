package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.view.GradientIcon

enum class BJTab { Home, Coupons, Favorites, Profile }

/** ---- BottomBar que NO crece de alto, solo se "sube" si hay gesto/botones del sistema ---- */
@Composable
fun BJBottomBar(
    selected: BJTab,
    onSelect: (BJTab) -> Unit,
    containerColor: Color = Color(0xFFF6F6F6),
    activeBrush: Brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
    inactiveIconColor: Color = Color(0xFF616161),
    inactiveTextColor: Color = Color(0xFF616161),
    iconSize: Dp = 28.dp
) {
    val labelBase = MaterialTheme.typography.labelSmall

    // Altura real del inset inferior (pill/botones); 0dp en equipos que no la muestran
    val bottomInset = WindowInsets.navigationBars
        .only(WindowInsetsSides.Bottom)
        .asPaddingValues()
        .calculateBottomPadding()

    Column(Modifier.fillMaxWidth()) {
        NavigationBar(
            containerColor = containerColor,
            tonalElevation = 0.dp,
            // No dejamos que NavigationBar cambie su alto por insets
            windowInsets = WindowInsets(0),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // ← altura fija (no se hace "más gorda")
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
            fun Item(tab: BJTab, icon: ImageVector, labelText: String) {
                NavigationBarItem(
                    selected = selected == tab,
                    onClick = { onSelect(tab) },
                    icon = { IconC(icon, selected == tab) },
                    label = { Label(labelText, selected == tab) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                    modifier = Modifier.padding(vertical = 0.dp)
                )
            }

            Item(BJTab.Home, Icons.Outlined.Home, "Menú")
            Item(BJTab.Coupons, Icons.Outlined.QrCode, "Cupones")
            Item(BJTab.Favorites, Icons.Outlined.FavoriteBorder, "Favoritos")
            Item(BJTab.Profile, Icons.Outlined.Person, "Perfil")
        }

        // Este espacio SOLO aparece si hay inset inferior; empuja la barra hacia arriba.
        if (bottomInset > 0.dp) {
            Spacer(Modifier.height(bottomInset))
        }
    }
}

/** Texto con gradiente (mismo que venías usando) */
@Composable
fun GradientText(
    text: String,
    brush: Brush,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
    )
) {
    val annotated = buildAnnotatedString {
        withStyle(
            SpanStyle(
                brush = brush,
                fontSize = style.fontSize,
                fontWeight = style.fontWeight,
                fontFamily = style.fontFamily,
                letterSpacing = style.letterSpacing
            )
        ) { append(text) }
    }
    Text(text = annotated, modifier = modifier, style = style.copy(color = Color.Unspecified))
}

/** ---- Ejemplo de uso en Scaffold sin duplicar el inset inferior ---- */
@Composable
fun DemoScreen(
    selected: BJTab = BJTab.Home,
    onSelect: (BJTab) -> Unit = {}
) {
    Scaffold(
        // Respetamos solo TOP + HORIZONTAL; el BOTTOM lo maneja BJBottomBar
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        bottomBar = {
            BJBottomBar(selected = selected, onSelect = onSelect)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // …tu contenido…
            Spacer(Modifier.height(24.dp))
            Text(
                "Contenido de ejemplo",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BJBottomBarPreview() {
    MaterialTheme { DemoScreen() }
}
