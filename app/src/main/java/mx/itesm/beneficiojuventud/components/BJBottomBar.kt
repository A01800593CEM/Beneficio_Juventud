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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.view.GradientIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

enum class BJTab { Home, Coupons, Favorites, Profile }

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

    NavigationBar(
        containerColor = containerColor,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0),
        modifier = Modifier.height(56.dp)
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
                .size(iconSize)          // ← mismo tamaño
                .offset(y = 2.dp)        // ↓ baja un poco el icono para cerrar el gap
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
}


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
        ) {
            append(text)
        }
    }
    Text(text = annotated, modifier = modifier, style = style.copy(color = androidx.compose.ui.graphics.Color.Unspecified))
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BJBottomBarPreview() {
    MaterialTheme {
        BJBottomBar(selected = BJTab.Home, onSelect = {})
    }
}
