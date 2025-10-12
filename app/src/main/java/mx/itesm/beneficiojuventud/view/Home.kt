package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.utils.MerchantCardData
import mx.itesm.beneficiojuventud.model.Promo
import mx.itesm.beneficiojuventud.model.PromoTheme
import mx.itesm.beneficiojuventud.model.popularCategories
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

// 🔹 Demo data
private val promos = listOf(
    Promo(
        R.drawable.el_fuego_sagrado,
        "Jueves Pozolero",
        "El Sazón de Iván",
        "2×1 en todos nuestros pozoles.",
        theme = PromoTheme.LIGHT
    ),
    Promo(
        R.drawable.carne,
        "Lunes sin Carne",
        "Bocado Rápido",
        "20% en bowls vegetarianos.",
        theme = PromoTheme.DARK
    ),
    Promo(
        R.drawable.bolos,
        "Tarde de Café",
        "Café Norte",
        "2×1 en capuchinos de 4 a 6 pm.",
        theme = PromoTheme.LIGHT
    ),
    Promo(
        R.drawable.el_fuego_sagrado,
        "Martes 2×1",
        "Cine Stelar",
        "Compra un boleto y obtén el segundo gratis para la misma función.",
        theme = PromoTheme.DARK
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

    // 🔸 NUEVO: categoría seleccionada para filtrar (null = sin filtro)
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    // 🔸 NUEVO: filtra los promos según la categoría elegida
    val filteredPromos = remember(selectedCategory) {
        if (selectedCategory.isNullOrBlank()) promos
        else promos.filter { matchesCategory(it, selectedCategory!!) }
    }

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
                    onSearch = {
                        // TODO: búsqueda global
                    },
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
                    // 🔸 NUEVO: click para filtrar; click de nuevo para quitar filtro
                    popularCategories.forEach { cat ->
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .clickable {
                                    selectedCategory =
                                        if (selectedCategory == cat.label) null else cat.label
                                }
                        ) {
                            CategoryPill(icon = cat.icon, label = cat.label)
                        }
                    }
                }

                // 🔸 NUEVO: chip para limpiar filtro + subtítulo de filtro activo
                if (!selectedCategory.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { selectedCategory = null },
                            label = { Text("Quitar filtro") }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Filtrando por: ${selectedCategory}",
                            color = Color(0xFF8C8C8C),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // Recomendado → ahora se filtra según la categoría seleccionada
            item {
                SectionTitle(
                    if (selectedCategory.isNullOrBlank())
                        "Recomendado para ti"
                    else
                        "Cupones (${selectedCategory})",
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (filteredPromos.isEmpty()) {
                    // Estado vacío cuando no hay coincidencias
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay cupones para esta categoría.",
                            color = Color(0xFF8C8C8C),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    PromoCarousel(
                        promos = filteredPromos,
                        modifier = Modifier.height(130.dp),
                        onItemClick = { _ ->
                            nav.navigate(Screens.PromoQR.route)
                        }
                    )
                }
            }

            // Ofertas Especiales → Business
            item {
                SectionTitle(
                    "Ofertas Especiales",
                    Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp)
                )
                MerchantRow(
                    data = specialOffers,
                    onItemClick = { _ ->
                        nav.navigate(Screens.Business.route)
                    }
                )
            }

            // Lo nuevo → Business (opcional)
            item {
                SectionTitle(
                    "Lo Nuevo",
                    Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 6.dp)
                )
                MerchantRow(
                    data = newOffers,
                    onItemClick = { _ ->
                        nav.navigate(Screens.Business.route)
                    }
                )
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

/* ────────────────────────────────────────────────────────────────────────────
   Utilidad de mapeo de categorías → ajusta a tu taxonomía real
   Idealmente tus Promos deberían tener un campo `category` o `tags`.
   Aquí hago un mapeo demo por `subtitle` y texto.
   ──────────────────────────────────────────────────────────────────────────── */
private fun matchesCategory(promo: Promo, category: String): Boolean {
    val c = category.lowercase()

    val tags: Set<String> = when (promo.subtitle.lowercase()) {
        "cine stelar"      -> setOf("cine", "entretenimiento")
        "el sazón de iván" -> setOf("mexicana", "pozole", "restaurante")
        "bocado rápido"    -> setOf("saludable", "comida rápida", "veg", "bowls")
        "café norte"       -> setOf("cafetería", "café", "postres")
        else               -> emptySet()
    }

    val text = "${promo.title} ${promo.subtitle} ${promo.body}".lowercase()

    return c in tags || text.contains(c)
}
