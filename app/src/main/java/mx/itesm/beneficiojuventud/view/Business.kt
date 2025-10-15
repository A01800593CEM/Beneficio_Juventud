package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Business(
    nav: NavHostController,
    collabId: String,
    modifier: Modifier = Modifier,
    promoViewModel: PromoViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(BJTab.Home) }

    val collaborator by collabViewModel.collabState.collectAsState()
    val promos by promoViewModel.promoListState.collectAsState()

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 🔹 Cargar datos desde API
    LaunchedEffect(collabId) {
        runCatching {
            isLoading = true
            collabViewModel.getCollaboratorById(collabId)
            promoViewModel.getAllPromotions()
        }.onFailure { e ->
            error = e.message
        }
        isLoading = false
    }

    // 🔹 Filtrar promociones por colaborador
    val coupons = promos.filter { it.collaboratorId == collabId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            BJTopHeader(
                title = collaborator.businessName ?: "Negocio",
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
                            logoUrl = collaborator.logoUrl ?: ""
                        )
                    }

                    item {
                        SectionTitle(
                            text = "${coupons.size} Cupones Disponibles",
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

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
                        items(coupons.size) { i ->
                            PromoImageBanner(
                                promo = coupons[i],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
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

/**
 * Cabecera del negocio (usa datos reales del backend)
 */
@Composable
private fun BusinessHeroHeader(
    name: String,
    description: String,
    address: String,
    logoUrl: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val ctx = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
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

        // Gradiente oscuro lateral para textos legibles
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

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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
}
