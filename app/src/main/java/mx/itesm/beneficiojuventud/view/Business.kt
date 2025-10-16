// Business.kt (ajustado a IDs String para colaboradores favoritos)
package mx.itesm.beneficiojuventud.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel

private const val TAG = "Business"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Business(
    nav: NavHostController,
    collabId: String,      // ← cognitoId del colaborador (String)
    cognitoId: String,     // ← cognitoId del usuario (String)
    modifier: Modifier = Modifier,
    promoViewModel: PromoViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(BJTab.Home) }

    val collaborator by collabViewModel.collabState.collectAsState()
    val promos by promoViewModel.promoListState.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val userError by userViewModel.error.collectAsState()

    // Carga colaborador (por cognitoId) y promociones
    LaunchedEffect(collabId) {
        runCatching {
            isLoading = true
            collabViewModel.getCollaboratorById(collabId)  // ← asegúrate que acepte String
            promoViewModel.getAllPromotions()
        }.onFailure { e -> error = e.message }
        isLoading = false
    }

    // Favoritos (lista de cognitoId de colaboradores)
    val favCollabs by userViewModel.favoriteCollabs.collectAsState()

    LaunchedEffect(cognitoId) {
        runCatching { userViewModel.getFavoriteCollabs(cognitoId) }
    }

    LaunchedEffect(userError) {
        userError?.let { snackbarHostState.showSnackbar(it) }
    }

    // ¿Este colaborador (collabId String) está en favoritos?
    val isFavoriteRemote = remember(favCollabs, collabId) {
        favCollabs.contains(collabId)
    }
    var isFavoriteLocal by remember(collabId, isFavoriteRemote) {
        mutableStateOf(isFavoriteRemote)
    }

    fun toggleFavorite() {
        val to = !isFavoriteLocal
        isFavoriteLocal = to

        runCatching {
            userViewModel.toggleFavoriteCollaborator(collabId, cognitoId)
        }.onFailure { e ->
            Log.e(TAG, "Error al alternar favorito: ${e.message}")
        }

        // Refresca del backend para mantener consistencia
        runCatching { userViewModel.refreshFavorites(cognitoId) }
    }

    // Filtrar promos del colaborador actual (por cognitoId String)
    val coupons = remember(promos, collabId) {
        promos.filter { it.collaboratorId == collabId }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = { BJTopHeader(title = collaborator.businessName ?: "Negocio", nav = nav) },
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
                            name = collaborator.businessName ?: "",
                            description = collaborator.description ?: "",
                            address = collaborator.address ?: "",
                            logoUrl = collaborator.logoUrl ?: "",
                            favoriteChecked = isFavoriteLocal,
                            onToggleFavorite = { toggleFavorite() }
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
                                Text("No hay promociones para este negocio.", color = Color(0xFF8C8C8C))
                            }
                        }
                    } else {
                        items(coupons.size) { i ->
                            val promo = coupons[i]
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
                            Text("Versión 1.0.01", color = Color(0xFFAEAEAE), fontSize = 10.sp)
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
