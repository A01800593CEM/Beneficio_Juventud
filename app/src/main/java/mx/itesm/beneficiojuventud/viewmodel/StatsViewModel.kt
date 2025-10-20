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
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    selectedTimeRange = timeRange
                )

                val dashboard = withContext(Dispatchers.IO) {
                    remoteService.getCollaboratorDashboard(collaboratorId, timeRange)
                }

                // Parse the charts from dashboard
                val gson = Gson()

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
                    promotionStats = dashboard.promotionStats.map { promo ->
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
                    topRedeemedCoupons = topRedeemedCoupons,
                    redemptionTrendsByPromotion = redemptionTrendsByPromotion
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error loading analytics"
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
                    promotionStats = dashboard.promotionStats.map { promo ->
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
                    topRedeemedCoupons = topRedeemedCoupons,
                    redemptionTrendsByPromotion = redemptionTrendsByPromotion
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Error refreshing analytics"
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