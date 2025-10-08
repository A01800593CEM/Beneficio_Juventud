package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Tabs del proyecto */
enum class BJTab { Home, Coupons, Favorites, Profile }

/** Icono con gradiente */
@Composable
fun GradientIcon(
    imageVector: ImageVector,
    brush: Brush,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = modifier.drawWithCache {
            onDrawWithContent {
                drawIntoCanvas { canvas ->
                    val bounds = Rect(0f, 0f, size.width, size.height)
                    canvas.saveLayer(bounds, Paint())
                    drawContent()
                    drawRect(brush = brush, blendMode = BlendMode.SrcIn)
                    canvas.restore()
                }
            }
        }
    )
}

/** Texto con gradiente */
@Composable
fun GradientText(
    text: String,
    brush: Brush,
    modifier: Modifier = Modifier,
) {
    val base = MaterialTheme.typography.labelSmall
    Text(
        text = text,
        style = base.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color.White
        ),
        modifier = modifier.drawWithCache {
            onDrawWithContent {
                drawIntoCanvas { canvas ->
                    val bounds = Rect(0f, 0f, size.width, size.height)
                    canvas.saveLayer(bounds, Paint())
                    drawContent()
                    drawRect(brush = brush, blendMode = BlendMode.SrcIn)
                    canvas.restore()
                }
            }
        }
    )
}

/** Menú inferior */
@Composable
fun BJBottomBar(
    selected: BJTab,
    onSelect: (BJTab) -> Unit,
    containerColor: Color = Color(0xFFF6F6F6),
    activeBrush: Brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
    inactiveIconColor: Color = Color(0xFF616161),
    inactiveTextColor: Color = Color(0xFF616161),
    iconSize: androidx.compose.ui.unit.Dp = 28.dp
) {
    val labelBase = MaterialTheme.typography.labelSmall

    NavigationBar(containerColor = containerColor) {

        @Composable
        fun label(text: String, isSelected: Boolean) =
            if (isSelected)
                GradientText(
                    text,
                    activeBrush,
                    modifier = Modifier.offset(y = (-6).dp) // 👈 sube el texto
                )
            else
                Text(
                    text = text,
                    style = labelBase.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = inactiveTextColor,
                    modifier = Modifier.offset(y = (-6).dp) // 👈 sube el texto también
                )

        @Composable
        fun iconContent(
            icon: ImageVector,
            isSelected: Boolean
        ) {
            val mod = Modifier.size(iconSize)
            if (isSelected)
                GradientIcon(icon, activeBrush, modifier = mod)
            else
                Icon(icon, null, tint = inactiveIconColor, modifier = mod)
        }

        // Ítems del menú
        NavigationBarItem(
            selected = selected == BJTab.Home,
            onClick = { onSelect(BJTab.Home) },
            icon = { iconContent(Icons.Outlined.Home, selected == BJTab.Home) },
            label = { label("Menú", selected == BJTab.Home) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )

        NavigationBarItem(
            selected = selected == BJTab.Coupons,
            onClick = { onSelect(BJTab.Coupons) },
            icon = { iconContent(Icons.Outlined.QrCode, selected == BJTab.Coupons) },
            label = { label("Cupones", selected == BJTab.Coupons) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )

        NavigationBarItem(
            selected = selected == BJTab.Favorites,
            onClick = { onSelect(BJTab.Favorites) },
            icon = { iconContent(Icons.Outlined.FavoriteBorder, selected == BJTab.Favorites) },
            label = { label("Favoritos", selected == BJTab.Favorites) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )

        NavigationBarItem(
            selected = selected == BJTab.Profile,
            onClick = { onSelect(BJTab.Profile) },
            icon = { iconContent(Icons.Outlined.Person, selected == BJTab.Profile) },
            label = { label("Perfil", selected == BJTab.Profile) },
            alwaysShowLabel = true,
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BJBottomBarPreview() {
    MaterialTheme {
        BJBottomBar(selected = BJTab.Home, onSelect = {})
    }
}
