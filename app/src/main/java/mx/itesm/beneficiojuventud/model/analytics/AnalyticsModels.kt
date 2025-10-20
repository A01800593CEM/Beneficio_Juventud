package mx.itesm.beneficiojuventud.model.analytics

import com.google.gson.annotations.SerializedName

// Main response from backend
data class AnalyticsDashboard(
    val metadata: Map<String, Any>,
    val summary: AnalyticsSummary,
    val charts: Map<String, AnalyticsChart>,
    val promotionStats: List<PromotionStatItem>
)

data class AnalyticsSummary(
    val totalPromotions: Int,
    val activePromotions: Int,
    val totalBookings: Int,
    val redeemedCoupons: Int,
    val totalFavorites: Int,
    val conversionRate: String
)

data class AnalyticsChart(
    val type: String,
    val title: String,
    val description: String,
    val entries: List<ChartEntry>,
    val xAxisLabel: String,
    val yAxisLabel: String,
    val minY: Int,
    val maxY: Int
)

data class ChartEntry(
    val x: Int,
    val y: Int,
    val xLabel: String? = null  // Optional label for x-axis (e.g., dates)
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

// Promotion Analytics Response
data class PromotionAnalyticsResponse(
    val metadata: Map<String, Any>,
    val summary: PromotionAnalyticsSummary,
    val promotions: List<PromotionAnalytics>
)

data class PromotionAnalyticsSummary(
    val totalPromotions: Int,
    val activePromotions: Int,
    val totalRedemptions: Int,
    val totalBookings: Int
)

data class PromotionAnalytics(
    val promotionId: Int,
    val title: String,
    val description: String,
    val type: String,
    val status: String,
    val dateRange: DateRange,
    val performance: PromotionPerformance
)

data class DateRange(
    val startDate: String,
    val endDate: String
)

data class PromotionPerformance(
    val totalStock: Int,
    val availableStock: Int,
    val usedStock: Int,
    val redeemedCount: Int,
    val bookingCount: Int,
    val conversionRate: String,
    val redemptionPercentage: String
)

// New models for bar chart and multi-series line chart
data class BarChartEntry(
    val label: String,
    val value: Int,
    val promotionId: Int?
)

data class BarChartData(
    val type: String,
    val title: String,
    val description: String,
    val entries: List<BarChartEntry>,
    val xAxisLabel: String,
    val yAxisLabel: String
)

data class MultiSeriesLineChartData(
    val type: String,
    val title: String,
    val description: String,
    val series: List<SeriesData>,
    val xAxisLabel: String,
    val yAxisLabel: String
)

data class SeriesData(
    val seriesId: String,
    val seriesLabel: String,
    val entries: List<ChartEntry>
)