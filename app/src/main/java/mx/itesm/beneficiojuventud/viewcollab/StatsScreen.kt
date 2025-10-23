package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.remember
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.utils.DataUtils
import mx.itesm.beneficiojuventud.model.analytics.BarChartEntry
import mx.itesm.beneficiojuventud.model.analytics.MultiSeriesLineChartData
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.StatsUiState
import mx.itesm.beneficiojuventud.viewmodel.StatsViewModel

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
    val totalFavorites: Int,
    val conversionRate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    nav: NavHostController,
    collaboratorId: String,
    viewModel: StatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(collaboratorId) {
        viewModel.loadAnalytics(collaboratorId, "month")
    }

    // New composable that receives the state
    StatsScreenContent(
        nav = nav,
        uiState = uiState,
        onTimeRangeSelected = { newTimeRange ->
            viewModel.changeTimeRange(collaboratorId, newTimeRange)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreenContent(
    nav: NavHostController,
    uiState: StatsUiState,
    onTimeRangeSelected: (String) -> Unit
) {


    // Fallback demo state for preview/testing (when backend is unavailable)
    val fallbackUiState by remember {
        mutableStateOf(
            StatsUiState(
                isLoading = false,
                summary = AnalyticsSummary(
                    totalPromotions = 6,
                    activePromotions = 4,
                    totalBookings = 145,
                    redeemedCoupons = 89,
                    totalFavorites = 42,
                    conversionRate = "61.38%"
                ),
                redemptionEntries = listOf(5, 12, 8, 15, 10, 18, 20, 16, 14, 22, 19, 25),
                bookingEntries = listOf(8, 14, 11, 17, 13, 20, 23, 19, 16, 24, 21, 28),
                promotionStats = listOf(
                    PromotionStatItem(1, "20% Descuento", "descuento", "activa", 50, 100, "50.00"),
                    PromotionStatItem(2, "2x1 Bebidas", "multicompra", "activa", 20, 100, "80.00")
                ),
                // NEW: Demo data for top redeemed coupons bar chart
                topRedeemedCoupons = listOf(
                    BarChartEntry(label = "50% Pizza", value = 45, promotionId = 1),
                    BarChartEntry(label = "Café Gratis", value = 38, promotionId = 2),
                    BarChartEntry(label = "2x1 Hamburguesa", value = 32, promotionId = 3),
                    BarChartEntry(label = "20% Ropa", value = 28, promotionId = 4),
                    BarChartEntry(label = "Entrada Gratis Cine", value = 22, promotionId = 5)
                ),
                // NEW: Demo data for multi-series line chart with date labels
                redemptionTrendsByPromotion = MultiSeriesLineChartData(
                    type = "multiline",
                    title = "Canjes por Cupón en el Tiempo",
                    description = "Tendencias de los top 5 cupones",
                    series = listOf(
                        mx.itesm.beneficiojuventud.model.analytics.SeriesData(
                            seriesId = "promo_1",
                            seriesLabel = "50% Pizza",
                            entries = listOf(
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(0, 3, "Lun"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(1, 5, "Mar"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(2, 4, "Mié"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(3, 7, "Jue"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(4, 6, "Vie"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(5, 8, "Sáb"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(6, 12, "Dom")
                            )
                        ),
                        mx.itesm.beneficiojuventud.model.analytics.SeriesData(
                            seriesId = "promo_2",
                            seriesLabel = "Café Gratis",
                            entries = listOf(
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(0, 2, "Lun"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(1, 4, "Mar"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(2, 3, "Mié"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(3, 6, "Jue"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(4, 5, "Vie"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(5, 9, "Sáb"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(6, 9, "Dom")
                            )
                        ),
                        mx.itesm.beneficiojuventud.model.analytics.SeriesData(
                            seriesId = "promo_3",
                            seriesLabel = "2x1 Hamburguesa",
                            entries = listOf(
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(0, 1, "Lun"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(1, 3, "Mar"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(2, 2, "Mié"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(3, 4, "Jue"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(4, 5, "Vie"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(5, 7, "Sáb"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(6, 10, "Dom")
                            )
                        ),
                        mx.itesm.beneficiojuventud.model.analytics.SeriesData(
                            seriesId = "promo_4",
                            seriesLabel = "20% Ropa",
                            entries = listOf(
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(0, 2, "Lun"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(1, 2, "Mar"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(2, 3, "Mié"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(3, 5, "Jue"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(4, 4, "Vie"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(5, 6, "Sáb"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(6, 6, "Dom")
                            )
                        ),
                        mx.itesm.beneficiojuventud.model.analytics.SeriesData(
                            seriesId = "promo_5",
                            seriesLabel = "Entrada Gratis Cine",
                            entries = listOf(
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(0, 1, "Lun"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(1, 2, "Mar"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(2, 2, "Mié"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(3, 3, "Jue"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(4, 4, "Vie"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(5, 5, "Sáb"),
                                mx.itesm.beneficiojuventud.model.analytics.ChartEntry(6, 5, "Dom")
                            )
                        )
                    ),
                    xAxisLabel = "Días",
                    yAxisLabel = "Canjes"
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
                        onTimeRangeSelected = onTimeRangeSelected
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
                    if (uiState.redemptionEntries.isNotEmpty()) {
                        StatsChartCard(
                            title = "Canjes Diarios",
                            description = "Cupones canjeados por día",
                            chartEntries = uiState.redemptionEntries
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // Booking Trends Chart
                    if (uiState.bookingEntries.isNotEmpty()) {
                        StatsChartCard(
                            title = "Reservciones Diarias",
                            description = "Cupones reservados por día",
                            chartEntries = uiState.bookingEntries
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // Top Redeemed Coupons Bar Chart
                    if (uiState.topRedeemedCoupons.isNotEmpty()) {
                        TopRedeemedCouponsChart(
                            topCoupons = uiState.topRedeemedCoupons
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // Multi-Series Line Chart for Redemptions by Promotion
                    val chartData = uiState.redemptionTrendsByPromotion
                    val hasValidData = chartData?.series?.isNotEmpty() == true &&
                            chartData.series.any { it.entries.isNotEmpty() }

                    if (hasValidData) {
                        MultiSeriesLineChartCard(chartData = chartData)
                        Spacer(Modifier.height(16.dp))
                    } else {
                        // Fallback to demo data when no real data
                        MultiSeriesLineChartCard(chartData = fallbackUiState.redemptionTrendsByPromotion)
                        Spacer(Modifier.height(16.dp))
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
    // Early return if no data
    if (chartEntries.isEmpty()) {
        return
    }

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

            // Convert chart entries to YCharts format
            val pointsData = chartEntries.mapIndexed { index, value ->
                Point(index.toFloat(), value.toFloat())
            }

            // Ensure we have at least 1 step for xAxis
            val xSteps = maxOf(chartEntries.size - 1, 1)

            val xAxisData = AxisData.Builder()
                .axisStepSize(40.dp)
                .steps(xSteps)
                .bottomPadding(8.dp)
                .axisOffset(16.dp)
                .labelData { index -> index.toString() }
                .build()

            val maxValue = chartEntries.maxOrNull() ?: 1
            val yAxisData = AxisData.Builder()
                .steps(5)
                .labelAndAxisLinePadding(20.dp)
                .axisOffset(16.dp)
                .labelData { index ->
                    ((maxValue / 5.0) * index).toInt().toString()
                }
                .build()

            val lineChartData = LineChartData(
                linePlotData = LinePlotData(
                    lines = listOf(
                        Line(
                            dataPoints = pointsData,
                            lineStyle = LineStyle(color = PrimaryTeal),
                            intersectionPoint = IntersectionPoint(color = PrimaryTeal),
                            selectionHighlightPoint = SelectionHighlightPoint(color = PrimaryTeal),
                            shadowUnderLine = ShadowUnderLine(color = PrimaryTeal.copy(alpha = 0.3f)),
                            selectionHighlightPopUp = SelectionHighlightPopUp()
                        )
                    )
                ),
                xAxisData = xAxisData,
                yAxisData = yAxisData,
                backgroundColor = Color(0xFFFAFAFA)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                LineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    lineChartData = lineChartData
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Favoritos",
                    value = "${summary.totalFavorites}",
                    modifier = Modifier.weight(1f)
                )
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

@Composable
private fun TopRedeemedCouponsChart(
    topCoupons: List<BarChartEntry>
) {
    // Early return if no data
    if (topCoupons.isEmpty()) {
        return
    }

    // Define colors for each bar
    val barColors = listOf(
        Color(0xFF6200EE),  // Purple
        Color(0xFF03DAC5),  // Teal
        Color(0xFFFF6B6B),  // Red
        Color(0xFFFFB74D),  // Orange
        Color(0xFF4FC3F7)   // Blue
    )

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
                text = "Top 5 Cupones Más Canjeados",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGrey
            )
            Text(
                text = "Cupones más populares por número de canjes",
                fontSize = 12.sp,
                color = DarkGrey,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))



            // Convert to YCharts BarChart format with custom colors
            val barData = topCoupons.mapIndexed { index, entry ->
                BarData(
                    point = Point(index.toFloat(), entry.value.toFloat()),
                    color = barColors[index % barColors.size],
                    label = (index + 1).toString()
                )
            }

            val paddedData = listOf(
                BarData(Point(-1f, 0f), Color.Transparent, ""), // ghost left
            ) + barData + listOf(
                BarData(Point(barData.size.toFloat(), 0f), Color.Transparent, "") // ghost right
            )


            val xAxisData = AxisData.Builder()
                .axisStepSize(40.dp)
                .steps(paddedData.size - 1)
                .bottomPadding(8.dp)
                .axisOffset(16.dp)
                .labelData { index ->
                    // Skip the ghost bar at position 0 (which is at x=-1)
                    // Real bars are at indices 1, 2, 3, 4, 5
                    if (index >= 1 && index <= topCoupons.size) {
                        index.toString()
                    } else {
                        ""
                    }
                }
                .build()

            val maxValue = (topCoupons.maxOfOrNull { it.value } ?: 0)
            val yMax = ((maxValue + 9) / 10) * 10 // round up to nearest 10
            val yAxisData = AxisData.Builder()
                .steps(5)
                .labelAndAxisLinePadding(20.dp)
                .axisOffset(16.dp)
                .labelData { index ->
                    ((yMax / 5.0) * index).toInt().toString()
                }
                .build()


            val barChartData = BarChartData(
                chartData = paddedData,
                xAxisData = xAxisData,
                yAxisData = yAxisData,
                horizontalExtraSpace = 60.dp,  // extra spacing around bars
                backgroundColor = Color(0xFFFAFAFA)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                BarChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    barChartData = barChartData
                )
            }


            Spacer(modifier = Modifier.height(12.dp))

            // Legend showing coupon names with matching colors
            Column {
                topCoupons.forEachIndexed { index, entry ->
                    val barColor = barColors[index % barColors.size]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(barColor, shape = RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.label,
                            fontSize = 12.sp,
                            color = TextGrey,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${entry.value}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiSeriesLineChartCard(
    chartData: MultiSeriesLineChartData?
) {
    // Define colors for each line
    val lineColors = listOf(
        Color(0xFF6200EE),  // Purple
        Color(0xFF03DAC5),  // Teal
        Color(0xFFFF6B6B),  // Red
        Color(0xFFFFB74D),  // Orange
        Color(0xFF4FC3F7)   // Blue
    )
    if (chartData == null || chartData.series.isEmpty() || chartData.series.all { it.entries.isEmpty() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin datos para mostrar", color = Color.Gray, fontSize = 14.sp)
        }
        return
    }
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
                text = chartData.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGrey
            )
            Text(
                text = chartData.description,
                fontSize = 12.sp,
                color = DarkGrey,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Convert multi-series data to YCharts format
            // Filter out series with no entries to prevent crashes
            val lines = chartData.series
                .filter { it.entries.isNotEmpty() }
                .mapIndexed { index, series ->
                    Line(
                        dataPoints = series.entries.map { entry ->
                            Point(entry.x.toFloat(), entry.y.toFloat())
                        },
                        lineStyle = LineStyle(color = lineColors[index % lineColors.size]),
                        intersectionPoint = IntersectionPoint(color = lineColors[index % lineColors.size]),
                        selectionHighlightPoint = SelectionHighlightPoint(color = lineColors[index % lineColors.size]),
                        shadowUnderLine = ShadowUnderLine(
                            color = lineColors[index % lineColors.size].copy(alpha = 0.3f)
                        ),
                        selectionHighlightPopUp = SelectionHighlightPopUp()
                    )
                }

            // Show message if no valid lines, otherwise show chart
            if (lines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin datos para mostrar", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                val allEntries = chartData.series.flatMap { it.entries }
                val maxX = maxOf(allEntries.maxOfOrNull { it.x } ?: 1, 1)
                val maxY = maxOf(allEntries.maxOfOrNull { it.y } ?: 1, 1)

                // Use xLabel from first series if available, otherwise use numeric index
                val xLabels = chartData.series.firstOrNull()?.entries?.associate {
                    it.x to (it.xLabel ?: it.x.toString())
                } ?: emptyMap()

                val xAxisData = AxisData.Builder()
                    .axisStepSize(40.dp)
                    .steps(maxX)
                    .bottomPadding(8.dp)
                    .startPadding(20.dp)  // Add padding at the start
                    .endPadding(20.dp)    // Add padding at the end
                    .axisOffset(16.dp)
                    .labelData { index -> xLabels[index] ?: index.toString() }
                    .build()

                val yAxisData = AxisData.Builder()
                    .steps(5)
                    .labelAndAxisLinePadding(20.dp)
                    .axisOffset(16.dp)
                    .labelData { index ->
                        ((maxY / 5.0) * index).toInt().toString()
                    }
                    .build()

                val lineChartData = LineChartData(
                    linePlotData = LinePlotData(lines = lines),
                    xAxisData = xAxisData,
                    yAxisData = yAxisData,
                    backgroundColor = Color(0xFFFAFAFA)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LineChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        lineChartData = lineChartData
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legend for series with matching colors
                Column {
                    chartData.series
                        .filter { it.entries.isNotEmpty() }
                        .forEachIndexed { index, series ->
                            val lineColor = lineColors[index % lineColors.size]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(lineColor, shape = RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = series.seriesLabel,
                                    fontSize = 12.sp,
                                    color = TextGrey,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                }
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun StatsScreenPreview() {
    // The same fallback state you already defined
    val fallbackUiState = StatsUiState(
        isLoading = false,
        summary = AnalyticsSummary(
            totalPromotions = 6,
            activePromotions = 4,
            totalBookings = 145,
            redeemedCoupons = 89,
            totalFavorites = 42,
            conversionRate = "61.38%"
        ),
        redemptionEntries = listOf(5, 12, 8, 15, 10, 18, 20, 16, 14, 22, 19, 25),
        bookingEntries = listOf(8, 14, 11, 17, 13, 20, 23, 19, 16, 24, 21, 28),
        promotionStats = listOf(
            PromotionStatItem(1, "20% Descuento", "descuento", "activa", 50, 100, "50.00"),
            PromotionStatItem(2, "2x1 Bebidas", "multicompra", "activa", 20, 100, "80.00")
        ),
        topRedeemedCoupons = listOf(
            BarChartEntry(label = "50% Pizza", value = 45, promotionId = 1),
            BarChartEntry(label = "Café Gratis", value = 38, promotionId = 2)
        ),
        redemptionTrendsByPromotion = MultiSeriesLineChartData(
            type = "multiline",
            title = "Canjes por Cupón en el Tiempo",
            description = "Tendencias de los top 5 cupones",
            series = listOf(
                mx.itesm.beneficiojuventud.model.analytics.SeriesData(
                    seriesId = "promo_1",
                    seriesLabel = "50% Pizza",
                    entries = listOf(
                        mx.itesm.beneficiojuventud.model.analytics.ChartEntry(0, 3, "Lun")
                    )
                )
            ),
            xAxisLabel = "Días",
            yAxisLabel = "Canjes"
        ),
        selectedTimeRange = "month"
    )

    BeneficioJuventudTheme {
        // Call the UI-only composable with the fallback data
        StatsScreenContent(
            nav = rememberNavController(),
            uiState = fallbackUiState,
            onTimeRangeSelected = {} // No action needed in preview
        )
    }
}
