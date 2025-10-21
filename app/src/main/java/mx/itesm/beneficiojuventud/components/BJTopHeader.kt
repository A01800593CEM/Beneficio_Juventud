package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.R

// Insets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

/**
 * Header superior reutilizable.
 * - Respeta safe drawing (Top + Horizontal) para notch/estatus bar.
 * - No aplica padding inferior (lo maneja BJBottomBar).
 * - Cambia el título con el parámetro [title].
 */
@Composable
fun BJTopHeader(
    title: String,
    nav: NavHostController,
    modifier: Modifier = Modifier,
    showNotificationsIcon: Boolean = false
) {
    Column(
        modifier
            .fillMaxWidth()
            // 👉 Solo Top + Horizontal
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        // Logo centrado
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                // Usa tu componente existente
                BackButton(nav = nav)

                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF616161)
                )
            }

            if (showNotificationsIcon) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notificaciones",
                    tint = Color(0xFF008D96),
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        GradientDivider(
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BJTopHeaderPreview() {
    val nav = rememberNavController()
    Box(Modifier.fillMaxSize()) {
        BJTopHeader(
            title = "Promociones",
            nav = nav,
            showNotificationsIcon = true
        )
    }
}
