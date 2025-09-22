package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
        Text(
            "  O  ",
            style = TextStyle(
                fontSize = 14.dp.value.sp, // o 14.sp si prefieres importar directamente
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD9D9D9)
            )
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF008D96), Color(0xFF4B4C7E))
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}