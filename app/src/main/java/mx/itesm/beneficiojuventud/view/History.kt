package mx.itesm.beneficiojuventud.view

import android.util.Log
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import kotlinx.coroutines.flow.collectLatest
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import mx.itesm.beneficiojuventud.components.BJBottomBar
import mx.itesm.beneficiojuventud.components.BJTab
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.BookingViewModel
import mx.itesm.beneficiojuventud.viewmodel.RedeemedCouponViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModelFactory
import androidx.compose.runtime.mutableStateListOf
import mx.itesm.beneficiojuventud.model.RoomDB.LocalDatabase

enum class HistoryType {
    CUPON_USADO,
    CUPON_GUARDADO,
    CUPON_RESERVADO,
    RESERVA_CANCELADA,
    FAVORITO_AGREGADO,
    FAVORITO_QUITADO
}

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
    redeemedVm: RedeemedCouponViewModel = viewModel(),
    bookingVm: BookingViewModel = viewModel()      // <= para eventos de reservas
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(BJTab.Profile) }

    // Database para cargar historial persistente
    val database = remember {
        Room.databaseBuilder(context, LocalDatabase::class.java, "beneficio_juventud_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    // 1) Canjes reales desde backend
    val redeemedList by redeemedVm.redeemedListState.collectAsState()
    LaunchedEffect(userId) {
        runCatching { redeemedVm.getRedeemedByUser(userId) }
    }

    // 1b) Historial persistente desde Room (NUEVO)
    val persistedHistory = remember { mutableStateListOf<HistoryEntry>() }

    LaunchedEffect(userId) {
        try {
            database.historyDao().getHistoryByUser(userId).collectLatest { historyEntities ->
                persistedHistory.clear()
                historyEntities.forEach { entity ->
                    val type = when (entity.type) {
                        "CUPON_USADO" -> HistoryType.CUPON_USADO
                        "CUPON_RESERVADO" -> HistoryType.CUPON_RESERVADO
                        "RESERVA_CANCELADA" -> HistoryType.RESERVA_CANCELADA
                        "FAVORITO_AGREGADO" -> HistoryType.FAVORITO_AGREGADO
                        "FAVORITO_QUITADO" -> HistoryType.FAVORITO_QUITADO
                        else -> HistoryType.CUPON_GUARDADO
                    }
                    persistedHistory.add(
                        HistoryEntry(
                            type = type,
                            title = when (type) {
                                HistoryType.CUPON_USADO -> "Cupón usado"
                                HistoryType.CUPON_RESERVADO -> "Cupón reservado"
                                HistoryType.RESERVA_CANCELADA -> "Reservación cancelada"
                                HistoryType.FAVORITO_AGREGADO -> "Favorito agregado"
                                HistoryType.FAVORITO_QUITADO -> "Favorito eliminado"
                                else -> "Evento"
                            },
                            subtitle = entity.subtitle ?: "",
                            date = entity.date ?: "",
                            iso = entity.iso ?: ""
                        )
                    )
                }
                Log.d("History", "Historial persistente cargado: ${persistedHistory.size} entradas")
            }
        } catch (e: Exception) {
            Log.e("History", "Error loading persisted history", e)
        }
    }

    // 2) Eventos de favoritos – in-memory (nuevos eventos)
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

    // 2b) Eventos de reservas/cancelaciones – in-memory (nuevos eventos)
    val bookingEntries = remember { mutableStateListOf<HistoryEntry>() }

    LaunchedEffect(userId, bookingVm) {
        Log.d("History", "Iniciando escucha de eventos de booking para userId: $userId")
        bookingVm.bookingEvents.collectLatest { evt ->
            Log.d("History", "Evento recibido: ${evt::class.simpleName}, title=${evt.title}, business=${evt.businessName}")

            val type = when (evt) {
                is mx.itesm.beneficiojuventud.viewmodel.BookingEvent.Reserved -> HistoryType.CUPON_RESERVADO
                is mx.itesm.beneficiojuventud.viewmodel.BookingEvent.Cancelled -> HistoryType.RESERVA_CANCELADA
            }

            val title = when (type) {
                HistoryType.CUPON_RESERVADO -> "Cupón reservado"
                HistoryType.RESERVA_CANCELADA -> "Reservación cancelada"
                else -> "Evento"
            }

            val subtitle = buildString {
                append(evt.title)
                evt.businessName?.takeIf { it.isNotBlank() }?.let { append(" en $it") }
            }

            val entry = HistoryEntry(
                type = type,
                title = title,
                subtitle = subtitle,
                date = formatShort(evt.timestampIso),
                iso = evt.timestampIso
            )

            Log.d("History", "Agregando entrada al historial: $entry")
            bookingEntries.add(entry)
            Log.d("History", "Total de entradas de booking: ${bookingEntries.size}")
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
    // Usar derivedStateOf para que observe cambios en las listas mutables
    val entries: List<HistoryEntry> by remember {
        derivedStateOf {
            // Combinar: historial persistente (antiguo) + eventos en memoria (nuevo) + cupones redimidos
            val merged = (persistedHistory + redeemedEntries + favoriteEntries + bookingEntries)
                .distinctBy { it.iso }  // Evitar duplicados (cupones redimidos pueden estar en ambas)
                .sortedByDescending {
                    parseIso(it.iso) ?: OffsetDateTime.MIN
                }
            Log.d("History", "Entradas totales - Persisted: ${persistedHistory.size}, Redeemed: ${redeemedEntries.size}, Favorites: ${favoriteEntries.size}, Bookings: ${bookingEntries.size}, Total: ${merged.size}")
            merged
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
                HistoryType.CUPON_GUARDADO,
                HistoryType.CUPON_RESERVADO,
                HistoryType.RESERVA_CANCELADA -> Icons.Outlined.ConfirmationNumber
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
                color = Color(0xFFAEAEAE),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

private fun formatShort(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(iso)
        val date = odt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val time = odt.format(DateTimeFormatter.ofPattern("HH:mm"))
        "$date\n$time"
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
        val fakeVm: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current))
        History(nav = nav, userId = "preview", userViewModel = fakeVm)
    }
}

/**
 * Wrapper para usar EN PRODUCCIÓN dentro de tu NavHost.
 * Ajusta "main_graph" al route del NavGraph padre que contiene tus tabs.
 */
