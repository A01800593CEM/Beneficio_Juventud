package mx.itesm.beneficiojuventud.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.components.PromoImageBanner
import mx.itesm.beneficiojuventud.components.SectionTitle
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel

private const val TAG = "Business"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Business(
    nav: NavHostController,
    collabId: String,
    userCognitoId: String,
    modifier: Modifier = Modifier,
    promoViewModel: PromoViewModel,
    collabViewModel: CollabViewModel,
    userViewModel: UserViewModel
) {
    var selectedTab by remember { mutableStateOf(BJTab.Home) }

    // ⬇️ añade initial=... para evitar "Cannot infer type..."
    val collaborator: Collaborator by collabViewModel
        .collabState
        .collectAsState(initial = Collaborator())

    val promos: List<Promotions> by promoViewModel
        .promoListState
        .collectAsState(initial = emptyList())

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Tu UserViewModel expone 'error', NO 'errorMessage'
    val errorMsg: String? by userViewModel.error.collectAsState(initial = null)

    // ⬇️ favoritos de colaboradores (objetos) desde el VM actual
    val favoriteCollabs: List<Collaborator> by userViewModel
        .favoriteCollabs
        .collectAsState(initial = emptyList())

    // Set de IDs (cognitoId) para consulta O(1) en UI
    val favIds: Set<String> = remember(favoriteCollabs) {
        favoriteCollabs.mapNotNull { it.cognitoId }.toSet()
    }

    // Cargar colaborador (por cognitoId) y todas las promos
    LaunchedEffect(collabId) {
        runCatching {
            isLoading = true
            collabViewModel.getCollaboratorById(collabId)   // acepta cognitoId:String
            promoViewModel.getAllPromotions()
        }.onFailure { e -> error = e.message }
        isLoading = false
    }

    // Traer favoritos del usuario (colabs + promos)
    LaunchedEffect(userCognitoId) {
        runCatching { userViewModel.refreshFavorites(userCognitoId) }
            .onFailure { e -> Log.e(TAG, "Error al refrescar favoritos: ${e.message}") }
    }

    // Mostrar errores en snackbar (si manejas errores globales del VM)
    LaunchedEffect(errorMsg) {
        errorMsg?.let { snackbarHostState.showSnackbar(it) }
    }

    // Estado de favorito del colaborador actual (derivado del set)
    val isFavoriteRemote by remember(favIds, collabId) {
        mutableStateOf(favIds.contains(collabId))
    }
    var isFavoriteLocal by remember(collabId, isFavoriteRemote) {
        mutableStateOf(isFavoriteRemote)
    }

    fun toggleFavorite() {
        // Optimistic UI
        isFavoriteLocal = !isFavoriteLocal

        runCatching {
            userViewModel.toggleFavoriteCollaborator(
                collaboratorId = collabId,     // es cognitoId
                cognitoId = userCognitoId
            )
        }.onFailure { e ->
            Log.e(TAG, "toggleFavoriteCollaborator error: ${e.message}")
        }

        // Refrescar para asegurar consistencia con backend
        runCatching { userViewModel.refreshFavorites(userCognitoId) }
    }

    // Promos del colaborador actual
    val coupons: List<Promotions> = remember(promos, collabId) {
        promos.filter { it.collaboratorId == collabId }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            BJTopHeader(
                title = collaborator.businessName ?: "Negocio",
                nav = nav
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error al cargar: $error", color = Color(0xFFB00020))
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 96.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        BusinessHeroHeader(
                            name = collaborator.businessName.orEmpty(),
                            description = collaborator.description.orEmpty(),
                            address = collaborator.address.orEmpty(),
                            logoUrl = collaborator.logoUrl.orEmpty(),
                            favoriteChecked = isFavoriteLocal,
                            onToggleFavorite = ::toggleFavorite
                        )
                    }

                    item { SectionTitle(text = "${coupons.size} Cupones Disponibles") }

                    if (coupons.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay promociones para este negocio.",
                                    color = Color(0xFF8C8C8C)
                                )
                            }
                        }
                    } else {
                        items(coupons, key = { it.promotionId ?: it.hashCode() }) { promo ->
                            PromoImageBanner(
                                promo = promo,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                onClick = {
                                    promo.promotionId?.let { id ->
                                        nav.navigate(Screens.PromoQR.createRoute(id))
                                    }
                                }
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                "Versión 1.0.01",
                                color = Color(0xFFAEAEAE),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BusinessHeroHeader(
    name: String,
    description: String,
    address: String,
    logoUrl: String,
    favoriteChecked: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val ctx = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(shape)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(ctx)
                .data(logoUrl.ifBlank { null })
                .crossfade(true)
                .build(),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Degradado overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val brush = Brush.horizontalGradient(
                        0.00f to Color(0xFF2B2B2B).copy(alpha = 1f),
                        0.20f to Color(0xFF2B2B2B).copy(alpha = .85f),
                        0.50f to Color(0xFF2B2B2B).copy(alpha = .5f),
                        1.00f to Color.Transparent
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush)
                    }
                }
        )

        // Botón de favorito
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.25f)
            ) {
                IconToggleButton(
                    checked = favoriteChecked,
                    onCheckedChange = { onToggleFavorite() }
                ) {
                    Icon(
                        imageVector = if (favoriteChecked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (favoriteChecked) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (favoriteChecked) Color(0xFFFF3B3B) else Color.White,
                        modifier = Modifier
                            .size(34.dp)
                            .padding(horizontal = 5.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Textos
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = name,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth(0.7f),
                maxLines = 2
            )
            if (description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color(0xFFD3D3D3),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
            if (address.isNotBlank()) {
                Text(
                    text = address,
                    color = Color(0xFFC3C3C3),
                    fontSize = 12.sp
                )
            }
        }
    }
}
