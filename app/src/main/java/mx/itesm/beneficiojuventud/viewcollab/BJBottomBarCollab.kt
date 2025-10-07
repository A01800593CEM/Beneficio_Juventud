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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val GradientBrush = Brush.horizontalGradient(listOf(DarkBlue, Teal))

@Composable
fun BJBottomBarCollab(
    selected: BJTabCollab,
    onSelect: (BJTabCollab) -> Unit,
    onAddClick: () -> Unit
) {
    val tabs = listOf(BJTabCollab.Menu, BJTabCollab.Stats, BJTabCollab.Promotions, BJTabCollab.Profile)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            modifier = Modifier.height(60.dp),
            containerColor = Color.White,
            tonalElevation = 8.dp
        ) {
            NavigationBarItem(
                selected = selected == tabs[0],
                onClick = { onSelect(tabs[0]) },
                icon = { Icon(tabs[0].icon, contentDescription = tabs[0].label) },
                label = { Text(tabs[0].label, fontSize = 10.sp) },
                colors = navigationBarItemColors()
            )
            NavigationBarItem(
                selected = selected == tabs[1],
                onClick = { onSelect(tabs[1]) },
                icon = { Icon(tabs[1].icon, contentDescription = tabs[1].label) },
                label = { Text(tabs[1].label, fontSize = 10.sp) },
                colors = navigationBarItemColors()
            )
            Spacer(modifier = Modifier.weight(1f))
            NavigationBarItem(
                selected = selected == tabs[2],
                onClick = { onSelect(tabs[2]) },
                icon = { Icon(tabs[2].icon, contentDescription = tabs[2].label) },
                label = { Text(tabs[2].label, fontSize = 10.sp) },
                colors = navigationBarItemColors()
            )
            NavigationBarItem(
                selected = selected == tabs[3],
                onClick = { onSelect(tabs[3]) },
                icon = { Icon(tabs[3].icon, contentDescription = tabs[3].label) },
                label = { Text(tabs[3].label, fontSize = 10.sp) },
                colors = navigationBarItemColors()
            )
        }

        Box(
            modifier = Modifier
                .offset(y = (-10).dp)
                .size(52.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(GradientBrush)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Teal,
    unselectedIconColor = Color(0xFF969696),
    selectedTextColor = Teal,
    unselectedTextColor = Color(0xFF969696),
    indicatorColor = Color.Transparent
)