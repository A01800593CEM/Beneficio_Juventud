package mx.itesm.beneficiojuventud.viewcollab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.itesm.beneficiojuventud.model.analytics.AnalyticsDashboard
import mx.itesm.beneficiojuventud.model.analytics.RemoteServiceAnalytics

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

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dashboard = dashboard
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

                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    dashboard = dashboard
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