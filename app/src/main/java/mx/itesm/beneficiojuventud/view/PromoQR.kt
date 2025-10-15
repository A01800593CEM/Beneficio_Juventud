package mx.itesm.beneficiojuventud.view

import android.graphics.Bitmap
import android.util.Log // LOG: import
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.model.PromoTheme
import mx.itesm.beneficiojuventud.model.promos.PromotionState
import mx.itesm.beneficiojuventud.model.promos.PromotionType
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.IconToggleButton

// LOG: etiqueta global para Logcat
private const val TAG = "PromoQR"

// ---------- Mappers de texto ----------
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

// ---------- Modelo solo para QR visual ----------
data class PromoDetailUi(
    val bannerUrlOrRes: Any, // String URL o @DrawableRes Int
    val title: String,
    val merchant: String,
    val discountLabel: String,
    val validUntil: String,
    val description: String,
    val terms: String,
    val stockLabel: String,
    val theme: PromoTheme = PromoTheme.LIGHT
)


// ---------- Helpers ----------
private fun formatDate(date: Date?): String {
    if (date == null) return "—"
    val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return fmt.format(date)
}

private fun buildDiscountLabel(p: Promotions): String {
    p.promotionString?.takeIf { it.isNotBlank() }?.let { return it }
    return p.promotionType?.displayName ?: "Promoción"
}

private fun toUi(p: Promotions): PromoDetailUi {
    val banner = p.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.bolos
    val title  = p.title ?: "Promoción"
    val merch  = p.collaboratorId?.let { "Colaborador #$it" } ?: "Comercio"
    val valid  = formatDate(p.endDate)
    val desc   = p.description ?: "Sin descripción."
    val terms  = buildString {
        append("Tipo: ${p.promotionType?.displayName ?: "—"}")
        p.promotionState?.let { append(" • Estado: ${it.displayName}") }
    }
    val stock  = if (p.totalStock != null && p.availableStock != null) {
        "Stock: ${p.availableStock} / ${p.totalStock}"
    } else "Stock: —"

    return PromoDetailUi(
        bannerUrlOrRes = banner,
        title = title,
        merchant = merch,
        discountLabel = buildDiscountLabel(p),
        validUntil = valid,
        description = desc,
        terms = terms,
        stockLabel = stock,
        theme = PromoTheme.LIGHT
    )
}

// ----- QR payload & encoding (ZXing) -----
private fun buildQrPayload(
    promotionId: Int,
    userId: String,
    limitPerUser: Int?
): String {
    val version = 1
    val ts = System.currentTimeMillis()
    val nonce = UUID.randomUUID().toString().substring(0, 8)
    val lpu = limitPerUser ?: -1
    return "bj|v=$version|pid=$promotionId|uid=$userId|lpu=$lpu|ts=$ts|n=$nonce"
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

private fun generateQrImageBitmap(data: String, sizePx: Int = 900): ImageBitmap? {
    return try {
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
    } catch (e: Exception) {
        Log.e(TAG, "Error generando imagen de QR", e) // LOG
        null
    }
}

// ---------- Pantalla ----------
@Composable
fun PromoQR(
    nav: NavHostController,
    promotionId: Int,
    cognitoId: String,
    modifier: Modifier = Modifier,
    viewModel: PromoViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel() // <- USANDO BACKEND
) {
    var selectedTab by remember { mutableStateOf(BJTab.Coupons) }
    var showQrDialog by rememberSaveable { mutableStateOf(false) }
    var isRedeemed by rememberSaveable { mutableStateOf(false) }

    val promo by viewModel.promoState.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    // Snackbar para feedback
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar promo
    LaunchedEffect(promotionId) {
        isLoading = true
        error = null
        Log.d(TAG, "API:getPromotionById START promotionId=$promotionId") // LOG
        try {
            viewModel.getPromotionById(promotionId)
            Log.d(TAG, "API:getPromotionById DONE promotionId=$promotionId") // LOG
        } catch (e: Exception) {
            Log.e(TAG, "API:getPromotionById ERROR promotionId=$promotionId", e) // LOG
            error = e.message ?: "Error al cargar la promoción."
        } finally {
            isLoading = false
        }
    }

    // Cargar favoritos del usuario desde backend
    val favPromos by userViewModel.favoritePromotions.collectAsState()
    val userError by userViewModel.error.collectAsState()

    LaunchedEffect(cognitoId) {
        Log.d(TAG, "API:getFavoritePromotions START user=$cognitoId") // LOG
        try {
            userViewModel.getFavoritePromotions(cognitoId)
            Log.d(TAG, "API:getFavoritePromotions DONE user=$cognitoId") // LOG
        } catch (e: Exception) {
            Log.e(TAG, "API:getFavoritePromotions ERROR user=$cognitoId", e) // LOG
        }
    }

    // Mostrar errores del backend en snackbar
    LaunchedEffect(userError) {
        userError?.let { msg ->
            Log.w(TAG, "API:UserViewModel ERROR message=$msg") // LOG
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    // Logs cuando cambia promo (respuesta de getPromotionById)
    LaunchedEffect(promo) {
        if ((promo.title != null) || (promo.description != null) || (promo.imageUrl != null)) {
            Log.d(
                TAG,
                "STATE:promo UPDATED id=$promotionId title=${promo.title} type=${promo.promotionType} state=${promo.promotionState}"
            )
        }
    }

    // Logs cuando cambian favoritos (respuesta de getFavoritePromotions / refreshFavorites)
    LaunchedEffect(favPromos) {
        Log.d(TAG, "STATE:favorites UPDATED user=$cognitoId count=${favPromos.size}") // LOG
    }

    val detail: PromoDetailUi? = remember(promo) {
        val hasData = (promo.title != null) || (promo.description != null) || (promo.imageUrl != null)
        if (hasData) toUi(promo) else null
    }

    val qrPayload = remember(promotionId, cognitoId, promo.limitPerUser) {
        val payload = buildQrPayload(
            promotionId = promotionId,
            userId = cognitoId,
            limitPerUser = promo.limitPerUser
        )
        // LOG: evita exponer completo el UID en logs
        Log.d(
            TAG,
            "QR:payload BUILT pid=$promotionId uidPrefix=${cognitoId.take(6)} lpu=${promo.limitPerUser} length=${payload.length}"
        )
        payload
    }
    var qrImage by remember(promotionId, cognitoId, promo.limitPerUser) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(qrPayload) {
        Log.d(TAG, "QR:image GENERATE START") // LOG
        qrImage = generateQrImageBitmap(qrPayload, sizePx = 900)
        Log.d(TAG, "QR:image GENERATE DONE success=${qrImage != null}") // LOG
    }

    // ======= Favorito (derivado del backend) + UI optimista =======
    val isFavoriteRemote = remember(favPromos, promotionId) {
        favPromos.any { it.promotionId == promotionId }
    }
    var isFavoriteLocal by remember(promotionId, isFavoriteRemote) { mutableStateOf(isFavoriteRemote) }

    fun toggleFavorite() {
        val newValue = !isFavoriteLocal
        Log.d(
            TAG,
            "UI:toggleFavorite CLICK promotionId=$promotionId user=$cognitoId from=$isFavoriteLocal to=$newValue"
        )
        // Optimista
        isFavoriteLocal = newValue

        // Llamadas reales
        try {
            if (newValue) {
                Log.d(TAG, "API:favoritePromotion START pid=$promotionId user=$cognitoId")
                userViewModel.favoritePromotion(promotionId, cognitoId)
                Log.d(TAG, "API:favoritePromotion DONE pid=$promotionId user=$cognitoId")
            } else {
                Log.d(TAG, "API:unfavoritePromotion START pid=$promotionId user=$cognitoId")
                userViewModel.unfavoritePromotion(promotionId, cognitoId)
                Log.d(TAG, "API:unfavoritePromotion DONE pid=$promotionId user=$cognitoId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "API:favorite/unfavorite ERROR pid=$promotionId user=$cognitoId", e)
        }

        // Refrescar lista
        try {
            Log.d(TAG, "API:refreshFavorites START user=$cognitoId")
            userViewModel.refreshFavorites(cognitoId)
            Log.d(TAG, "API:refreshFavorites DONE user=$cognitoId")
        } catch (e: Exception) {
            Log.e(TAG, "API:refreshFavorites ERROR user=$cognitoId", e)
        }
    }

    val theme = detail?.theme ?: PromoTheme.LIGHT
    val titleColor    = if (theme == PromoTheme.LIGHT) Color(0xFFFFFFFF) else Color(0xFF505050)
    val subtitleColor = if (theme == PromoTheme.LIGHT) Color(0xFFD3D3D3) else Color(0xFF616161)
    val gradientBrush = when (theme) {
        PromoTheme.LIGHT -> Brush.horizontalGradient(
            0.00f to Color(0xFF2B2B2B).copy(alpha = 1f),
            0.15f to Color(0xFF2B2B2B).copy(alpha = .95f),
            0.30f to Color(0xFF2B2B2B).copy(alpha = .70f),
            0.45f to Color(0xFF2B2B2B).copy(alpha = .40f),
            0.60f to Color(0xFF2B2B2B).copy(alpha = .25f),
            0.75f to Color.Transparent,
            1.00f to Color.Transparent
        )
        PromoTheme.DARK -> Brush.horizontalGradient(
            0.00f to Color.White.copy(alpha = 1f),
            0.15f to Color.White.copy(alpha = .95f),
            0.30f to Color.White.copy(alpha = .70f),
            0.45f to Color.White.copy(alpha = .40f),
            0.60f to Color.White.copy(alpha = .25f),
            0.75f to Color.Transparent,
            1.00f to Color.Transparent
        )
    }

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
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            error != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { Text(text = error!!, color = Color.Red) }
            }
            detail != null -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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
                                // Banner desde URL o drawable
                                if (detail.bannerUrlOrRes is String) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(detail.bannerUrlOrRes)
                                            .crossfade(true)
                                            .placeholder(R.drawable.bolos)
                                            .error(R.drawable.bolos)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(alpha = 0.92f)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = detail.bannerUrlOrRes as Int),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(alpha = 0.92f)
                                    )
                                }

                                // Overlay de gradiente
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

                                // Etiqueta de descuento (arriba-derecha)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF008D96))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = detail.discountLabel,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                // Corazón flotante (abajo-derecha) con backend
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 18.dp, bottom = 18.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.Black.copy(alpha = 0.25f)
                                    ) {
                                        IconToggleButton(
                                            checked = isFavoriteLocal,
                                            onCheckedChange = { toggleFavorite() }
                                        ) {
                                            Icon(
                                                imageVector = if (isFavoriteLocal) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = if (isFavoriteLocal) "Quitar de favoritos" else "Agregar a favoritos",
                                                tint = if (isFavoriteLocal) Color(0xFFFF3B3B) else Color.White,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .padding(horizontal = 5.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                // Textos (abajo-izquierda)
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = detail.merchant,
                                        color = subtitleColor.copy(alpha = 0.95f),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = detail.title,
                                        color = titleColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth(0.70f) // 70% ancho
                                    )
                                }
                            }
                        }
                    }

                    item { InfoCardApi(detail) }

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
                        Crossfade(
                            targetState = isRedeemed,
                            label = "redeem_overlay_xfade"
                        ) { redeemed ->
                            if (redeemed) {
                                RedeemedCardInner()
                            } else {
                                QRCardInner(
                                    detail = detail ?: return@Crossfade,
                                    qrBitmap = qrImage
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isRedeemed = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("Simular escaneo") }

                        MainButton(
                            text = "Cerrar",
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            onClick = { showQrDialog = false }
                        )
                    }
                }
            }
        }
    }
}

// ---------- Secciones reutilizadas ----------
@Composable
private fun InfoCardApi(detail: PromoDetailUi) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
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
                color = Color.Gray,
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
                color = Color(0xFF6B7280),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = detail.terms,
                color = Color.Gray,
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
                modifier = Modifier
                    .size(220.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "QR",
                        modifier = Modifier.fillMaxSize()
                    )
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
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(horizontal = 8.dp),
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
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = dash
                    ),
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
        Text(
            text = "¡Cupón Canjeado!",
            color = Color(0xFF22C55E),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Gracias por usar nuestros servicios",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

// Preview local (usa una promo dummy)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PromoQRPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        PromoQR(
            nav = nav,
            promotionId = 123,
            cognitoId = "a1fbe500-a091-70e3-5a7b-3b1f4537f10f"
        )
    }
}
