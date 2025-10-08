package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
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
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

// ---- Modelo de datos ----
enum class HistoryType { CUPON_USADO, CUPON_GUARDADO, FAVORITO_AGREGADO }

data class HistoryEntry(
    val type: HistoryType,
    val title: String,
    val subtitle: String,
    val date: String
)

// ---- Pantalla ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(nav: NavHostController, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(BJTab.Perfil) }

    val entries = remember {
        listOf(
            HistoryEntry(HistoryType.CUPON_USADO, "Cupón Usado", "2×1 en Pizzas en La Bella Italia", "15 Oct 2024"),
            HistoryEntry(HistoryType.CUPON_GUARDADO, "Cupón Guardado", "Envío Gratis en Tortas el León", "01 Oct 2024"),
            HistoryEntry(HistoryType.FAVORITO_AGREGADO, "Favorito Agregado", "Tortas el León", "20 Sep 2024"),
            HistoryEntry(HistoryType.CUPON_USADO, "Cupón Usado", "20% de Descuento en Café Tacuba", "15 Sep 2024"),
            HistoryEntry(HistoryType.CUPON_USADO, "Cupón Usado", "2×1 en Pizzas en La Bella Italia", "12 Sep 2024"),
            HistoryEntry(HistoryType.CUPON_GUARDADO, "Cupón Guardado", "Envío Gratis en Café Tacuba", "01 Sep 2024"),
            HistoryEntry(HistoryType.FAVORITO_AGREGADO, "Favorito Agregado", "La Bella Italia", "20 Ago 2024")
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                // Logo centrado arriba
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

                // Fila con back, título y campana
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = "Volver",
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF616161)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Historial",
                            fontWeight = FontWeight.Black,
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

                // Divisor gradiente
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
                        BJTab.Menu      -> nav.navigate(Screens.Home.route)
                        BJTab.Cupones   -> { /* nav.navigate(...) */ }
                        BJTab.Favoritos -> { /* nav.navigate(...) */ }
                        BJTab.Perfil    -> Unit
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
                items(entries) { item ->
                    HistoryCard(
                        entry = item,
                        onClick = { /* TODO: Navega al detalle */ }
                    )
                }

                // Versión al final del scroll
                item {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Versión 1.0.01",
                            color = Color(0xFFAEAEAE),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ---- Item / Tarjeta ----
@Composable
private fun HistoryCard(
    entry: HistoryEntry,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = SolidColor(Color(0xFFD3D3D3))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---- Icono dinámico con gradiente ----
            val iconVector = when (entry.type) {
                HistoryType.FAVORITO_AGREGADO -> Icons.Outlined.FavoriteBorder
                HistoryType.CUPON_USADO,
                HistoryType.CUPON_GUARDADO -> Icons.Outlined.ConfirmationNumber
            }

            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithCache {
                        val gradient = Brush.linearGradient(
                            listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush = gradient, blendMode = BlendMode.SrcAtop)
                        }
                    }
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF616161)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.subtitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFFAEAEAE)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = entry.date,
                fontSize = 10.sp,
                color = Color(0xFFAEAEAE)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        History(nav = nav)
    }
}
