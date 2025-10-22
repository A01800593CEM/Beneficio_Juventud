package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.itesm.beneficiojuventud.model.analytics.AnalyticsDashboard
import mx.itesm.beneficiojuventud.model.analytics.BarChartData
import mx.itesm.beneficiojuventud.model.analytics.BarChartEntry
import mx.itesm.beneficiojuventud.model.analytics.MultiSeriesLineChartData
import mx.itesm.beneficiojuventud.model.analytics.RemoteServiceAnalytics
import mx.itesm.beneficiojuventud.viewcollab.AnalyticsSummary
import mx.itesm.beneficiojuventud.viewcollab.PromotionStatItem

/**
 * UI State for Stats/Analytics screen
 */
data class StatsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val summary: AnalyticsSummary? = null,
    val redemptionEntries: List<Int> = emptyList(),
    val bookingEntries: List<Int> = emptyList(),
    val promotionStats: List<PromotionStatItem> = emptyList(),
    val topRedeemedCoupons: List<BarChartEntry> = emptyList(),
    val redemptionTrendsByPromotion: MultiSeriesLineChartData? = null,
    val error: String? = null,
    val selectedTimeRange: String = "month",
    val dashboard: AnalyticsDashboard? = null
)

/**
 * ViewModel for collaborator analytics dashboard
 * Handles fetching and managing analytics data from backend
 */
class StatsViewModel : ViewModel() {

    private val remoteService = RemoteServiceAnalytics

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    /**
     * Load analytics for the collaborator
     * @param collaboratorId The collaborator's cognito ID
     * @param timeRange One of: "week", "month", "year"
     */
    fun loadAnalytics(collaboratorId: String, timeRange: String = "month") {
        viewModelScope.launch {
            try {
                android.util.Log.d("StatsViewModel", "Loading analytics for collaborator: $collaboratorId, timeRange: $timeRange")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    selectedTimeRange = timeRange
                )

                val dashboard = withContext(Dispatchers.IO) {
                    remoteService.getCollaboratorDashboard(collaboratorId, timeRange)
                }

                android.util.Log.d("StatsViewModel", "Dashboard loaded successfully")
                android.util.Log.d("StatsViewModel", "Summary - Promotions: ${dashboard.summary.totalPromotions}, " +
                        "Bookings: ${dashboard.summary.totalBookings}, " +
                        "Redeemed: ${dashboard.summary.redeemedCoupons}")
                android.util.Log.d("StatsViewModel", "Charts available: ${dashboard.charts.keys}")

                // Parse the charts from dashboard
                val gson = Gson()

                // Parse redemption entries (daily redemptions line chart)
                val redemptionEntries = try {
                    val chartData = dashboard.charts["redemptionTrends"]
                    val json = gson.toJson(chartData)
                    val analyticsChart = gson.fromJson(json, mx.itesm.beneficiojuventud.model.analytics.AnalyticsChart::class.java)
                    analyticsChart.entries.map { it.y }
                } catch (e: Exception) {
                    // Return a list with at least one point to avoid crash
                    listOf(0)
                }

                // Parse booking entries (daily bookings line chart)
                val bookingEntries = try {
                    val chartData = dashboard.charts["bookingTrends"]
                    val json = gson.toJson(chartData)
                    val analyticsChart = gson.fromJson(json, mx.itesm.beneficiojuventud.model.analytics.AnalyticsChart::class.java)
                    analyticsChart.entries.map { it.y }
                } catch (e: Exception) {
                    // Return a list with at least one point to avoid crash
                    listOf(0)
                }

                // Parse bar chart data for top redeemed coupons
                val topRedeemedCoupons = try {
                    val chartData = dashboard.charts["topRedeemedCoupons"]
                    val json = gson.toJson(chartData)
                    val barChartData = gson.fromJson(json, BarChartData::class.java)
                    barChartData.entries
                } catch (e: Exception) {
                    emptyList()
                }

                // Parse multi-series line chart data
                val redemptionTrendsByPromotion = try {
                    val chartData = dashboard.charts["redemptionTrendsByPromotion"]
                    val json = gson.toJson(chartData)
                    gson.fromJson(json, MultiSeriesLineChartData::class.java)
                } catch (e: Exception) {
                    null
                }

                // Parse promotionStats from nested structure: charts.promotionStats.data
                val promotionStats = try {
                    val chartData = dashboard.charts["promotionStats"]
                    val json = gson.toJson(chartData)
                    val promotionStatsChart = gson.fromJson(json, mx.itesm.beneficiojuventud.model.analytics.PromotionStatsChart::class.java)
                    promotionStatsChart.data
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error parsing promotionStats", e)
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dashboard = dashboard,
                    summary = AnalyticsSummary(
                        totalPromotions = dashboard.summary.totalPromotions,
                        activePromotions = dashboard.summary.activePromotions,
                        totalBookings = dashboard.summary.totalBookings,
                        redeemedCoupons = dashboard.summary.redeemedCoupons,
                        totalFavorites = dashboard.summary.totalFavorites,
                        conversionRate = dashboard.summary.conversionRate
                    ),
                    promotionStats = promotionStats.map { promo ->
                        PromotionStatItem(
                            promotionId = promo.promotionId,
                            title = promo.title,
                            type = promo.type,
                            status = promo.status,
                            stockRemaining = promo.stockRemaining,
                            totalStock = promo.totalStock,
                            stockUtilization = promo.stockUtilization
                        )
                    },
                    redemptionEntries = redemptionEntries,
                    bookingEntries = bookingEntries,
                    topRedeemedCoupons = topRedeemedCoupons,
                    redemptionTrendsByPromotion = redemptionTrendsByPromotion
                )
            } catch (e: Exception) {
                android.util.Log.e("StatsViewModel", "Error loading analytics", e)
                android.util.Log.e("StatsViewModel", "Error type: ${e.javaClass.simpleName}")
                android.util.Log.e("StatsViewModel", "Error message: ${e.message}")

                val errorMessage = when {
                    e is java.net.UnknownHostException || e.message?.contains("Unable to resolve host") == true ->
                        "Sin conexión a internet. Por favor verifica tu conexión."
                    e is java.net.SocketTimeoutException || e.message?.contains("timeout") == true ->
                        "Tiempo de espera agotado. Por favor intenta de nuevo."
                    e is java.net.ConnectException || e.message?.contains("Failed to connect") == true ->
                        "No se pudo conectar al servidor. Verifica tu conexión a internet."
                    else -> e.message ?: "Error desconocido al cargar estadísticas"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    /**
     * Refresh analytics for the current collaborator and time range
     * @param collaboratorId The collaborator's cognito ID
     */
    fun refreshAnalytics(collaboratorId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = true,
                    error = null
                )

                val dashboard = withContext(Dispatchers.IO) {
                    remoteService.getCollaboratorDashboard(
                        collaboratorId,
                        _uiState.value.selectedTimeRange
                    )
                }

                // Parse the charts from dashboard
                val gson = Gson()

                // Parse redemption entries (daily redemptions line chart)
                val redemptionEntries = try {
                    val chartData = dashboard.charts["redemptionTrends"]
                    val json = gson.toJson(chartData)
                    val analyticsChart = gson.fromJson(json, mx.itesm.beneficiojuventud.model.analytics.AnalyticsChart::class.java)
                    analyticsChart.entries.map { it.y }
                } catch (e: Exception) {
                    // Return a list with at least one point to avoid crash
                    listOf(0)
                }

                // Parse booking entries (daily bookings line chart)
                val bookingEntries = try {
                    val chartData = dashboard.charts["bookingTrends"]
                    val json = gson.toJson(chartData)
                    val analyticsChart = gson.fromJson(json, mx.itesm.beneficiojuventud.model.analytics.AnalyticsChart::class.java)
                    analyticsChart.entries.map { it.y }
                } catch (e: Exception) {
                    // Return a list with at least one point to avoid crash
                    listOf(0)
                }

                // Parse bar chart data for top redeemed coupons
                val topRedeemedCoupons = try {
                    val chartData = dashboard.charts["topRedeemedCoupons"]
                    val json = gson.toJson(chartData)
                    val barChartData = gson.fromJson(json, BarChartData::class.java)
                    barChartData.entries
                } catch (e: Exception) {
                    emptyList()
                }

                // Parse multi-series line chart data
                val redemptionTrendsByPromotion = try {
                    val chartData = dashboard.charts["redemptionTrendsByPromotion"]
                    val json = gson.toJson(chartData)
                    gson.fromJson(json, MultiSeriesLineChartData::class.java)
                } catch (e: Exception) {
                    null
                }

                // Parse promotionStats from nested structure: charts.promotionStats.data
                val promotionStats = try {
                    val chartData = dashboard.charts["promotionStats"]
                    val json = gson.toJson(chartData)
                    val promotionStatsChart = gson.fromJson(json, mx.itesm.beneficiojuventud.model.analytics.PromotionStatsChart::class.java)
                    promotionStatsChart.data
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error parsing promotionStats during refresh", e)
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    dashboard = dashboard,
                    summary = AnalyticsSummary(
                        totalPromotions = dashboard.summary.totalPromotions,
                        activePromotions = dashboard.summary.activePromotions,
                        totalBookings = dashboard.summary.totalBookings,
                        redeemedCoupons = dashboard.summary.redeemedCoupons,
                        totalFavorites = dashboard.summary.totalFavorites,
                        conversionRate = dashboard.summary.conversionRate
                    ),
                    promotionStats = promotionStats.map { promo ->
                        PromotionStatItem(
                            promotionId = promo.promotionId,
                            title = promo.title,
                            type = promo.type,
                            status = promo.status,
                            stockRemaining = promo.stockRemaining,
                            totalStock = promo.totalStock,
                            stockUtilization = promo.stockUtilization
                        )
                    },
                    redemptionEntries = redemptionEntries,
                    bookingEntries = bookingEntries,
                    topRedeemedCoupons = topRedeemedCoupons,
                    redemptionTrendsByPromotion = redemptionTrendsByPromotion
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e is java.net.UnknownHostException || e.message?.contains("Unable to resolve host") == true ->
                        "Sin conexión a internet. Por favor verifica tu conexión."
                    e is java.net.SocketTimeoutException || e.message?.contains("timeout") == true ->
                        "Tiempo de espera agotado. Por favor intenta de nuevo."
                    e is java.net.ConnectException || e.message?.contains("Failed to connect") == true ->
                        "No se pudo conectar al servidor. Verifica tu conexión a internet."
                    else -> e.message ?: "Error al actualizar estadísticas"
                }

                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = errorMessage
                )
            }
        }
    }

    /**
     * Change the time range and reload analytics
     * @param collaboratorId The collaborator's cognito ID
     * @param timeRange One of: "week", "month", "year"
     */
    fun changeTimeRange(collaboratorId: String, timeRange: String) {
        loadAnalytics(collaboratorId, timeRange)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}