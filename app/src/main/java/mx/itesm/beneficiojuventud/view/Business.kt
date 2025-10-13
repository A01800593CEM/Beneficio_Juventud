package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.model.Promo
import mx.itesm.beneficiojuventud.model.PromoTheme
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

// --------- Mock data ----------

/**
 * Información básica del negocio mostrada en la pantalla de detalle.
 * @param imageRes Recurso drawable de la imagen del negocio.
 * @param name Nombre comercial del negocio.
 * @param category Categoría a la que pertenece.
 * @param location Ubicación breve o sucursal.
 * @param rating Calificación promedio mostrada al usuario.
 * @param isFavorite Indica si el negocio está marcado como favorito.
 */
data class BusinessInfo(
    val imageRes: Int,
    val name: String,
    val category: String,
    val location: String,
    val rating: Double,
    val isFavorite: Boolean = false
)

private val mockBusiness = BusinessInfo(
    imageRes = R.drawable.el_fuego_sagrado,
    name = "Fuego Lento & Brasa",
    category = "Alimentos",
    location = "Zona Rosa, Local 45",
    rating = 4.7
)

private val businessCoupons = listOf(
    Promo(
        bg = R.drawable.el_fuego_sagrado,
        title = "Martes 2×1",
        subtitle = "Cine Stelar",
        body = "Compra un boleto y obtén el segundo gratis para la misma función.",
        theme = PromoTheme.DARK
    ),
    Promo(
        bg = R.drawable.el_fuego_sagrado,
        title = "Martes 2×1",
        subtitle = "Cine Stelar",
        body = "Compra un boleto y obtén el segundo gratis para la misma función.",
        theme = PromoTheme.DARK
    )
)

// --------- Pantalla ----------

/**
 * Pantalla de detalle de un negocio con hero, datos básicos y cupones disponibles.
 * @param nav Controlador de navegación para manejar back y tabs.
 * @param modifier Modificador externo del contenedor.
 * @param business Datos del negocio a mostrar; por defecto usa mock.
 * @param coupons Lista de cupones asociados al negocio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Business(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    business: BusinessInfo = mockBusiness,
    coupons: List<Promo> = businessCoupons
) {
    var selectedTab by remember { mutableStateOf(BJTab.Home) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // 🔧 Evita doble padding de status bar (BJTopHeader ya aplica safeDrawing Top+Horizontal)
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            BJTopHeader(
                title = business.name,
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { BusinessHeroCard(business = business) }

            item {
                SectionTitle(
                    text = "${coupons.size} Cupones Disponibles",
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            items(coupons.size) { i ->
                PromoImageBanner(
                    promo = coupons[i],
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Versión 1.0.01", color = Color(0xFFAEAEAE), fontSize = 10.sp)
                }
            }
        }
    }
}

// --------- UI helpers ----------

/**
 * Tarjeta de cabecera con imagen, gradiente y datos clave del negocio.
 * @param business Información del negocio a renderizar.
 * @param modifier Modificador opcional del contenedor.
 */
@Composable
private fun BusinessHeroCard(
    business: BusinessInfo,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(shape)
    ) {
        Image(
            painter = painterResource(id = business.imageRes),
            contentDescription = business.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val brush = Brush.horizontalGradient(
                        0.00f to Color(0xFF2B2B2B).copy(alpha = 1f),
                        0.15f to Color(0xFF2B2B2B).copy(alpha = .95f),
                        0.35f to Color(0xFF2B2B2B).copy(alpha = .65f),
                        0.55f to Color(0xFF2B2B2B).copy(alpha = .35f),
                        0.75f to Color.Transparent,
                        1.00f to Color.Transparent
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush)
                    }
                }
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    business.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    business.category,
                    color = Color(0xFFD3D3D3),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    business.location,
                    color = Color(0xFFC3C3C3),
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD900),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", business.rating),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Guardar",
                        tint = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Vista previa de la pantalla de negocio con datos mock.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BusinessPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Business(nav = nav)
    }
}
