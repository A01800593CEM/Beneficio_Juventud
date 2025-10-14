package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.components.GradientIcon
import mx.itesm.beneficiojuventud.components.GradientText

/**
 * Menú inferior para el Colaborador.
 * Utiliza BJTabCollab para construir dinámicamente sus elementos.
 */
@Composable
fun BJBottomBarCollab(
    selected: BJTabCollab,
    onSelect: (BJTabCollab) -> Unit,
    onAddClick: () -> Unit,
    containerColor: Color = Color(0xFFF6F6F6),
    activeBrush: Brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
    inactiveIconColor: Color = Color(0xFF616161),
    inactiveTextColor: Color = Color(0xFF616161),
    iconSize: androidx.compose.ui.unit.Dp = 28.dp
) {
    val tabs = listOf(BJTabCollab.Menu, BJTabCollab.Stats, BJTabCollab.Promotions, BJTabCollab.Profile)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // El resto del contenido se anidará dentro de este Box con padding.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            NavigationBar(
                modifier = Modifier.height(68.dp),
                containerColor = containerColor,
            ) {
                @Composable
                fun iconContent(icon: ImageVector, isSelected: Boolean) {
                    val mod = Modifier.size(iconSize)
                    if (isSelected)
                        GradientIcon(icon, activeBrush, modifier = mod)
                    else
                        Icon(icon, null, tint = inactiveIconColor, modifier = mod)
                }

                @Composable
                fun labelContent(text: String, isSelected: Boolean) {
                    val mod = Modifier.offset(y = (-6).dp)
                    if (isSelected)
                        GradientText(text, activeBrush, mod)
                    else
                        Text(text, color = inactiveTextColor, modifier = mod, fontSize = 10.sp)
                }

                // Ítem de Menú
                NavigationBarItem(
                    selected = selected == tabs[0],
                    onClick = { onSelect(tabs[0]) },
                    icon = { iconContent(tabs[0].icon, selected == tabs[0]) },
                    label = { labelContent(tabs[0].label, selected == tabs[0]) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )

                // Ítem de Estadísticas
                NavigationBarItem(
                    selected = selected == tabs[1],
                    onClick = { onSelect(tabs[1]) },
                    icon = { iconContent(tabs[1].icon, selected == tabs[1]) },
                    label = { labelContent(tabs[1].label, selected == tabs[1]) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Ítem de Promociones
                NavigationBarItem(
                    selected = selected == tabs[2],
                    onClick = { onSelect(tabs[2]) },
                    icon = { iconContent(tabs[2].icon, selected == tabs[2]) },
                    label = { labelContent(tabs[2].label, selected == tabs[2]) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )

                // Ítem de Perfil
                NavigationBarItem(
                    selected = selected == tabs[3],
                    onClick = { onSelect(tabs[3]) },
                    icon = { iconContent(tabs[3].icon, selected == tabs[3]) },
                    label = { labelContent(tabs[3].label, selected == tabs[3]) },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }

            // Botón central flotante
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 4.dp)
                    .size(62.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(activeBrush)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BJBottomBarCollabPreview() {
    MaterialTheme {
        BJBottomBarCollab(selected = BJTabCollab.Menu, onSelect = {}, onAddClick = {})
    }
}