// Este es de Prueba

package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ehsanarmani.yolocharts.piechart.PieChart
import com.ehsanarmani.yolocharts.piechart.model.PieData
import ir.ehsannarmani.compose_charts.PieChart

@Composable
fun TestEhsanarmaniChart() {
    val data = listOf(
        PieData(value = 20f, color = Color.Red, label = "Android"),
        PieData(value = 45f, color = Color.Cyan, label = "Windows"),
        PieData(value = 35f, color = Color.Gray, label = "Linux")
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            modifier = Modifier.size(200.dp),
            pieData = data,
            holeRadius = 0.6f
        )
    }
}

@Composable
fun PieData(value: Float, color: Color, label: String) {
    TODO("Not yet implemented")
}

@Preview(showBackground = true)
@Composable
fun TestEhsanarmaniChartPreview() {
    TestEhsanarmaniChart()
}