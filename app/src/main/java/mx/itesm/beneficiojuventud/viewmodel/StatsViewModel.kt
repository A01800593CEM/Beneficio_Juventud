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
import mx.itesm.beneficiojuventud.model.analytics.BackendAnalyticsChart
import mx.itesm.beneficiojuventud.model.analytics.BackendMultiSeriesLineChartData
import mx.itesm.beneficiojuventud.model.analytics.ChartEntry
import mx.itesm.beneficiojuventud.model.analytics.SeriesData
import mx.itesm.beneficiojuventud.model.analytics.formatDateLabel
import mx.itesm.beneficiojuventud.viewcollab.AnalyticsSummary
import mx.itesm.beneficiojuventud.viewcollab.PromotionStatItem

/**
 * UI State for Stats/Analytics screen
 */
data class StatsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val summary: AnalyticsSummary? = null,
    val redemptionEntries: List<ChartEntry> = emptyList(),
    val bookingEntries: List<ChartEntry> = emptyList(),
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

                android.util.Log.d("StatsViewModel", "Dashboard received successfully for timeRange: $timeRange")
                android.util.Log.d("StatsViewModel", "Charts keys: ${dashboard.charts.keys}")

                // Parse the charts from dashboard
                val gson = Gson()

                // Parse redemption entries (daily redemptions line chart)
                val redemptionEntries = try {
                    android.util.Log.d("StatsViewModel", "Parsing redemptionTrends...")
                    val chartData = dashboard.charts["redemptionTrends"]
                    if (chartData == null) {
                        android.util.Log.w("StatsViewModel", "redemptionTrends chart data is null")
                        emptyList()
                    } else {
                        val json = gson.toJson(chartData)
                        android.util.Log.d("StatsViewModel", "redemptionTrends JSON: $json")
                        val backendChart = gson.fromJson(json, BackendAnalyticsChart::class.java)
                        android.util.Log.d("StatsViewModel", "Parsed redemptionTrends, entries count: ${backendChart.entries?.size}")

                        // Convert backend entries (with date strings) to indexed entries with labels
                        backendChart.entries?.mapIndexed { index, entry ->
                            android.util.Log.d("StatsViewModel", "Processing redemption entry $index: x=${entry.x}, y=${entry.y}")
                            val label = formatDateLabel(entry.x, timeRange)
                            android.util.Log.d("StatsViewModel", "Formatted label: $label")
                            ChartEntry(
                                x = index,
                                y = entry.y,
                                xLabel = label
                            )
                        } ?: emptyList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error parsing redemption entries: ${e.message}", e)
                    e.printStackTrace()
                    emptyList()
                }

                // Parse booking entries (daily bookings line chart)
                val bookingEntries = try {
                    android.util.Log.d("StatsViewModel", "Parsing bookingTrends...")
                    val chartData = dashboard.charts["bookingTrends"]
                    if (chartData == null) {
                        android.util.Log.w("StatsViewModel", "bookingTrends chart data is null")
                        emptyList()
                    } else {
                        val json = gson.toJson(chartData)
                        android.util.Log.d("StatsViewModel", "bookingTrends JSON: $json")
                        val backendChart = gson.fromJson(json, BackendAnalyticsChart::class.java)
                        android.util.Log.d("StatsViewModel", "Parsed bookingTrends, entries count: ${backendChart.entries?.size}")

                        // Convert backend entries (with date strings) to indexed entries with labels
                        backendChart.entries?.mapIndexed { index, entry ->
                            android.util.Log.d("StatsViewModel", "Processing booking entry $index: x=${entry.x}, y=${entry.y}")
                            val label = formatDateLabel(entry.x, timeRange)
                            android.util.Log.d("StatsViewModel", "Formatted label: $label")
                            ChartEntry(
                                x = index,
                                y = entry.y,
                                xLabel = label
                            )
                        } ?: emptyList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error parsing booking entries: ${e.message}", e)
                    e.printStackTrace()
                    emptyList()
                }

                // Parse bar chart data for top redeemed coupons
                val topRedeemedCoupons = try {
                    val chartData = dashboard.charts["topRedeemedCoupons"]
                    if (chartData == null) {
                        emptyList()
                    } else {
                        val json = gson.toJson(chartData)
                        val barChartData = gson.fromJson(json, BarChartData::class.java)
                        barChartData.entries ?: emptyList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error parsing top redeemed coupons: ${e.message}", e)
                    emptyList()
                }

                // Parse multi-series line chart data
                val redemptionTrendsByPromotion = try {
                    android.util.Log.d("StatsViewModel", "Parsing redemptionTrendsByPromotion...")
                    val chartData = dashboard.charts["redemptionTrendsByPromotion"]
                    if (chartData == null) {
                        android.util.Log.w("StatsViewModel", "redemptionTrendsByPromotion chart data is null")
                        null
                    } else {
                        val json = gson.toJson(chartData)
                        android.util.Log.d("StatsViewModel", "redemptionTrendsByPromotion JSON: ${json.take(500)}")
                        val backendMultiSeries = gson.fromJson(json, BackendMultiSeriesLineChartData::class.java)
                        android.util.Log.d("StatsViewModel", "Parsed multiSeries, series count: ${backendMultiSeries.series?.size}")

                        // Convert to app model with indexed x values
                        if (backendMultiSeries.series != null) {
                            MultiSeriesLineChartData(
                                type = backendMultiSeries.type ?: "multiline",
                                title = backendMultiSeries.title ?: "",
                                description = backendMultiSeries.description ?: "",
                                series = backendMultiSeries.series.mapNotNull { backendSeries ->
                                    android.util.Log.d("StatsViewModel", "Processing series: ${backendSeries.seriesLabel}, entries: ${backendSeries.entries?.size}")
                                    if (backendSeries.seriesId != null && backendSeries.seriesLabel != null && backendSeries.entries != null) {
                                        SeriesData(
                                            seriesId = backendSeries.seriesId,
                                            seriesLabel = backendSeries.seriesLabel,
                                            entries = backendSeries.entries.mapIndexed { index, entry ->
                                                android.util.Log.d("StatsViewModel", "Series ${backendSeries.seriesLabel} entry $index: x=${entry.x}, y=${entry.y}")
                                                val label = formatDateLabel(entry.x, timeRange)
                                                android.util.Log.d("StatsViewModel", "Formatted label: $label")
                                                ChartEntry(
                                                    x = index,
                                                    y = entry.y,
                                                    xLabel = label
                                                )
                                            }
                                        )
                                    } else {
                                        android.util.Log.w("StatsViewModel", "Skipping series with null fields")
                                        null
                                    }
                                },
                                xAxisLabel = backendMultiSeries.xAxisLabel ?: "Date",
                                yAxisLabel = backendMultiSeries.yAxisLabel ?: "Count"
                            )
                        } else null
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error parsing multi-series chart: ${e.message}", e)
                    e.printStackTrace()
                    null
                }

                android.util.Log.d("StatsViewModel", "Successfully parsed all charts, updating UI state")

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
                    promotionStats = dashboard.promotionStats?.map { promo ->
                        PromotionStatItem(
                            promotionId = promo.promotionId,
                            title = promo.title,
                            type = promo.type,
                            status = promo.status,
                            stockRemaining = promo.stockRemaining,
                            totalStock = promo.totalStock,
                            stockUtilization = promo.stockUtilization
                        )
                    } ?: emptyList(),
                    redemptionEntries = redemptionEntries,
                    bookingEntries = bookingEntries,
                    topRedeemedCoupons = topRedeemedCoupons,
                    redemptionTrendsByPromotion = redemptionTrendsByPromotion
                )
            } catch (e: Exception) {
                android.util.Log.e("StatsViewModel", "Exception in loadAnalytics", e)
                e.printStackTrace()

                val errorMessage = when {
                    e is java.net.UnknownHostException || e.message?.contains("Unable to resolve host") == true ->
                        "Sin conexión a internet. Por favor verifica tu conexión."
                    e is java.net.SocketTimeoutException || e.message?.contains("timeout") == true ->
                        "Tiempo de espera agotado. Por favor intenta de nuevo."
                    e is java.net.ConnectException || e.message?.contains("Failed to connect") == true ->
                        "No se pudo conectar al servidor. Verifica tu conexión a internet."
                    else -> "Error: ${e.message ?: e.javaClass.simpleName}"
                }

                android.util.Log.e("StatsViewModel", "Error message shown to user: $errorMessage")

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
                    if (chartData == null) {
                        emptyList()
                    } else {
                        val json = gson.toJson(chartData)
                        val backendChart = gson.fromJson(json, BackendAnalyticsChart::class.java)
                        backendChart.entries?.mapIndexed { index, entry ->
                            ChartEntry(
                                x = index,
                                y = entry.y,
                                xLabel = formatDateLabel(entry.x, _uiState.value.selectedTimeRange)
                            )
                        } ?: emptyList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error refreshing redemption entries: ${e.message}", e)
                    emptyList()
                }

                // Parse booking entries (daily bookings line chart)
                val bookingEntries = try {
                    val chartData = dashboard.charts["bookingTrends"]
                    if (chartData == null) {
                        emptyList()
                    } else {
                        val json = gson.toJson(chartData)
                        val backendChart = gson.fromJson(json, BackendAnalyticsChart::class.java)
                        backendChart.entries?.mapIndexed { index, entry ->
                            ChartEntry(
                                x = index,
                                y = entry.y,
                                xLabel = formatDateLabel(entry.x, _uiState.value.selectedTimeRange)
                            )
                        } ?: emptyList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error refreshing booking entries: ${e.message}", e)
                    emptyList()
                }

                // Parse bar chart data for top redeemed coupons
                val topRedeemedCoupons = try {
                    val chartData = dashboard.charts["topRedeemedCoupons"]
                    if (chartData == null) {
                        emptyList()
                    } else {
                        val json = gson.toJson(chartData)
                        val barChartData = gson.fromJson(json, BarChartData::class.java)
                        barChartData.entries ?: emptyList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error refreshing top redeemed coupons: ${e.message}", e)
                    emptyList()
                }

                // Parse multi-series line chart data
                val redemptionTrendsByPromotion = try {
                    val chartData = dashboard.charts["redemptionTrendsByPromotion"]
                    if (chartData == null) {
                        null
                    } else {
                        val json = gson.toJson(chartData)
                        val backendMultiSeries = gson.fromJson(json, BackendMultiSeriesLineChartData::class.java)
                        if (backendMultiSeries.series != null) {
                            MultiSeriesLineChartData(
                                type = backendMultiSeries.type ?: "multiline",
                                title = backendMultiSeries.title ?: "",
                                description = backendMultiSeries.description ?: "",
                                series = backendMultiSeries.series.mapNotNull { backendSeries ->
                                    if (backendSeries.seriesId != null && backendSeries.seriesLabel != null && backendSeries.entries != null) {
                                        SeriesData(
                                            seriesId = backendSeries.seriesId,
                                            seriesLabel = backendSeries.seriesLabel,
                                            entries = backendSeries.entries.mapIndexed { index, entry ->
                                                ChartEntry(
                                                    x = index,
                                                    y = entry.y,
                                                    xLabel = formatDateLabel(entry.x, _uiState.value.selectedTimeRange)
                                                )
                                            }
                                        )
                                    } else null
                                },
                                xAxisLabel = backendMultiSeries.xAxisLabel ?: "Date",
                                yAxisLabel = backendMultiSeries.yAxisLabel ?: "Count"
                            )
                        } else null
                    }
                } catch (e: Exception) {
                    android.util.Log.e("StatsViewModel", "Error refreshing multi-series chart: ${e.message}", e)
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
                    promotionStats = dashboard.promotionStats?.map { promo ->
                        PromotionStatItem(
                            promotionId = promo.promotionId,
                            title = promo.title,
                            type = promo.type,
                            status = promo.status,
                            stockRemaining = promo.stockRemaining,
                            totalStock = promo.totalStock,
                            stockUtilization = promo.stockUtilization
                        )
                    } ?: emptyList(),
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