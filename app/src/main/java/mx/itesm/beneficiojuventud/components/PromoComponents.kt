package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.model.promos.PromoTheme
import mx.itesm.beneficiojuventud.model.promos.PromotionState
import mx.itesm.beneficiojuventud.model.promos.PromotionType
import mx.itesm.beneficiojuventud.model.promos.Promotions

// -------------------- Paletas de texto --------------------

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

// -------------------- API pública --------------------

@Composable
fun PromoCarousel(
    promos: List<Promotions>,
    modifier: Modifier = Modifier,
    onItemClick: (Promotions) -> Unit = {}
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = promos,
            key = { it.promotionId ?: it.title ?: it.imageUrl ?: it.hashCode() }
        ) { promo ->
            PromoImageBanner(
                promo = promo,
                modifier = Modifier
                    .width(320.dp)
                    .height(150.dp),
                onClick = { onItemClick(promo) }
            )
        }
    }
}

// -------------------- Card de promoción --------------------

@Composable
fun PromoImageBanner(
    promo: Promotions,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    themeResolver: (Promotions) -> PromoTheme = { it.theme ?: PromoTheme.light }
) {
    val radius = 16.dp
    val theme = themeResolver(promo)

    val textColors = when (theme) {
        PromoTheme.light -> LightTextTheme
        PromoTheme.dark  -> DarkTextTheme
    }

    val gradientBrush = when (theme) {
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

    val title = promo.title ?: "Promoción"
    val subtitle = buildString {
        promo.promotionType?.let { append(it.displayName) }
        promo.promotionState?.let {
            if (isNotEmpty()) append(" • ")
            append(it.displayName)
        }
    }
    val body = promo.description.orEmpty()
    val dataToLoad = promo.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.bolos

    Surface(
        shape = RoundedCornerShape(radius),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFD3D3D3)),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(Modifier.clip(RoundedCornerShape(radius))) {

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(dataToLoad)
                    .crossfade(true)
                    .error(R.drawable.bolos)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = if (theme == PromoTheme.light)
                                Color.White.copy(alpha = 0.85f) else Color(0xFF505050)
                        )
                    }
                },
                success = { SubcomposeAsyncImageContent() },
                error = { SubcomposeAsyncImageContent() }
            )

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

            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = textColors.titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColors.subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (body.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    TwoLineEllipsizedText(
                        text = body,
                        color = textColors.bodyColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PromoImageBannerFav(
    promo: Promotions,
    isFavorite: Boolean,
    onFavoriteClick: (Promotions) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    themeResolver: (Promotions) -> PromoTheme = { it.theme ?: PromoTheme.light }
) {
    val radius = 16.dp
    val theme = themeResolver(promo)

    val textColors = when (theme) {
        PromoTheme.light -> LightTextTheme
        PromoTheme.dark  -> DarkTextTheme
    }

    val gradientBrush = when (theme) {
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

    val title = promo.title ?: "Promoción"
    val subtitle = buildString {
        promo.promotionType?.let { append(it.displayName) }
        promo.promotionState?.let {
            if (isNotEmpty()) append(" • ")
            append(it.displayName)
        }
    }
    val body = promo.description.orEmpty()
    val dataToLoad = promo.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.bolos

    Surface(
        shape = RoundedCornerShape(radius),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFD3D3D3)),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(Modifier.clip(RoundedCornerShape(radius))) {

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(dataToLoad)
                    .crossfade(true)
                    .error(R.drawable.bolos)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = if (theme == PromoTheme.light)
                                Color.White.copy(alpha = 0.85f) else Color(0xFF505050)
                        )
                    }
                },
                success = { SubcomposeAsyncImageContent() },
                error = { SubcomposeAsyncImageContent() }
            )

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

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(onClick = { onFavoriteClick(promo) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFE53935) else Color(0xFF505050)
                    )
                }
            }

            Column(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = textColors.titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColors.subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (body.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    TwoLineEllipsizedText(
                        text = body,
                        color = textColors.bodyColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

// -------------------- Helpers de UI --------------------

@Composable
private fun TwoLineEllipsizedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    lineHeight: TextUnit = 16.sp
) {
    var adjusted by remember(text) { mutableStateOf<String?>(null) }
    val display = adjusted ?: text.replace('\n', ' ')

    Text(
        text = display,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        maxLines = 2,
        softWrap = true,
        overflow = TextOverflow.Clip,
        modifier = modifier,
        onTextLayout = { layout ->
            if (adjusted == null && layout.hasVisualOverflow && layout.lineCount >= 2) {
                val end = layout.getLineEnd(1, visibleEnd = true)
                val clipped = display.take(end).trimEnd(' ', '\u00A0', '.', ',', ';', ':')
                adjusted = "$clipped…"
            }
        }
    )
}

// -------------------- Mappers de enums --------------------

private val PromotionType.displayName: String
    get() = when (this) {
        PromotionType.descuento -> "Descuento"
        PromotionType.multicompra -> "Multicompra"
        PromotionType.regalo -> "Regalo"
        PromotionType.otro -> "Otro"
    }

private val PromotionState.displayName: String
    get() = when (this) {
        PromotionState.activa -> "Activa"
        PromotionState.inactiva -> "Inactiva"
        PromotionState.finalizada -> "Finalizada"
    }
