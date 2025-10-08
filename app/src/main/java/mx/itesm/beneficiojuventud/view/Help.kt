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
import androidx.compose.ui.graphics.vector.ImageVector
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

data class FaqItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Help(nav: NavHostController, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(BJTab.Perfil) }
    val appVersion = "1.0.01"

    val faqs = remember {
        listOf(
            FaqItem(
                "¿Cómo uso mis cupones?",
                "Ve a la sección de cupones y selecciona el que quieras usar. Muestra el código al comerciante."
            ),
            FaqItem(
                "¿Los cupones tienen fecha de vencimiento?",
                "Sí, cada cupón tiene una fecha límite que aparece en los detalles del mismo."
            ),
            FaqItem(
                "¿Cómo actualizo mi información personal?",
                "Ve a Perfil > Editar Perfil para actualizar tus datos personales."
            ),
            FaqItem(
                "¿Puedo compartir mis cupones?",
                "Los cupones son personales e intransferibles, vinculados a tu cuenta."
            ),
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
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = "Volver",
                                tint = Color(0xFF616161),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Ayuda y Soporte",
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
                // Contactar soporte
                item {
                    HelpActionItem(
                        icon = Icons.Outlined.Email,
                        title = "Contactar Soporte",
                        subtitle = "soporte@beneficio.com",
                        onClick = { /* TODO: abrir Intent.ACTION_SENDTO mailto: */ }
                    )
                }
                // Línea de ayuda
                item {
                    HelpActionItem(
                        icon = Icons.Outlined.PhoneInTalk,
                        title = "Línea de Ayuda",
                        subtitle = "+52 55 0987 6543",
                        onClick = { /* TODO: abrir Intent.ACTION_DIAL tel: */ }
                    )
                }

                // Título de FAQ
                item {
                    Text(
                        text = "Preguntas Frecuentes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF616161),
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }

                // Lista de FAQs
                items(faqs) { item ->
                    FaqCard(item)
                }

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

/** Ítem con icono con gradiente y chevron (correo / teléfono) */
@Composable
private fun HelpActionItem(
    icon: ImageVector,
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
            brush = SolidColor(Color(0xFFD3D3D3))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GradientIcon(
                imageVector = icon,
                brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(8.dp))
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

/** Ícono con gradiente */
@Composable
fun GradientIcon(
    imageVector: ImageVector,
    brush: Brush,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = Color.Unspecified,
        modifier = modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
                }
            }
    )
}

/** Tarjeta de FAQ */
@Composable
private fun FaqCard(item: FaqItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = SolidColor(Color(0xFFD3D3D3))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = item.question,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF616161)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.answer,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color(0xFFAEAEAE)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HelpPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Help(nav = nav)
    }
}
