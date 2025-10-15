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
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

/**
 * Representa una pregunta frecuente con su respuesta correspondiente.
 * @param question Pregunta planteada por el usuario.
 * @param answer Respuesta explicativa mostrada en la interfaz.
 */
data class FaqItem(val question: String, val answer: String)

/**
 * Pantalla de ayuda y soporte que muestra contacto, línea de ayuda y preguntas frecuentes.
 * Incluye barra de navegación inferior y divisor superior con gradiente.
 * @param nav Controlador de navegación para cambiar de pantalla.
 * @param modifier Modificador opcional para ajustar el contenedor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Help(nav: NavHostController, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(BJTab.Profile) }
    val appVersion = "1.0.01"

    val faqs = remember {
        listOf(
            FaqItem("¿Cómo uso mis cupones?", "Ve a la sección de cupones y selecciona el que quieras usar. Muestra el código al comerciante."),
            FaqItem("¿Los cupones tienen fecha de vencimiento?", "Sí, cada cupón tiene una fecha límite que aparece en los detalles del mismo."),
            FaqItem("¿Cómo actualizo mi información personal?", "Ve a Perfil > Editar Perfil para actualizar tus datos personales."),
            FaqItem("¿Puedo compartir mis cupones?", "Los cupones son personales e intransferibles, vinculados a tu cuenta.")
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            BJTopHeader(
                title = "Ayuda y Soporte",
                nav = nav
            )
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
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                items(faqs) { item -> FaqCard(item) }

                // Versión al final
                item {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Versión $appVersion",
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

/**
 * Ítem de acción para contacto o soporte con ícono, texto y chevron lateral.
 * @param icon Icono representativo de la acción.
 * @param title Título principal del ítem.
 * @param subtitle Subtítulo con información de contacto.
 * @param onClick Acción ejecutada al presionar el ítem.
 */
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

/**
 * Ícono con gradiente aplicado mediante mezcla de color.
 * @param imageVector Ícono base a renderizar.
 * @param brush Degradado aplicado al ícono.
 * @param modifier Modificador opcional para ajustar tamaño o posición.
 * @param contentDescription Descripción accesible del ícono.
 */
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

/**
 * Tarjeta que muestra una pregunta frecuente con su respuesta.
 * @param item Elemento FAQ con pregunta y respuesta.
 */
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

/**
 * Vista previa de la pantalla de ayuda y soporte.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HelpPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Help(nav = nav)
    }
}
