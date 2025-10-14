package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.PromoTheme

// Paletas de texto
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

/**
 * Carrusel de promociones reales desde la BD.
 */
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
                    .width(320.dp)      // banner tipo “hero”
                    .height(150.dp),
                onClick = { onItemClick(promo) }
            )
        }
    }
}


/**
 * Card individual para cada promoción.
 */
@Composable
fun PromoImageBanner(
    promo: Promotions,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    themeResolver: (Promotions) -> PromoTheme = { PromoTheme.LIGHT }
) {
    val radius = 16.dp
    val theme = themeResolver(promo)

    val textColors = when (theme) {
        PromoTheme.LIGHT -> LightTextTheme
        PromoTheme.DARK -> DarkTextTheme
    }

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

    val title = promo.title ?: "Promoción"
    val subtitle = buildString {
        promo.promotionType?.let { append(it.name.replaceFirstChar { c -> c.uppercase() }) }
        promo.promotionState?.let { append(" • ${it.name.replaceFirstChar { c -> c.uppercase() }}") }
    }
    val body = promo.description.orEmpty()

    // Soporta null/empty
    val dataToLoad = promo.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.placeholder_promo

    Surface(
        shape = RoundedCornerShape(radius),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFD3D3D3)),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(Modifier.clip(RoundedCornerShape(radius))) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(dataToLoad)
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_promo) // crea un drawable simple
                    .error(R.drawable.placeholder_promo)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
                            .fillMaxWidth(0.7f) // un poco más ancho para textos largos
                            .padding(end = 8.dp)
                    )
                }
            }
        }
    }
}


/**
 * Texto truncado en 2 líneas (con "…").
 */
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
