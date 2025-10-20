package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entriesOf
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

// ========== COLOR CONSTANTS ==========
private val PrimaryPurple = Color(0xFF4B4C7E)
private val PrimaryTeal = Color(0xFF008D96)
private val TextGrey = Color(0xFF616161)
private val LightGrey = Color(0xFFF0F0F0)
private val DarkGrey = Color(0xFF7D7A7A)
private val White = Color.White

// ========== DATA MODELS ==========
data class VicoChartEntry(
    val x: Int,
    val y: Int
)

data class PromotionStatItem(
    val promotionId: Int,
    val title: String,
    val type: String,
    val status: String,
    val stockRemaining: Int,
    val totalStock: Int,
    val stockUtilization: String
)

data class AnalyticsSummary(
    val totalPromotions: Int,
    val activePromotions: Int,
    val totalBookings: Int,
    val redeemedCoupons: Int,
    val conversionRate: String,
    val totalRevenueImpact: String
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    nav: NavHostController,
    collaboratorId: String,
    viewModel: StatsViewModel = viewModel()
) {

//    val uiState by viewModel.uiState.collectAsState()

     LaunchedEffect(collaboratorId) {
         viewModel.loadAnalytics(collaboratorId, "month")
     }

    // Temporary demo state (remove once ViewModel is integrated)
    val uiState by remember {
        mutableStateOf(
            StatsUiState(
                isLoading = false,
                summary = AnalyticsSummary(
                    totalPromotions = 6,
                    activePromotions = 4,
                    totalBookings = 145,
                    redeemedCoupons = 89,
                    conversionRate = "61.38%",
                    totalRevenueImpact = "$4450.00"
                ),
                redemptionEntries = listOf(5, 12, 8, 15, 10, 18, 20, 16, 14, 22, 19, 25),
                bookingEntries = listOf(8, 14, 11, 17, 13, 20, 23, 19, 16, 24, 21, 28),
                promotionStats = listOf(
                    PromotionStatItem(1, "20% Descuento", "descuento", "activa", 50, 100, "50.00"),
                    PromotionStatItem(2, "2x1 Bebidas", "multicompra", "activa", 20, 100, "80.00")
                ),
                selectedTimeRange = "month"
            )
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle errors with snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        bottomBar = {
            BJBottomBarCollab(nav = nav)
        }
    ) { innerPadding ->
        val bottomInset = WindowInsets.navigationBars
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues()
            .calculateBottomPadding()
        val bottomBarHeight = 68.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            } else {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = bottomBarHeight + bottomInset),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.logo_beneficio_joven),
                        contentDescription = "Logo de la App",
                        modifier = Modifier.size(width = 45.dp, height = 33.dp)
                    )
                    Spacer(Modifier.height(24.dp))

                    StatsScreenHeader(nav = nav)
                    Spacer(Modifier.height(16.dp))

                    // Time Range Selector
                    TimeRangeSelector(
                        selectedTimeRange = uiState.selectedTimeRange,
                        onTimeRangeSelected = { newTimeRange ->
                             viewModel.changeTimeRange(collaboratorId, newTimeRange)
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Summary Cards
                    uiState.summary?.let { summary ->
                        StatsSummaryCard(summary = summary)
                        Spacer(Modifier.height(16.dp))

                        // Promotions Stats
                        if (uiState.promotionStats.isNotEmpty()) {
                            PromotionStatsCard(promotions = uiState.promotionStats)
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    // Redemption Trends Chart
                    StatsChartCard(
                        title = "Canjes Diarios",
                        description = "Cupones canjeados por día",
                        chartEntries = uiState.redemptionEntries
                    )
                    Spacer(Modifier.height(16.dp))

                    // Booking Trends Chart
                    StatsChartCard(
                        title = "Reservciones Diarias",
                        description = "Cupones reservados por día",
                        chartEntries = uiState.bookingEntries
                    )
                    Spacer(Modifier.height(16.dp))


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
            IconButton(onClick = { /* TODO: Settings */ }) {
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
private fun TimeRangeSelector(
    selectedTimeRange: String,
    onTimeRangeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(LightGrey, shape = RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("week", "month", "year").forEach { timeRange ->
            val isSelected = selectedTimeRange == timeRange
            Button(
                onClick = { onTimeRangeSelected(timeRange) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) PrimaryTeal else Color.Transparent,
                    contentColor = if (isSelected) White else TextGrey
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = when (timeRange) {
                        "week" -> "Semana"
                        "month" -> "Mes"
                        "year" -> "Año"
                        else -> ""
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatsChartCard(
    title: String,
    description: String,
    chartEntries: List<Int>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGrey
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = DarkGrey,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Convert chart entries to Vico format
            val modelProducer = remember(chartEntries) {
                ChartEntryModelProducer(
                    entriesOf(*chartEntries.mapIndexed { index, value ->
                        index.toFloat() to value.toFloat()
                    }.toTypedArray())
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Chart(
                    chart = lineChart(),
                    chartModelProducer = modelProducer,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFFFAFAFA))
                )
            }
        }
    }
}

@Composable
private fun StatsSummaryCard(summary: AnalyticsSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resumen",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGrey,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Promociones",
                    value = "${summary.totalPromotions}",
                    subvalue = "${summary.activePromotions} activas",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SummaryItem(
                    label = "Reservas",
                    value = "${summary.totalBookings}",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Canjeados",
                    value = "${summary.redeemedCoupons}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SummaryItem(
                    label = "Conversión",
                    value = summary.conversionRate,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Impacto de Ingresos",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextGrey
                    )
                    Text(
                        text = summary.totalRevenueImpact,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    subvalue: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFFAFAFA), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = DarkGrey,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTeal
        )
        if (subvalue != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subvalue,
                fontSize = 10.sp,
                color = DarkGrey
            )
        }
    }
}

@Composable
private fun PromotionStatsCard(promotions: List<PromotionStatItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Promociones Activas",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGrey,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            promotions.forEach { promo ->
                PromotionStatItemRow(promo = promo)
                if (promotions.indexOf(promo) < promotions.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = LightGrey, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun PromotionStatItemRow(promo: PromotionStatItem) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = promo.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextGrey
                )
                Text(
                    text = promo.type,
                    fontSize = 11.sp,
                    color = DarkGrey
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        color = if (promo.status == "activa") Color(0xFFE8F5E9) else LightGrey,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = promo.status.replaceFirstChar { it.uppercase() },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (promo.status == "activa") Color(0xFF2E7D32) else TextGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stock progress bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Stock",
                    fontSize = 11.sp,
                    color = DarkGrey
                )
                Text(
                    text = "${promo.stockRemaining}/${promo.totalStock}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGrey
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (promo.totalStock - promo.stockRemaining).toFloat() / promo.totalStock },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = PrimaryTeal,
                trackColor = LightGrey,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${promo.stockUtilization}% utilizado",
                fontSize = 10.sp,
                color = DarkGrey
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun StatsScreenPreview() {
    BeneficioJuventudTheme {
        StatsScreen(nav = rememberNavController(), collaboratorId = "test-collab-id")
    }
}