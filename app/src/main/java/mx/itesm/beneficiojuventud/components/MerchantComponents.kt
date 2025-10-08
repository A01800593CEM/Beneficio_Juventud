package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.model.MerchantCardData

@Composable
fun MerchantRow(data: List<MerchantCardData>) {
    // ancho aprox 62% del ancho de pantalla
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.62f

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(data.size) { i ->
            MerchantCard(
                item = data[i],
                index = i,
                modifier = Modifier
                    .width(cardWidth)
                    .height(150.dp) // puedes subirlo a 160–170dp si quieres más texto
            )
        }
    }
}

@Composable
fun MerchantCard(item: MerchantCardData, index: Int, modifier: Modifier = Modifier) {
    val imageRes = if (index % 2 == 0) R.drawable.brasa else R.drawable.ensalada

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE6E6E6))
        ),
        modifier = modifier
    ) {
        Column {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF3A3A3A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", item.rating),
                        fontSize = 11.sp,
                        color = Color(0xFF7A7A7A)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.category,
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMerchantRow() {
    val sampleData = listOf(
        MerchantCardData("Sneaker Lab", "Limpieza de tenis", 4.8),
        MerchantCardData("Bocado Rápido", "Comida saludable", 4.6),
        MerchantCardData("Café Aroma", "Restaurante", 4.9),
        MerchantCardData("TechZone", "Electrónica", 4.7),
    )
    MaterialTheme {
        Surface(color = Color(0xFFF5F5F5)) {
            MerchantRow(sampleData)
        }
    }
}
