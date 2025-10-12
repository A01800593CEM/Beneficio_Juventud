package mx.itesm.beneficiojuventud.view

import CategoryViewModel
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.SectionTitle
import mx.itesm.beneficiojuventud.components.CategoryPill
import mx.itesm.beneficiojuventud.components.PromoImageBanner
import mx.itesm.beneficiojuventud.model.Promo
import mx.itesm.beneficiojuventud.model.PromoTheme
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

/**
 * Fuente de datos temporal para renderizar cupones en la lista.
 * Reemplazar por datos remotos cuando se integre el backend.
 */
private val coupons = listOf(
    Promo(
        bg = R.drawable.el_fuego_sagrado,
        title = "Martes 2×1",
        subtitle = "Cine Stelar",
        body = "Compra un boleto y obtén el segundo gratis para la misma función.",
        theme = PromoTheme.DARK
    ),
    Promo(
        bg = R.drawable.el_fuego_sagrado,
        title = "Jueves Pozolero",
        subtitle = "El Sazón de Iván",
        body = "2×1 en todos nuestros pozoles.",
        theme = PromoTheme.LIGHT
    ),
    Promo(
        bg = R.drawable.carne,
        title = "Lunes sin Carne",
        subtitle = "Bocado Rápido",
        body = "20% en bowls vegetarianos.",
        theme = PromoTheme.DARK
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Coupons(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    vm: CategoryViewModel = viewModel() // Usa el MISMO VM de categorías
) {
    var selectedTab by remember { mutableStateOf(BJTab.Coupons) }

    // Estados del VM
    val categories by vm.categories.collectAsState()
    val isLoading by vm.loading.collectAsState(initial = false)
    val error by vm.error.collectAsState(initial = null)

    // Filtro por categoría (id)
    var selectedCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }

    // Carga categorías si están vacías
    LaunchedEffect(Unit) {
        if (categories.isEmpty()) vm.loadCategories()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CouponsTopBar(
                title = "Cupones",
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
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Categorías Populares (desde API)
            item {
                SectionTitle(
                    "Categorías Populares",
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when {
                    isLoading -> {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Cargando categorías…")
                        }
                    }

                    error != null -> {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Error al cargar categorías",
                                color = Color(0xFFB00020),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(12.dp))
                            TextButton(onClick = { vm.loadCategories() }) { Text("Reintentar") }
                        }
                    }

                    else -> {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(
                                items = categories,
                                key = { it.id ?: (it.name ?: it.hashCode()).hashCode() }
                            ) { c ->
                                val id = c.id ?: return@items
                                val name = c.name ?: "Categoría"

                                CategoryPill(
                                    icon = Icons.Outlined.NotificationsNone, // Ajusta si tu API trae iconos
                                    label = name,
                                    selected = selectedCategoryId == id,
                                    onClick = {
                                        selectedCategoryId =
                                            if (selectedCategoryId == id) null else id
                                        // Si tienes endpoint por categoría, dispara aquí:
                                        // vm.loadCouponsByCategory(selectedCategoryId)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // Título de lista
            item {
                SectionTitle(
                    "Todos los Cupones",
                    Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 6.dp)
                )
            }

            // Lista de cupones usando PromoImageBanner (click -> navega a PromoQR)
            items(
                count = coupons.size,
                key = { it }
            ) { i ->
                val titleArg = Uri.encode(coupons[i].title)
                PromoImageBanner(
                    promo = coupons[i],
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    onClick = {
                        nav.navigate(Screens.PromoQR.route + "?idx=$i&title=$titleArg")
                    }
                )
            }

            // Pie
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Versión 1.0.01",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Barra superior de la pantalla de cupones.
 */
@Composable
private fun CouponsTopBar(
    title: String,
    nav: NavHostController
) {
    Column(
        Modifier
            .fillMaxWidth()
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
                BackButton(nav = nav)

                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CouponsPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Coupons(nav = nav)
    }
}
