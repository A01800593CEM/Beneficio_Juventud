package mx.itesm.beneficiojuventud.model.analytics

import com.google.gson.annotations.SerializedName

// Main response from backend
data class AnalyticsDashboard(
    val metadata: Map<String, Any>,
    val summary: AnalyticsSummary,
    val charts: Map<String, Any>,  // Changed to Any to avoid deserialization issues
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

// Backend response model where x is a date string
// Server guarantees x and y are never null
data class BackendChartEntry(
    val x: String,  // Date string from backend (e.g., "2025-01-15"), never null
    val y: Int      // Count value, never null (defaults to 0 on server if no data)
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
    val promotionId: Int? = null
)

data class BarChartData(
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    val entries: List<BarChartEntry>? = null,
    val xAxisLabel: String? = null,
    val yAxisLabel: String? = null
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

// Backend response models
data class BackendAnalyticsChart(
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    val entries: List<BackendChartEntry>? = null,
    val xAxisLabel: String? = null,
    val yAxisLabel: String? = null,
    val minY: Int? = null,
    val maxY: Int? = null
)

data class BackendSeriesData(
    val seriesId: String? = null,
    val seriesLabel: String? = null,
    val entries: List<BackendChartEntry>? = null
)

data class BackendMultiSeriesLineChartData(
    val type: String? = null,
    val title: String? = null,
    val description: String? = null,
    val series: List<BackendSeriesData>? = null,
    val xAxisLabel: String? = null,
    val yAxisLabel: String? = null
)

// Helper function to convert backend date string to a short label
// Adapts format based on time range:
// - week: "Lun 15", "Mar 16" (day name + day number)
// - month: "15/1", "16/1" (day/month)
// - year: "Ene", "Feb" (month name)
fun formatDateLabel(dateString: String?, timeRange: String? = "month"): String {
    // Handle null inputs
    if (dateString == null || dateString.isEmpty()) {
        return ""
    }

    val safeTimeRange = timeRange?.lowercase() ?: "month"

    return try {
        // Expected format: "2025-01-15" (YYYY-MM-DD)
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val year = parts[0].toIntOrNull() ?: 2025
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1

            when (safeTimeRange) {
                "week" -> {
                    // Calculate day of week (simplified)
                    val dayNames = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(year, month - 1, day)
                    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
                    "${dayNames[dayOfWeek]} $day"
                }
                "year" -> {
                    // Return month name only
                    val monthNames = listOf(
                        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
                    )
                    monthNames.getOrNull(month - 1) ?: month.toString()
                }
                "month" -> {
                    // Return day/month format
                    "$day/$month"
                }
                else -> "$day/$month"
            }
        } else {
            dateString
        }
    } catch (e: Exception) {
        android.util.Log.e("AnalyticsModels", "Error formatting date label: $dateString", e)
        dateString
    }
}