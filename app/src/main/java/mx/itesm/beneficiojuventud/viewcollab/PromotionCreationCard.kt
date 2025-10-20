package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)

private val AppGradientBrush = Brush.horizontalGradient(
    colors = listOf(DarkBlue, Teal)
)

private val TitleGrey = Color(0xFF616161)
private val DescriptionGrey = Color(0xFF969696)

fun Modifier.applyGradient(brush: Brush) = this
    .graphicsLayer(alpha = 0.99f)
    .drawWithCache {
        onDrawWithContent {
            drawContent()
            drawRect(brush, blendMode = BlendMode.SrcAtop)
        }
    }

@Composable
fun PromotionCreationCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = AppGradientBrush,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .heightIn(min = 110.dp), // 🔹 Altura mínima común
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier
                .size(28.dp)
                .applyGradient(AppGradientBrush)
        )
        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = TitleGrey
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DescriptionGrey
            )
        }

        Spacer(Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Continuar",
            tint = Color.Gray,
            modifier = Modifier
                .size(36.dp)
                .applyGradient(AppGradientBrush)
        )
    }
}


@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PromotionCreationCardPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        PromotionCreationCard(
            icon = Icons.Filled.AddBox,
            title = "Crear Manualmente",
            description = "Diseña tu promoción, paso a paso, con total control sobre cada detalle.",
            onClick = {}
        )
        Spacer(Modifier.height(24.dp))
        PromotionCreationCard(
            icon = Icons.Default.AutoAwesome,
            title = "Generar con IA",
            description = "¿No sabes qué cupón crear? Deja que la IA lo haga por ti.",
            onClick = {}
        )
    }
}