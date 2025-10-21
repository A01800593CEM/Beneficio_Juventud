// mx/itesm/beneficiojuventud/view/PromoQR.kt
package mx.itesm.beneficiojuventud.view

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.model.promos.PromoTheme        // <- IMPORT CORRECTO
import mx.itesm.beneficiojuventud.model.promos.PromotionState
import mx.itesm.beneficiojuventud.model.promos.PromotionType
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.BookingViewModel
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModelFactory
import mx.itesm.beneficiojuventud.model.bookings.BookingStatus
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.IconToggleButton
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import mx.itesm.beneficiojuventud.model.redeemedcoupon.RemoteServiceRedeemedCoupon

// ---------- Log
private const val TAG = "PromoQR"

// ---------- Mappers de enums a display ----------
private val PromotionType.displayName: String
    get() = when (this) {
        PromotionType.descuento   -> "Descuento"
        PromotionType.multicompra -> "Multicompra"
        PromotionType.regalo      -> "Regalo"
        PromotionType.otro        -> "Otro"
    }

private val PromotionState.displayName: String
    get() = when (this) {
        PromotionState.activa     -> "Activa"
        PromotionState.inactiva   -> "Inactiva"
        PromotionState.finalizada -> "Finalizada"
    }

// ---------- Modelo UI para la pantalla ----------
data class PromoDetailUi(
    val bannerUrlOrRes: Any, // String URL o @DrawableRes Int
    val title: String,
    val merchant: String,
    val discountLabel: String,
    val validUntil: String,
    val description: String,
    val terms: String,
    val stockLabel: String,
    val theme: PromoTheme = PromoTheme.light,
    val accentColor: Color = Color(0xFF008D96), // puedes cambiar si agregas accent en backend
    val isBookable: Boolean = false,
    val dailyLimitPerUser: Int? = null
)

// ---------- Paletas/gradiente (idéntico a PromoComponents) ----------
private data class PromoTextColors(
    val titleColor: Color,
    val subtitleColor: Color,
    val bodyColor: Color
)

private val LightTextTheme = PromoTextColors(
    titleColor = Color(0xFFFFFFFF),
    subtitleColor = Color(0xFFD3D3D3),
    bodyColor = Color(0xFFC3C3C3)
)

private val DarkTextTheme = PromoTextColors(
    titleColor = Color(0xFF505050),
    subtitleColor = Color(0xFF616161),
    bodyColor = Color(0xFF636363)
)

private fun textColorsFor(theme: PromoTheme) =
    when (theme) {
        PromoTheme.light -> LightTextTheme
        PromoTheme.dark  -> DarkTextTheme
    }

private fun bannerGradient(theme: PromoTheme) =
    when (theme) {
        PromoTheme.light -> Brush.horizontalGradient(
            0.00f to Color(0xFF2B2B2B).copy(alpha = 1f),
            0.15f to Color(0xFF2B2B2B).copy(alpha = .95f),
            0.30f to Color(0xFF2B2B2B).copy(alpha = .70f),
            0.45f to Color(0xFF2B2B2B).copy(alpha = .40f),
            0.60f to Color(0xFF2B2B2B).copy(alpha = .25f),
            0.75f to Color.Transparent,
            1.00f to Color.Transparent
        )
        PromoTheme.dark -> Brush.horizontalGradient(
            0.00f to Color.White.copy(alpha = 1f),
            0.15f to Color.White.copy(alpha = .95f),
            0.30f to Color.White.copy(alpha = .70f),
            0.45f to Color.White.copy(alpha = .40f),
            0.60f to Color.White.copy(alpha = .25f),
            0.75f to Color.Transparent,
            1.00f to Color.Transparent
        )
    }

// ---------- Helpers ----------
private fun formatDate(date: Date?): String {
    if (date == null) return "—"
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return fmt.format(date)
}

private fun parseDate(iso: String?): Date? = runCatching {
    if (iso.isNullOrBlank()) return null
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    sdf.parse(iso)
}.getOrNull()

private fun buildDiscountLabel(p: Promotions): String {
    p.promotionString?.takeIf { it.isNotBlank() }?.let { return it }
    return p.promotionType?.displayName ?: "Promoción"
}

private fun toUi(p: Promotions): PromoDetailUi {
    val banner = p.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.bolos
    val title  = p.title ?: "Promoción"
    val merch  = p.businessName ?: "Sin Nombre Negocio"
    val valid  = formatDate(parseDate(p.endDate))
    val desc   = p.description ?: "Sin descripción."
    val terms  = buildString {
        append("Tipo: ${p.promotionType?.displayName ?: "—"}")
        p.promotionState?.let { append(" • Estado: ${it.displayName}") }
    }

    // --- NUEVO: texto de stock/uso según bookable ---
    val isBookable = p.isBookable == true
    val stockText = if (isBookable) {
        "Disponibles: ${p.availableStock ?: "—"}"
    } else {
        "Usos disponibles: ${p.limitPerUser ?: "—"}"
    }

    val themeMode = p.theme ?: PromoTheme.light

    return PromoDetailUi(
        bannerUrlOrRes = banner,
        title = title,
        merchant = merch,
        discountLabel = buildDiscountLabel(p),
        validUntil = valid,
        description = desc,
        terms = terms,
        stockLabel = stockText,
        theme = themeMode,
        accentColor = if (themeMode == PromoTheme.dark) Color(0xFF00A3A3) else Color(0xFF008D96),
        isBookable = isBookable,
        dailyLimitPerUser = p.dailyLimitPerUser
    )
}

// ---------- QR (ZXing) ----------
private fun buildQrPayload(
    promotionId: Int,
    userId: String,
    limitPerUser: Int?
): String {
    val version = 1
    val ts = System.currentTimeMillis()
    val nonce = UUID.randomUUID().toString().substring(0, 8)
    val lpu = limitPerUser ?: -1
    val payload = "bj|v=$version|pid=$promotionId|uid=$userId|lpu=$lpu|ts=$ts|n=$nonce"

    Log.d(TAG, "========== QR GENERATION DEBUG ==========")
    Log.d(TAG, "promotionId: $promotionId")
    Log.d(TAG, "userId: $userId")
    Log.d(TAG, "limitPerUser: $limitPerUser")
    Log.d(TAG, "version: $version")
    Log.d(TAG, "timestamp: $ts")
    Log.d(TAG, "nonce: $nonce")
    Log.d(TAG, "QR Payload: $payload")
    Log.d(TAG, "=========================================")

    return payload
}

private fun bitMatrixToBitmap(matrix: BitMatrix): Bitmap {
    val width = matrix.width
    val height = matrix.height
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    for (y in 0 until height) {
        for (x in 0 until width) {
            bmp.setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
        }
    }
    return bmp
}

private fun generateQrImageBitmap(data: String, sizePx: Int = 900): ImageBitmap? = runCatching {
    val hints = mapOf(
        EncodeHintType.MARGIN to 1,
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
    )
    val matrix = MultiFormatWriter().encode(
        data,
        BarcodeFormat.QR_CODE,
        sizePx,
        sizePx,
        hints
    )
    bitMatrixToBitmap(matrix).asImageBitmap()
}.getOrElse {
    Log.e(TAG, "Error generando imagen de QR", it)
    null
}

// ---------- Pantalla ----------
@Composable
fun PromoQR(
    nav: NavHostController,
    promotionId: Int,
    cognitoId: String,
    modifier: Modifier = Modifier,
    viewModel: PromoViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current)),
    bookingViewModel: BookingViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(BJTab.Coupons) }
    var showQrDialog by rememberSaveable { mutableStateOf(false) }
    var isRedeemed by rememberSaveable { mutableStateOf(false) }

    val promo by viewModel.promoState.collectAsState()
    val favPromos by userViewModel.favoritePromotions.collectAsState()
    val userError by userViewModel.error.collectAsState()

    // Booking states
    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
    val bookingError by bookingViewModel.error.collectAsState()
    val bookingLoading by bookingViewModel.isLoading.collectAsState()
    val bookingMessage by bookingViewModel.message.collectAsState()
    val userBookings by bookingViewModel.bookings.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar promo
    LaunchedEffect(promotionId) {
        isLoading = true
        error = null
        try {
            viewModel.getPromotionById(promotionId)
        } catch (e: Exception) {
            error = e.message ?: "Error al cargar la promoción."
        } finally {
            isLoading = false
        }
    }

    // Cargar favoritos del usuario
    LaunchedEffect(cognitoId) {
        try { userViewModel.getFavoritePromotions(cognitoId) } catch (_: Exception) {}
    }

    // Cargar reservas del usuario
    LaunchedEffect(cognitoId) {
        try { bookingViewModel.loadUserBookings(cognitoId) } catch (_: Exception) {}
    }

    LaunchedEffect(userError) {
        userError?.let { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
    }

    // Manejar éxito de reservación
    LaunchedEffect(bookingSuccess) {
        if (bookingSuccess) {
            // Navegar a pantalla de éxito
            nav.navigate(
                Screens.Status.createRoute(
                    StatusType.COUPON_RESERVATION_SUCCESS,
                    Screens.Favorites.route
                )
            ) {
                popUpTo(Screens.PromoQR.route) { inclusive = true }
            }
            bookingViewModel.resetBookingSuccess()
        }
    }

    // Mostrar errores de reservación
    LaunchedEffect(bookingError) {
        bookingError?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = "OK",
                    duration = SnackbarDuration.Long
                )
            }
            bookingViewModel.clearError()
        }
    }

    // Mostrar mensajes de reservación y recargar bookings
    LaunchedEffect(bookingMessage) {
        bookingMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
            }
            bookingViewModel.clearMessage()

            // Recargar bookings después de cancelación
            if (msg.contains("cancelada", ignoreCase = true)) {
                Log.d(TAG, "Recargando bookings después de cancelación")
                kotlinx.coroutines.delay(500) // Pequeña espera para asegurar que el DB se actualizó
                bookingViewModel.loadUserBookings(cognitoId)
            }
        }
    }

    // Model UI
    val detail: PromoDetailUi? = remember(promo) {
        val hasData = (promo.title != null) || (promo.description != null) || (promo.imageUrl != null)
        if (hasData) toUi(promo) else null
    }

    // QR
    val qrPayload = remember(promotionId, cognitoId, promo.limitPerUser) {
        buildQrPayload(promotionId, cognitoId, promo.limitPerUser)
    }
    var qrImage by remember(promotionId, cognitoId, promo.limitPerUser) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(qrPayload) { qrImage = generateQrImageBitmap(qrPayload, sizePx = 900) }

    // Favoritos (optimista)
    val isFavoriteRemote = remember(favPromos, promotionId) { favPromos.any { it.promotionId == promotionId } }
    var isFavoriteLocal by remember(promotionId, isFavoriteRemote) { mutableStateOf(isFavoriteRemote) }
    fun toggleFavorite() {
        val newValue = !isFavoriteLocal
        isFavoriteLocal = newValue
        try {
            if (newValue) userViewModel.favoritePromotion(promotionId, cognitoId)
            else userViewModel.unfavoritePromotion(promotionId, cognitoId)
            userViewModel.refreshFavorites(cognitoId)
        } catch (_: Exception) {}
    }

    // Log de todos los bookings para debug
    LaunchedEffect(userBookings) {
        Log.d(TAG, "Total userBookings: ${userBookings.size}")
        userBookings.forEach { booking ->
            Log.d(TAG, "Booking: id=${booking.bookingId}, promotionId=${booking.promotionId}, status=${booking.status}")
        }
    }

    // Verificar si la promoción está reservada (solo bookings activos)
    val currentBooking = remember(userBookings, promotionId) {
        val found = userBookings.find {
            it.promotionId == promotionId &&
            it.status != BookingStatus.CANCELLED
        }
        Log.d(TAG, "Current booking for promotion $promotionId: ${found?.bookingId}")
        found
    }

    // Buscar booking cancelado para verificar cooldown
    // Note: Backend doesn't track cancelledDate, so cooldown feature is disabled
    val cancelledBooking = remember(userBookings, promotionId) {
        val found = userBookings.find {
            it.promotionId == promotionId &&
            it.status == BookingStatus.CANCELLED
        }
        Log.d(TAG, "Cancelled booking for promotion $promotionId: ${found?.bookingId}")
        found
    }

    // Estado de la reserva con expiración
    val bookingExpired = remember(currentBooking) {
        currentBooking?.let { isBookingExpired(it.bookingDate) } ?: false
    }

    val isReserved = currentBooking != null && !bookingExpired

    // Estados mutables para actualización en tiempo real
    var timeRemaining by remember { mutableStateOf(Pair(0L, 0L)) }
    var inCooldown by remember { mutableStateOf(false) }
    var cooldownTime by remember { mutableStateOf(Pair(0L, 0L)) }

    // Actualizar contadores cada segundo
    LaunchedEffect(currentBooking, cancelledBooking) {
        while (true) {
            // Actualizar tiempo restante de reserva
            val newTimeRemaining = currentBooking?.let { getTimeUntilExpiration(it.bookingDate) } ?: Pair(0L, 0L)
            timeRemaining = newTimeRemaining

            // Auto-cancelar si el tiempo llegó a 0 y aún existe el booking
            if (currentBooking != null && newTimeRemaining.first == 0L && newTimeRemaining.second == 0L) {
                val expired = isBookingExpired(currentBooking.bookingDate)
                if (expired) {
                    Log.d(TAG, "Timer llegó a 0, auto-cancelando booking ${currentBooking.bookingId}")
                    bookingViewModel.cancelBooking(currentBooking)
                }
            }

            // Cooldown feature disabled - backend doesn't track cancelledDate
            // TODO: If cooldown is needed, backend must be updated to track cancellation timestamps
            inCooldown = false
            cooldownTime = Pair(0L, 0L)

            kotlinx.coroutines.delay(1000) // Actualizar cada segundo
        }
    }

    // Auto-cancelar reservas expiradas cuando se abre la pantalla
    LaunchedEffect(currentBooking, bookingExpired) {
        if (currentBooking != null && bookingExpired) {
            Log.d(TAG, "Reserva expirada detectada, cancelando automáticamente: ${currentBooking.bookingId}")
            bookingViewModel.cancelBooking(currentBooking)
        }
    }

    // Theme
    val currentTheme = detail?.theme ?: PromoTheme.light
    val texts = textColorsFor(currentTheme)
    val gradientBrush = bannerGradient(currentTheme)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = { BJTopHeader(title = "Promoción", nav = nav) },
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
            isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            error != null -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { Text(text = error!!, color = Color.Red) }
            }
            detail != null -> {
                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth()
                                .height(210.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(Modifier.fillMaxSize()) {
                                // Banner + loader con color según theme (como en PromoComponents)
                                val dataToLoad = if (detail.bannerUrlOrRes is String)
                                    detail.bannerUrlOrRes else (detail.bannerUrlOrRes as Int)

                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(dataToLoad)
                                        .crossfade(true)
                                        .error(R.drawable.bolos)
                                        .build(),
                                    contentDescription = detail.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(alpha = 0.92f),
                                    loading = {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(
                                                strokeWidth = 2.dp,
                                                color = if (currentTheme == PromoTheme.light)
                                                    Color.White.copy(alpha = 0.85f) else Color(0xFF505050)
                                            )
                                        }
                                    },
                                    success = { SubcomposeAsyncImageContent() },
                                    error = { SubcomposeAsyncImageContent() }
                                )

                                // Overlay de gradiente según theme (idéntico a componentes)
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .drawWithCache {
                                            onDrawWithContent {
                                                drawContent()
                                                drawRect(brush = gradientBrush)
                                            }
                                        }
                                )

                                // NUEVO: chip "Stock limitado" cuando es bookable
                                if (detail.isBookable) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFFFF3CD)) // amarillo suave
                                            .border(BorderStroke(1.dp, Color(0xFFFFEEA8)), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Stock limitado",
                                            color = Color(0xFF8A6D3B),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Favorito con surface blanca para contraste (como en PromoImageBannerFav)
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color.White.copy(alpha = 0.92f),
                                    border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                ) {
                                    IconToggleButton(
                                        checked = isFavoriteLocal,
                                        onCheckedChange = { toggleFavorite() }
                                    ) {
                                        Icon(
                                            imageVector = if (isFavoriteLocal) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = if (isFavoriteLocal) "Quitar de favoritos" else "Agregar a favoritos",
                                            tint = if (isFavoriteLocal) Color(0xFFE53935) else Color(0xFF505050),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                // Textos
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = detail.merchant,
                                        color = texts.subtitleColor.copy(alpha = 0.95f),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = detail.title,
                                        color = texts.titleColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth(0.70f)
                                    )
                                }
                            }
                        }
                    }

                    item { InfoCardApi(detail, texts) }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = detail.stockLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3C3C3C),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        MainButton(
                            text = "Ver QR",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onClick = { showQrDialog = true }
                        )
                    }
                    item {
                        if (detail.isBookable) {
                            Spacer(Modifier.height(8.dp))

                            // Botón con estado de carga
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                val redGradient = Brush.linearGradient(
                                    listOf(Color(0xFFDC2626), Color(0xFFEF4444))
                                )

                                val greyGradient = Brush.linearGradient(
                                    listOf(Color(0xFF9CA3AF), Color(0xFF6B7280))
                                )

                                MainButton(
                                    text = when {
                                        bookingLoading && !isReserved -> "Reservando..."
                                        bookingLoading && isReserved -> "Cancelando..."
                                        inCooldown -> {
                                            val (m, s) = cooldownTime
                                            "Cooldown (${formatTimeRemaining(m, s)})"
                                        }
                                        bookingExpired -> "Reserva Expirada"
                                        isReserved -> {
                                            val (m, s) = timeRemaining
                                            if (m > 0 || s > 0) {
                                                "Cancelar (${formatTimeRemaining(m, s)})"
                                            } else {
                                                "Cancelar Reservación"
                                            }
                                        }
                                        else -> "Reservar Cupón"
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundGradient = when {
                                        inCooldown || bookingExpired -> greyGradient
                                        isReserved -> redGradient
                                        else -> null
                                    },
                                    enabled = !bookingExpired && !inCooldown && !bookingLoading,
                                    onClick = {
                                        if (isReserved) {
                                            // Cancelar reservación (pero mantener en favoritos)
                                            currentBooking?.let { booking ->
                                                bookingViewModel.cancelBooking(booking)
                                            }
                                        } else if (inCooldown) {
                                            val (h, m) = cooldownTime
                                            val timeMsg = formatTimeRemaining(h, m)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Debes esperar $timeMsg antes de volver a reservar",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } else {
                                            // Verificar que hay stock disponible
                                            val available = promo.availableStock ?: 0
                                            if (available <= 0) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "No hay stock disponible",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                return@MainButton
                                            }

                                            // Agregar a favoritos si no está ya
                                            if (!isFavoriteLocal) {
                                                try {
                                                    userViewModel.favoritePromotion(promotionId, cognitoId)
                                                    isFavoriteLocal = true
                                                    userViewModel.refreshFavorites(cognitoId)
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Error al agregar a favoritos", e)
                                                }
                                            }

                                            // Reservar cupón
                                            bookingViewModel.reserveCoupon(promo, cognitoId)
                                        }
                                    }
                                )

                                // Mostrar loading spinner si está cargando
                                if (bookingLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 16.dp)
                                            .size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Versión 1.0.12",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showQrDialog) {
        // Polling para detectar cuando el cupón es canjeado por otro dispositivo
        val qrGenerationTime = remember { System.currentTimeMillis() }

        LaunchedEffect(showQrDialog) {
            while (showQrDialog && !isRedeemed) {
                try {
                    delay(3000L) // Consultar cada 3 segundos

                    Log.d(TAG, "Polling: Verificando si el cupón fue canjeado...")
                    val redeemedCoupons = RemoteServiceRedeemedCoupon.getRedeemedCouponsByUser(cognitoId)

                    // Buscar si hay un cupón canjeado de esta promoción después del momento de generación del QR
                    val wasRedeemed = redeemedCoupons.any { coupon ->
                        coupon.promotionId == promotionId &&
                        coupon.qrTimestamp != null &&
                        coupon.qrTimestamp!! >= qrGenerationTime - 5000 // 5 segundos de margen
                    }

                    if (wasRedeemed) {
                        Log.d(TAG, "¡Cupón detectado como canjeado! Mostrando pantalla de éxito")
                        isRedeemed = true
                        delay(1000L) // Dar tiempo para que se vea la animación
                        showQrDialog = false
                        nav.navigate(
                            Screens.Status.createRoute(
                                StatusType.COUPON_USE_SUCCESS,
                                Screens.Home.route
                            )
                        ) {
                            popUpTo(Screens.PromoQR.route) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al verificar estado del cupón: ${e.message}")
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
                .background(Color.Black.copy(alpha = 0.50f))
                .pointerInput(Unit) { detectTapGestures(onTap = { showQrDialog = false }) },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) { detectTapGestures(onTap = { /* consume */ }) }
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 300.dp, max = 420.dp)
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Código de Canje",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(targetState = isRedeemed, label = "redeem_overlay_xfade") { redeemed ->
                            if (redeemed) {
                                RedeemedCardInner()
                            } else {
                                QRCardInner(detail = detail ?: return@Crossfade, qrBitmap = qrImage)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    MainButton(
                        text = if (isRedeemed) "Volver" else "Cerrar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        onClick = {
                            showQrDialog = false
                            if (isRedeemed) {
                                // Si el cupón fue canjeado, navegar a la pantalla de éxito
                                nav.navigate(
                                    Screens.Status.createRoute(
                                        StatusType.COUPON_USE_SUCCESS,
                                        Screens.Home.route
                                    )
                                ) {
                                    popUpTo(Screens.PromoQR.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ---------- Subsecciones ----------
@Composable
private fun InfoCardApi(detail: PromoDetailUi, texts: PromoTextColors) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Descripción",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF454545),
                fontSize = 14.sp
            )
            Text(
                text = detail.description,
                color = texts.bodyColor,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Términos y condiciones",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF454545),
                fontSize = 14.sp
            )
            Text(
                text = "Vigencia: ${detail.validUntil}",
                color = texts.subtitleColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = detail.terms,
                color = texts.bodyColor,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun QRCardInner(
    detail: PromoDetailUi,
    qrBitmap: ImageBitmap?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(
                modifier = Modifier.size(220.dp).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(bitmap = qrBitmap, contentDescription = "QR", modifier = Modifier.fillMaxSize())
                } else {
                    CircularProgressIndicator()
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Presenta este código QR en ${detail.merchant} para canjear tu descuento",
            color = Color(0xFF6B7280),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp).padding(horizontal = 8.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RedeemedCardInner() {
    val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = Color(0xFFDFE3E6),
                    style = Stroke(width = strokeWidth, pathEffect = dash),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_check_circle),
            contentDescription = null,
            tint = Color(0xFF22C55E),
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text("¡Cupón Canjeado!", color = Color(0xFF22C55E), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text("Gracias por usar nuestros servicios", color = Color.Gray, fontSize = 12.sp)
    }
}

// ---------- Helpers de Expiración y Cooldown ----------

/**
 * Verifica si una reserva ha expirado (1 minuto desde bookingDate para pruebas)
 */
private fun isBookingExpired(bookingDate: String?): Boolean {
    if (bookingDate.isNullOrBlank()) return false
    return try {
        val booking = parseDate(bookingDate) ?: return false
        val now = Date()
        val diffMillis = now.time - booking.time
        val minute1InMillis = 1L * 60 * 1000  // 1 minuto para pruebas
        diffMillis >= minute1InMillis
    } catch (e: Exception) {
        false
    }
}

/**
 * Calcula el tiempo restante hasta la expiración
 * Retorna pair de (minutos, segundos) para pruebas
 */
private fun getTimeUntilExpiration(bookingDate: String?): Pair<Long, Long> {
    if (bookingDate.isNullOrBlank()) return Pair(0L, 0L)
    return try {
        val booking = parseDate(bookingDate) ?: return Pair(0L, 0L)
        val now = Date()
        val diffMillis = now.time - booking.time
        val minute1InMillis = 1L * 60 * 1000  // 1 minuto para pruebas
        val remainingMillis = maxOf(0L, minute1InMillis - diffMillis)

        val minutes = remainingMillis / (1000 * 60)
        val seconds = (remainingMillis % (1000 * 60)) / 1000

        Pair(minutes, seconds)
    } catch (e: Exception) {
        Pair(0L, 0L)
    }
}

/**
 * Formatea el tiempo restante en formato legible "Xm Ys" para pruebas
 */
private fun formatTimeRemaining(minutes: Long, seconds: Long): String {
    return when {
        minutes > 0 && seconds > 0 -> "${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m"
        seconds > 0 -> "${seconds}s"
        else -> "Expirando..."
    }
}

/**
 * Verifica si una reserva cancelada está en período de cooldown (1 minuto desde cancelación para pruebas)
 */
private fun isInCooldown(cancelledDate: String?): Boolean {
    if (cancelledDate.isNullOrBlank()) return false
    return try {
        val cancelled = parseDate(cancelledDate) ?: return false
        val now = Date()
        val diffMillis = now.time - cancelled.time
        val minute1InMillis = 1L * 60 * 1000  // 1 minuto cooldown para pruebas
        diffMillis < minute1InMillis
    } catch (e: Exception) {
        false
    }
}

/**
 * Calcula el tiempo restante de cooldown en formato (minutos, segundos) para pruebas
 */
private fun getTimeUntilCooldownEnd(cancelledDate: String?): Pair<Long, Long> {
    if (cancelledDate.isNullOrBlank()) return Pair(0L, 0L)
    return try {
        val cancelled = parseDate(cancelledDate) ?: return Pair(0L, 0L)
        val now = Date()
        val diffMillis = now.time - cancelled.time
        val minute1InMillis = 1L * 60 * 1000  // 1 minuto para pruebas
        val remainingMillis = maxOf(0L, minute1InMillis - diffMillis)

        val minutes = remainingMillis / (1000 * 60)
        val seconds = (remainingMillis % (1000 * 60)) / 1000

        Pair(minutes, seconds)
    } catch (e: Exception) {
        Pair(0L, 0L)
    }
}

// ---------- Preview ----------
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PromoQRPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        PromoQR(nav = nav, promotionId = 123, cognitoId = "a1fbe500-a091-70e3-5a7b-3b1f4537f10f")
    }
}
