package mx.itesm.beneficiojuventud.viewcollab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsSummary(
    val redeemedCoupons: String,
    val monthlyChange: String,
    val conversionRate: String
)

data class StatsUiState(
    val summary: StatsSummary? = null,
    val isLoading: Boolean = true
)

class StatsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val summaryData = StatsSummary(
                redeemedCoupons = "670",
                monthlyChange = "+23%",
                conversionRate = "7.12%"
            )
            _uiState.value = StatsUiState(summary = summaryData, isLoading = false)
        }
    }
}