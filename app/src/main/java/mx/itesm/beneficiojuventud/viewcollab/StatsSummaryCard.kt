package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.viewcollab.StatsSummary

private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val TextGrey = Color(0xFF616161)
private val TextGradient = Brush.horizontalGradient(listOf(DarkBlue, Teal))
private val GreenPositive = Color(0xFF4CAF50)
private val RedNegative = Color(0xFFE53935)

@Composable
fun StatsSummaryCard(summary: StatsSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = summary.redeemedCoupons, style = TextStyle(brush = TextGradient), fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(text = "Cupones Canjeados", fontSize = 12.sp, color = TextGrey, textAlign = TextAlign.Center)
                Text(text = "este mes", fontSize = 10.sp, color = TextGrey, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(8.dp))
            // vs. Mes Anterior
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = summary.monthlyChange, color = GreenPositive, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(text = "vs. Mes Anterior", fontSize = 12.sp, color = TextGrey, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = summary.conversionRate, color = RedNegative, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(text = "Tasa de Conversión", fontSize = 12.sp, color = TextGrey, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}