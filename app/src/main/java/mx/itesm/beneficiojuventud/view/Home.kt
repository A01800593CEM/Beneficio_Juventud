package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.model.MerchantCardData
import mx.itesm.beneficiojuventud.model.Promo
import mx.itesm.beneficiojuventud.model.PromoTheme
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

private data class Category(val icon: ImageVector, val label: String)

private val popularCategories = listOf(
    Category(Icons.Outlined.Fastfood, "Alimentos"),
    Category(Icons.Outlined.MusicNote, "Música"),
    Category(Icons.Outlined.SportsEsports, "Deportes"),
    Category(Icons.Outlined.Movie, "Cine y Ocio"),
    Category(Icons.Outlined.Storefront, "Moda"),
    Category(Icons.Outlined.Edit, "Papelería")
)

// 🔹 Cada promo ahora puede definir su propio theme
private val promos = listOf(
    Promo(
        R.drawable.el_fuego_sagrado,
        "Jueves Pozolero",
        "El Sazón de Iván",
        "2×1 en todos nuestros pozoles.",
        theme = PromoTheme.LIGHT // 🌕 letras blancas + fondo gris oscuro
    ),
    Promo(
        R.drawable.carne,
        "Lunes sin Carne",
        "Bocado Rápido",
        "20% en bowls vegetarianos.",
        theme = PromoTheme.DARK  // 🌑 letras negras + fondo blanco
    )
)


private val specialOffers = listOf(
    MerchantCardData("Fuego Lento & Brasa", "Asador • Parrilla", 4.7),
    MerchantCardData("Bocado Rápido", "Comida rápida", 4.6),
    MerchantCardData("Pastas Nonna", "Italiano", 4.8),
    MerchantCardData("Café Norte", "Cafetería", 4.5)
)

private val newOffers = listOf(
    MerchantCardData("Fuego Lento & Brasa", "Asador • Parrilla", 4.7),
    MerchantCardData("Bocado Rápido", "Comida rápida", 4.6),
    MerchantCardData("Panadería Luz", "Pan dulce", 4.9),
    MerchantCardData("VeggieGo", "Saludable", 4.4)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(nav: NavHostController, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(BJTab.Home) }
    var search by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD7F2F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color(0xFF008D96)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Hola, Iván",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color(0xFF4B4C7E)
                            )
                            Text(
                                "¿Listo para ahorrar?",
                                fontSize = 11.sp,
                                color = Color(0xFF8C8C8C)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Notificaciones",
                        tint = Color(0xFF008D96),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))
                BJSearchBar(
                    query = search,
                    onQueryChange = { search = it },
                    onSearch = { /* TODO: ejecutar búsqueda */ },
                    modifier = Modifier.fillMaxWidth()
                )

                GradientDivider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        },
        bottomBar = {
            BJBottomBar(
                selected = selectedTab,
                onSelect = { tab ->
                    selectedTab = tab
                    when (tab) {
                        BJTab.Home      -> Unit
                        BJTab.Coupons   -> { /* nav.navigate(...) */ }
                        BJTab.Favorites  -> { /*nav.navigate(Screens.Favorites.route) */}
                        BJTab.Profile    -> { nav.navigate(Screens.Profile.route) }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Categorías
            item {
                SectionTitle(
                    "Categorías Populares",
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    popularCategories.forEach { CategoryPill(icon = it.icon, label = it.label) }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Recomendado
            item {
                SectionTitle(
                    "Recomendado para ti",
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                PromoCarousel(
                    promos = promos,
                    modifier = Modifier.height(130.dp)
                )
            }

            // Ofertas
            item {
                SectionTitle(
                    "Ofertas Especiales",
                    Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp)
                )
                MerchantRow(data = specialOffers)
            }

            // Lo nuevo
            item {
                SectionTitle(
                    "Lo Nuevo",
                    Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 6.dp)
                )
                MerchantRow(data = newOffers)
            }

            // Pie
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Versión 1.0.01", color = Color(0xFFAEAEAE), fontSize = 10.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomePreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Home(nav = nav)
    }
}
