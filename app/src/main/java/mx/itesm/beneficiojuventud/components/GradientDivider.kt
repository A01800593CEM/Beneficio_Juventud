package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun GradientDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 2.dp,
    cornerRadius: Dp = 0.dp
) {
    // Gradiente fijo
    val brush = Brush.linearGradient(
        listOf(
            Color(0xFF4B4C7E),
            Color(0xFF008D96)
        )
    )

    Box(
        modifier = modifier
            .height(thickness)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun GradientDividerPreview() {
    MaterialTheme {
        GradientDivider()
    }
}
