package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.PromoImageBanner
import mx.itesm.beneficiojuventud.components.SectionTitle
import mx.itesm.beneficiojuventud.model.Promo
import mx.itesm.beneficiojuventud.model.PromoTheme
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

/// ---- Mock data ----

/**
 * Modelo de negocio favorito mostrado en la lista.
 * @param imageRes Recurso drawable de la imagen del negocio.
 * @param name Nombre comercial del negocio.
 * @param category Categoría a la que pertenece.
 * @param location Ubicación resumida para mostrar en tarjeta.
 * @param rating Calificación promedio mostrada con estrella.
 * @param isFavorite Indica si está marcado como favorito.
 */
data class FavoriteMerchant(
    val imageRes: Int,
    val name: String,
    val category: String,
    val location: String,
    val rating: Double,
    val isFavorite: Boolean = true
)

/**
 * Datos de ejemplo para la sección de negocios favoritos.
 */
private val sampleFavorites = listOf(
    FavoriteMerchant(R.drawable.el_fuego_sagrado, "Fuego Lento & Brasa", "Alimentos", "Zona Rosa, Local 45", 4.7),
    FavoriteMerchant(R.drawable.el_fuego_sagrado, "Fuego Lento & Brasa", "Alimentos", "Zona Rosa, Local 45", 4.7),
    FavoriteMerchant(R.drawable.el_fuego_sagrado, "Fuego Lento & Brasa", "Alimentos", "Zona Rosa, Local 45", 4.7),
)

/**
 * Modo de visualización de la pantalla de favoritos.
 */
private enum class FavoriteMode { Coupons, Businesses }

/**
 * Datos de ejemplo para la sección de cupones favoritos.
 */
private val samplePromos = listOf(
    Promo(
        bg = R.drawable.bolos,
        title = "Martes 2×1",
        subtitle = "Cine Stelar",
        body = "Compra un boleto y obtén el segundo gratis para la misma función.",
        theme = PromoTheme.LIGHT
    ),
    Promo(
        bg = R.drawable.bolos,
        title = "Miércoles de Palomitas",
        subtitle = "Cine Stelar",
        body = "Palomitas tamaño grande al precio de chicas.",
        theme = PromoTheme.DARK
    ),
    Promo(
        bg = R.drawable.bolos,
        title = "2×1 en Boliche",
        subtitle = "Strike Center",
        body = "Aplica de 5–8 pm. No acumulable con otras promos.",
        theme = PromoTheme.LIGHT
    )
)

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Pantalla de Favoritos con pestaña inferior, barra superior y contenido en lista.
 * Permite alternar entre cupones guardados y negocios favoritos mediante una píldora de selección.
 * @param nav Controlador de navegación para cambio de pantallas.
 * @param modifier Modificador externo para el contenedor de la pantalla.
 * @param favorites Lista de negocios favoritos a renderizar en modo Businesses.
 * @param promos Lista de cupones favoritos a renderizar en modo Coupons.
 */
@Composable
fun Favorites(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    favorites: List<FavoriteMerchant> = sampleFavorites,
    promos: List<Promo> = samplePromos
) {
    var selectedTab by remember { mutableStateOf(BJTab.Favorites) }
    var mode by remember { mutableStateOf(FavoriteMode.Coupons) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                // Logo centrado (igual a History)
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
                        BackButton(
                            nav = nav,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Favoritos",
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

                GradientDivider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // --- Toggle Mis Cupones / Negocios (píldora) ---
                TogglePill(
                    left = "Mis Cupones",
                    right = "Negocios",
                    selectedLeft = (mode == FavoriteMode.Coupons),
                    onSelectLeft = { mode = FavoriteMode.Coupons },
                    onSelectRight = { mode = FavoriteMode.Businesses },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }
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
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)
        ) {
            // Título con conteo
            item {
                val count = when (mode) {
                    FavoriteMode.Businesses -> favorites.size
                    FavoriteMode.Coupons    -> promos.size
                }
                val label = if (mode == FavoriteMode.Businesses)
                    "$count Negocios Guardados" else "$count Cupones Guardados"

                SectionTitle(label, Modifier.padding(top = 6.dp, bottom = 8.dp))
            }

            // Contenido según modo
            when (mode) {
                FavoriteMode.Businesses -> {
                    items(favorites) { merchant ->
                        FavoriteCard(
                            merchant = merchant,
                            onClick = { /* TODO: nav a detalle */ },
                            onToggleFavorite = { /* TODO */ },
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
                FavoriteMode.Coupons -> {
                    items(promos.size) { i ->
                        PromoImageBanner(
                            promo = promos[i],
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }

            // Footer versión
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Versión 1.0.01", color = Color(0xFFAEAEAE), fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * Control con forma de píldora para alternar entre dos opciones exclusivas.
 * Usa inversión de color para indicar la opción activa.
 * @param left Etiqueta de la opción izquierda.
 * @param right Etiqueta de la opción derecha.
 * @param selectedLeft Indica si la opción izquierda está activa.
 * @param onSelectLeft Acción al seleccionar la opción izquierda.
 * @param onSelectRight Acción al seleccionar la opción derecha.
 * @param modifier Modificador externo del componente.
 */
@Composable
private fun TogglePill(
    left: String,
    right: String,
    selectedLeft: Boolean,
    onSelectLeft: () -> Unit,
    onSelectRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color(0xFFD3D3D3)

    Surface(
        color = bg,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.height(40.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Left (invertido: seleccionado -> blanco)
            Surface(
                color = if (selectedLeft) Color(0xFFFFFFFF) else bg,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = if (selectedLeft) 1.dp else 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(1.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onSelectLeft() }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        left,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF616161)
                    )
                }
            }
            // Right (invertido: seleccionado -> blanco)
            Surface(
                color = if (!selectedLeft) Color(0xFFFFFFFF) else bg,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = if (!selectedLeft) 1.dp else 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(1.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onSelectRight() }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        right,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF616161)
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta para mostrar un negocio favorito con imagen, datos básicos, rating y acción de favorito.
 * @param merchant Modelo con la información del negocio.
 * @param onClick Acción al pulsar la tarjeta completa.
 * @param onToggleFavorite Acción al pulsar el ícono de favorito.
 * @param modifier Modificador externo de la tarjeta.
 */
@Composable
private fun FavoriteCard(
    merchant: FavoriteMerchant,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = SolidColor(Color(0xFFD3D3D3))
        ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = merchant.imageRes),
                contentDescription = merchant.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    merchant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF616161),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    fontWeight = FontWeight.Bold,
                    text = merchant.category,
                    color = Color(0xFF969696),
                    fontSize = 12.sp
                )
                Text(
                    text = merchant.location,
                    color = Color(0xFF8C8C8C),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD900),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", merchant.rating),
                        color = Color(0xFF616161),
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Outlined.Favorite,
                    contentDescription = "Quitar de favoritos",
                    tint = Color(0xFFE53935)
                )
            }
        }
    }
}

/**
 * Vista previa de la pantalla de favoritos con el tema de la app.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FavoritePreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Favorites(nav = nav)
    }
}
