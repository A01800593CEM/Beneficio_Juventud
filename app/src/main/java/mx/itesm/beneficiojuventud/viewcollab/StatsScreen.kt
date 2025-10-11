package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entriesOf

private val TextGrey = Color(0xFF616161)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    nav: NavHostController,
    viewModel: StatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf<BJTabCollab>(BJTabCollab.Stats) }

    Scaffold(
        bottomBar = {
            BJBottomBarCollab(
                selected = selectedTab,
                onSelect = { newTab -> selectedTab = newTab },
                onAddClick = { /* TODO */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de la App",
                modifier = Modifier.size(width = 45.dp, height = 33.dp)
            )
            Spacer(Modifier.height(24.dp))
            StatsScreenHeader(nav = nav)

            Spacer(Modifier.height(16.dp))

            // Producer del modelo para la gráfica (Vico 1.x)
            val modelProducer = remember {
                ChartEntryModelProducer(
                    entriesOf(1, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12, 0, 11, 12, 11))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 24.dp)
            ) {
                JetpackComposeBasicLineChart_1x(modelProducer)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F0F0))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    uiState.summary?.let { summary ->
                        StatsSummaryCard(summary = summary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsScreenHeader(nav: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = TextGrey
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Estadísticas",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = TextGrey
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Ajustes",
                    tint = TextGrey
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        GradientDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}

@Composable
fun JetpackComposeBasicLineChart_1x(modelProducer: ChartEntryModelProducer) {
    Chart(
        chart = lineChart(),
        chartModelProducer = modelProducer,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.White)
    )
}


@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun StatsScreenPreview() {
    BeneficioJuventudTheme {
        StatsScreen(nav = rememberNavController())
    }
}

