package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.RedeemedCouponViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import androidx.compose.runtime.mutableStateListOf

enum class HistoryType { CUPON_USADO, CUPON_GUARDADO, FAVORITO_AGREGADO, FAVORITO_QUITADO }

data class HistoryEntry(
    val type: HistoryType,
    val title: String,
    val subtitle: String,
    val date: String,
    val iso: String
)

/**
 * IMPORTANTE: NO crear aquí el UserViewModel con viewModel().
 * Debe venir INYECTADO desde el NavGraph padre para que sea la MISMA instancia
 * que usa la pantalla donde agregas/quitas favoritos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(
    nav: NavHostController,
    userId: String,
    userViewModel: UserViewModel,                 // <= compartido
    modifier: Modifier = Modifier,
    redeemedVm: RedeemedCouponViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(BJTab.Profile) }

    // 1) Canjes reales
    val redeemedList by redeemedVm.redeemedListState.collectAsState()
    LaunchedEffect(userId) {
        runCatching { redeemedVm.getRedeemedByUser(userId) }
    }

    // 2) Eventos de favoritos – in-memory
    val favoriteEntries = remember { mutableStateListOf<HistoryEntry>() }

    LaunchedEffect(userId, userViewModel) {
        userViewModel.favoritePromoEvents.collectLatest { evt ->
            val type = when (evt) {
                is UserViewModel.FavoritePromoEvent.Added  -> HistoryType.FAVORITO_AGREGADO
                is UserViewModel.FavoritePromoEvent.Removed -> HistoryType.FAVORITO_QUITADO
            }

            val title = if (type == HistoryType.FAVORITO_AGREGADO)
                "Favorito agregado" else "Favorito eliminado"

            val subtitle = buildString {
                append(evt.title)
                evt.businessName?.takeIf { it.isNotBlank() }?.let { append(" en $it") }
            }

            favoriteEntries.add(
                HistoryEntry(
                    type = type,
                    title = title,
                    subtitle = subtitle,
                    date = formatShort(evt.timestampIso),
                    iso = evt.timestampIso
                )
            )
        }
    }

    // 3) Mapear canjes -> HistoryEntry
    val redeemedEntries: List<HistoryEntry> = remember(redeemedList) {
        redeemedList.map { rc ->
            val promoTitle = rc.promotion?.title ?: "Cupón"
            val business = rc.promotion?.businessName ?: ""
            val subtitle = if (business.isNotBlank()) "$promoTitle en $business" else promoTitle
            val iso = rc.usedAt ?: ""
            HistoryEntry(
                type = HistoryType.CUPON_USADO,
                title = "Cupón usado",
                subtitle = subtitle,
                date = formatShort(iso),
                iso = iso
            )
        }
    }

    // 4) Fusionar y ordenar por ISO desc
    val entries: List<HistoryEntry> = remember(redeemedEntries, favoriteEntries) {
        (redeemedEntries + favoriteEntries).sortedByDescending {
            parseIso(it.iso) ?: OffsetDateTime.MIN
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = { BJTopHeader(title = "Historial", nav = nav) },
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (entries.isEmpty()) {
                    item {
                        Text(
                            text = "Aún no tienes actividad.",
                            color = Color(0xFFAEAEAE),
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(entries) { item ->
                        HistoryCard(
                            entry = item,
                            onClick = { /* navegar a detalle opcional */ }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Versión 1.0.01",
                            color = Color(0xFFAEAEAE),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    entry: HistoryEntry,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = SolidColor(Color(0xFFD3D3D3))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconVector = when (entry.type) {
                HistoryType.FAVORITO_AGREGADO,
                HistoryType.FAVORITO_QUITADO -> Icons.Outlined.FavoriteBorder
                HistoryType.CUPON_USADO,
                HistoryType.CUPON_GUARDADO -> Icons.Outlined.ConfirmationNumber
            }

            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithCache {
                        val gradient = Brush.linearGradient(
                            listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush = gradient, blendMode = BlendMode.SrcAtop)
                        }
                    }
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF616161)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.subtitle,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFFAEAEAE)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = entry.date,
                fontSize = 10.sp,
                color = Color(0xFFAEAEAE)
            )
        }
    }
}

private fun formatShort(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(iso)
        odt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    } catch (_: DateTimeParseException) {
        iso
    }
}

private fun parseIso(iso: String?): OffsetDateTime? = try {
    if (iso.isNullOrBlank()) null else OffsetDateTime.parse(iso)
} catch (_: Exception) {
    null
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        // Para preview, creamos un fake VM local (no hay eventos reales)
        val fakeVm: UserViewModel = viewModel()
        History(nav = nav, userId = "preview", userViewModel = fakeVm)
    }
}

/**
 * Wrapper para usar EN PRODUCCIÓN dentro de tu NavHost.
 * Ajusta "main_graph" al route del NavGraph padre que contiene tus tabs.
 */
@Composable
fun HistoryScreen(
    nav: NavHostController,
    userId: String,
    parentGraphRoute: String = "main_graph"
) {
    val parentEntry = remember(nav) { nav.getBackStackEntry(parentGraphRoute) }
    val sharedUserVm: UserViewModel = viewModel(parentEntry)
    History(nav = nav, userId = userId, userViewModel = sharedUserVm)
}
