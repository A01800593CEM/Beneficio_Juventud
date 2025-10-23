package mx.itesm.beneficiojuventud.view

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.CategoryViewModel
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel

// Insets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import mx.itesm.beneficiojuventud.model.promos.PromoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Coupons(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    vm: CategoryViewModel = viewModel(),          // VM de categorías
    promoVm: PromoViewModel = viewModel()         // VM de promociones reales
) {
    var selectedTab by rememberSaveable { mutableStateOf(BJTab.Coupons) }

    // Estados de categorías
    val categories by vm.categories.collectAsState()
    val isLoadingCategories by vm.loading.collectAsState(initial = false)
    val categoriesError by vm.error.collectAsState(initial = null)

    // Estados de promos
    val promos by promoVm.promoListState.collectAsState()
    var loadingPromos by rememberSaveable { mutableStateOf(false) }
    var promosError by rememberSaveable { mutableStateOf<String?>(null) }

    // Filtro por categoría (id)
    var selectedCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()

    // Cargar categorías si están vacías
    LaunchedEffect(Unit) {
        if (categories.isEmpty()) vm.loadCategories()
    }

    // Cargar promos (SOLO ACTIVAS) al entrar
    LaunchedEffect(Unit) {
        loadingPromos = true
        promosError = null
        try {
            promoVm.getActivePromotions()
        } catch (e: Exception) {
            promosError = e.message ?: "Error cargando promociones"
        } finally {
            loadingPromos = false
        }
    }

    // Reaccionar a cambio de categoría (SOLO ACTIVAS)
    LaunchedEffect(selectedCategoryId) {
        loadingPromos = true
        promosError = null
        try {
            if (selectedCategoryId == null) {
                promoVm.getActivePromotions()
            } else {
                // Si tu backend espera el nombre, ajusta aquí; de momento usamos id -> toString()
                promoVm.getActivePromotionsByCategory(selectedCategoryId.toString())
            }
        } catch (e: Exception) {
            promosError = e.message ?: "Error filtrando promociones"
        } finally {
            loadingPromos = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        topBar = {
            BJTopHeader(
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
                        BJTab.Home      -> nav.navigate(Screens.Home.route) {
                            popUpTo(Screens.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                        BJTab.Coupons   -> nav.navigate(Screens.Coupons.route) {
                            popUpTo(Screens.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                        BJTab.Favorites -> nav.navigate(Screens.Favorites.route) {
                            popUpTo(Screens.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                        BJTab.Profile   -> nav.navigate(Screens.Profile.route) {
                            popUpTo(Screens.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        // Altura estimada de la bottom bar + navegación del sistema
        val bottomInset = WindowInsets.navigationBars
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues()
            .calculateBottomPadding()
        val bottomBarHeight = 56.dp

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentPadding = PaddingValues(
                bottom = bottomBarHeight + bottomInset + 16.dp
            )
        ) {

            // Categorías Populares (desde API)
            item {
                SectionTitle(
                    "Categorías Populares",
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when {
                    isLoadingCategories -> {
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

                    categoriesError != null -> {
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
                                key = { it.id ?: (it.name ?: it.hashCode().toString()).hashCode() }
                            ) { c ->
                                val id = c.id ?: return@items
                                val name = c.name ?: "Categoría"

                                // Usa tu función de ícono dinámico
                                val icon = iconForCategoryName(name)

                                CategoryPill(
                                    icon = icon,
                                    label = name,
                                    selected = selectedCategoryId == id,
                                    onClick = {
                                        selectedCategoryId =
                                            if (selectedCategoryId == id) null else id
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
                    if (selectedCategoryId == null) "Todos los cupones" else "Cupones por categoría",
                    Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 6.dp)
                )
            }

            // Estado de carga/errores/empty para promos
            when {
                loadingPromos -> {
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Cargando cupones…")
                        }
                    }
                }

                promosError != null -> {
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Error al cargar cupones",
                                color = Color(0xFFB00020),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(12.dp))
                            TextButton(onClick = {
                                scope.launch {
                                    loadingPromos = true
                                    promosError = null
                                    try {
                                        if (selectedCategoryId == null) {
                                            promoVm.getAllPromotions()
                                        } else {
                                            promoVm.getPromotionByCategory(selectedCategoryId.toString())
                                        }
                                    } catch (e: Exception) {
                                        promosError = e.message ?: "Error desconocido"
                                    } finally {
                                        loadingPromos = false
                                    }
                                }
                            }) { Text("Reintentar") }
                        }
                    }
                }

                promos.isEmpty() -> {
                    item {
                        EmptyState(
                            title = "No hay cupones disponibles",
                            body = if (selectedCategoryId == null)
                                "Vuelve más tarde para ver nuevas promociones."
                            else
                                "No encontramos cupones en esta categoría. Prueba con otra."
                        )
                    }
                }

                else -> {
                    // Lista de cupones reales usando tu PromoImageBanner (Coil)
                    items(
                        items = promos,
                        key = { it.promotionId ?: it.hashCode() }
                    ) { promo ->
                        PromoImageBanner(
                            promo = promo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            onClick = {
                                val id = promo.promotionId ?: return@PromoImageBanner
                                nav.navigate("promoQR/$id")
                            },
                            themeResolver = { p -> p.theme ?: PromoTheme.light }
                        )
                    }
                }
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


@Composable
fun EmptyState(
    title: String,
    body: String
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
        Spacer(Modifier.height(6.dp))
        Text(body, fontSize = 12.sp, color = Color(0xFF9E9E9E))
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
