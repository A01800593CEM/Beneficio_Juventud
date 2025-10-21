package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.components.PromoImageBannerFav
import mx.itesm.beneficiojuventud.components.SectionTitle
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.promos.PromoTheme
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModelFactory

// 🚩 IMPORTA la card horizontal (la del screenshot)
import mx.itesm.beneficiojuventud.components.MerchantCardHorizontalFav

// ------------------------------------------------------------
// Ahora con cognitoId (String) como ID del colaborador
data class FavoriteMerchant(
    val cognitoId: String, // ID del colaborador (Cognito)
    val imageRes: Int,
    val name: String,
    val category: String,
    val location: String,
    val rating: Double,
    val isFavorite: Boolean = true
)
// ------------------------------------------------------------

private enum class FavoriteMode { Coupons, Businesses }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Favorites(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    favoriteMerchants: List<FavoriteMerchant> = emptyList(),
    userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current))
) {
    var selectedTab by remember { mutableStateOf(BJTab.Favorites) }
    var mode by remember { mutableStateOf(FavoriteMode.Coupons) }

    val user by userViewModel.userState.collectAsState()
    val favoritePromos by userViewModel.favoritePromotions.collectAsState()
    val favoriteCollabs by userViewModel.favoriteCollabs.collectAsState()
    val reservedPromos by userViewModel.reservedPromotions.collectAsState()
    val userBookings by userViewModel.userBookings.collectAsState()

    // Normaliza IDs favoritos de colaboradores a Set<String> para lookup O(1)
    val favoriteCollabIds: Set<String> = remember(favoriteCollabs) {
        favoriteCollabs.mapNotNull { it.cognitoId?.takeIf(String::isNotBlank) }.toSet()
    }

    // Set de IDs de promociones reservadas para lookup rápido
    val reservedPromoIds: Set<Int> = remember(userBookings) {
        userBookings.mapNotNull { it.promotionId }.toSet()
    }

    // Set de IDs de promociones favoritas para lookup rápido
    val favoritePromoIds: Set<Int> = remember(favoritePromos) {
        favoritePromos.mapNotNull { it.promotionId }.toSet()
    }

    val scope = rememberCoroutineScope()
    val cognitoId = user.cognitoId.orEmpty()

    LaunchedEffect(cognitoId) {
        if (cognitoId.isNotBlank()) {
            userViewModel.refreshFavorites(cognitoId)
        }
    }

    // ✅ MOVER ESTE CÁLCULO FUERA DEL BLOQUE DE LazyColumn
    val allCoupons = remember(reservedPromos, favoritePromos, reservedPromoIds) {
        // Primero los reservados
        val reserved = reservedPromos.toList()
        // Luego los favoritos que NO están reservados
        val favoritesOnly = favoritePromos.filter { promo ->
            promo.promotionId?.let { it !in reservedPromoIds } ?: true
        }
        reserved + favoritesOnly
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            BJTopHeader(
                title = if (mode == FavoriteMode.Coupons) "Cupones Favoritos" else "Negocios Favoritos",
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)
        ) {

            // Toggle modo
            item {
                Spacer(Modifier.height(8.dp))
                TogglePill(
                    left = "Cupones",
                    right = "Negocios",
                    selectedLeft = (mode == FavoriteMode.Coupons),
                    onSelectLeft = { mode = FavoriteMode.Coupons },
                    onSelectRight = { mode = FavoriteMode.Businesses },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            // Título con conteo
            item {
                val count = when (mode) {
                    FavoriteMode.Businesses -> favoriteCollabIds.size
                    FavoriteMode.Coupons    -> allCoupons.size
                }
                val label = if (mode == FavoriteMode.Businesses)
                    "$count Negocios Guardados" else "$count Cupones Guardados"

                SectionTitle(label, Modifier.padding(top = 6.dp, bottom = 8.dp))
            }

            // Si no hay login, mensaje genérico
            if (cognitoId.isBlank()) {
                item {
                    EmptyState(
                        title = "Inicia sesión para ver tus favoritos",
                        body = "Necesitamos tu cuenta para sincronizar cupones y negocios guardados."
                    )
                }
                return@LazyColumn
            }

            // Contenido según modo
            when (mode) {
                FavoriteMode.Businesses -> {
                    if (favoriteCollabs.isEmpty()) {
                        item {
                            EmptyState(
                                title = "Sin negocios favoritos",
                                body = "Cuando marques un negocio como favorito aparecerá aquí."
                            )
                        }
                    } else {
                        items(
                            items = favoriteCollabs,
                            key = { it.cognitoId ?: it.businessName ?: it.email ?: it.hashCode().toString() }
                        ) { collab: Collaborator ->
                            val isFav = favoriteCollabIds.contains(collab.cognitoId.orEmpty())

                            MerchantCardHorizontalFav(
                                collab = collab,
                                isFavorite = isFav,
                                onFavoriteClick = {
                                    collab.cognitoId?.let { collabId ->
                                        scope.launch {
                                            userViewModel.unfavoriteCollaborator(collabId, cognitoId)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 6.dp),
                                onClick = {
                                    collab.cognitoId?.let { id ->
                                        nav.navigate(Screens.Business.createRoute(id))
                                    }
                                }
                            )
                        }
                    }
                }

                FavoriteMode.Coupons -> {
                    if (allCoupons.isEmpty()) {
                        item {
                            EmptyState(
                                title = "Sin cupones favoritos",
                                body = "Guarda cupones para verlos aquí y canjéalos más tarde."
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = allCoupons,
                            key = { index, promo ->
                                // Usar índice + promotionId para garantizar unicidad
                                "${index}_${promo.promotionId ?: promo.hashCode()}"
                            }
                        ) { index, promo ->
                            val isReserved = promo.promotionId?.let { it in reservedPromoIds } ?: false
                            val isFavorite = promo.promotionId?.let { it in favoritePromoIds } ?: false

                            PromoImageBannerFav(
                                promo = promo,
                                isFavorite = isFavorite,
                                isReserved = isReserved,
                                onFavoriteClick = { p ->
                                    val id = p.promotionId ?: return@PromoImageBannerFav
                                    scope.launch {
                                        if (isFavorite) {
                                            userViewModel.unfavoritePromotion(id, cognitoId)
                                        } else {
                                            userViewModel.favoritePromotion(id, cognitoId)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .padding(vertical = 6.dp),
                                onClick = {
                                    val id = promo.promotionId ?: return@PromoImageBannerFav
                                    nav.navigate(Screens.PromoQR.createRoute(id))
                                },
                                themeResolver = { it.theme ?: PromoTheme.light }
                            )
                        }
                    }
                }
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
            // Left
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
            // Right
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

// Función deprecated - ya no se usa, se usa MerchantCardHorizontalFav
/*
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
                val isFav = merchant.isFavorite
                Icon(
                    imageVector = if (isFav) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFav) "Quitar de favoritos" else "Agregar a favoritos",
                    tint = if (isFav) Color(0xFFE53935) else Color(0xFF9E9E9E)
                )
            }
        }
    }
}
*/

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FavoritePreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        Favorites(nav = nav)
    }
}
