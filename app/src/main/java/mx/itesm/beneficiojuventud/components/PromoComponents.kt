package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.model.Promo
import mx.itesm.beneficiojuventud.model.PromoTheme

// Paletas de texto para los dos themes
data class PromoTextColors(
    val titleColor: Color,
    val subtitleColor: Color,
    val bodyColor: Color
)

val LightTextTheme = PromoTextColors(
    titleColor = Color(0xFFFFFFFF),
    subtitleColor = Color(0xFFD3D3D3),
    bodyColor   = Color(0xFFC3C3C3)
)

val DarkTextTheme = PromoTextColors(
    titleColor = Color(0xFF505050),
    subtitleColor = Color(0xFF616161),
    bodyColor   = Color(0xFF636363)
)

@Composable
fun PromoCarousel(
    promos: List<Promo>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(promos.size) { i ->
            PromoImageBanner(
                promo = promos[i],
                modifier = Modifier
                    .fillParentMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

@Composable
fun PromoImageBanner(
    promo: Promo,
    modifier: Modifier = Modifier
) {
    val radius = 16.dp

    // Colores de texto por theme
    val textColors = when (promo.theme) {
        PromoTheme.LIGHT -> LightTextTheme
        PromoTheme.DARK  -> DarkTextTheme
    }

    // Degradado por theme
    val gradientBrush = when (promo.theme) {
        PromoTheme.LIGHT -> Brush.horizontalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF2B2B2B).copy(alpha = 1f),
                0.15f to Color(0xFF2B2B2B).copy(alpha = .95f),
                0.30f to Color(0xFF2B2B2B).copy(alpha = .70f),
                0.45f to Color(0xFF2B2B2B).copy(alpha = .40f),
                0.60f to Color(0xFF2B2B2B).copy(alpha = .25f),
                0.75f to Color.Transparent,
                1.00f to Color.Transparent
            )
        )
        PromoTheme.DARK -> Brush.horizontalGradient(
            colorStops = arrayOf(
                0.00f to Color.White.copy(alpha = 1f),
                0.15f to Color.White.copy(alpha = .95f),
                0.30f to Color.White.copy(alpha = .70f),
                0.45f to Color.White.copy(alpha = .40f),
                0.60f to Color.White.copy(alpha = .25f),
                0.75f to Color.Transparent,
                1.00f to Color.Transparent
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(radius),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFD3D3D3)),
        modifier = modifier
    ) {
        Box(Modifier.clip(RoundedCornerShape(radius))) {

            Image(
                painter = painterResource(promo.bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.85f)
            )

            // Degradado encima
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
                    promo.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = textColors.titleColor
                )
                Text(
                    promo.subtitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColors.subtitleColor
                )
                if (promo.body.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    TwoLineEllipsizedText(
                        text = promo.body,
                        color = textColors.bodyColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth(0.5f) // ocupa 50% del card
                            .padding(end = 8.dp)
                    )
                }
            }
        }
    }
}

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
        overflow = TextOverflow.Clip, // nosotros agregamos la "…"
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

@Preview(showBackground = true)
@Composable
private fun PromoImageBannerPreview() {
    // Ejemplos independientes (no uses promos[i] aquí)
    val sampleLight = Promo(
        bg = R.drawable.bolos,
        title = "Martes 2×1",
        subtitle = "Cine Stelar",
        body = "Compra un boleto y obtén el segundo gratis para la misma función.",
        theme = PromoTheme.LIGHT
    )
    val sampleDark = sampleLight.copy(theme = PromoTheme.DARK)

    MaterialTheme {
        Column(Modifier.padding(8.dp)) {
            PromoImageBanner(
                promo = sampleLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 6.dp)
            )
            PromoImageBanner(
                promo = sampleDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 6.dp)
            )
        }
    }
}
