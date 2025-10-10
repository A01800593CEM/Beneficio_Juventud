package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.Promotions
import mx.itesm.beneficiojuventud.model.RemoteServicePromos

// ---------------------------
// Estados de UI (mismo patrón)
// ---------------------------
sealed class PromoUiState {
    object Idle : PromoUiState()
    object Loading : PromoUiState()
    data class SuccessSingle(val promo: Promotions?) : PromoUiState()
    data class SuccessList(val promos: List<Promotions>) : PromoUiState()
    data class Error(val message: String) : PromoUiState()
}

// ---------------------------
// ViewModel principal
// ---------------------------
class PromoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PromoUiState>(PromoUiState.Idle)
    val uiState: StateFlow<PromoUiState> = _uiState

    // 🔹 Obtener promoción por ID
    fun getPromotionById(id: Int) {
        viewModelScope.launch {
            _uiState.value = PromoUiState.Loading
            try {
                val promo = RemoteServicePromos.getPromotionById(id)
                _uiState.value = PromoUiState.SuccessSingle(promo)
            } catch (e: Exception) {
                _uiState.value = PromoUiState.Error(e.message ?: "Error al obtener la promoción")
            }
        }
    }

    // 🔹 Obtener promociones por categoría
    fun getPromotionsByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = PromoUiState.Loading
            try {
                val promos = RemoteServicePromos.getPromotionByCategory(category)
                _uiState.value = PromoUiState.SuccessList(promos)
            } catch (e: Exception) {
                _uiState.value = PromoUiState.Error(e.message ?: "Error al obtener promociones")
            }
        }
    }

    // 🔹 Crear nueva promoción
    fun createPromotion(promo: Promotions) {
        viewModelScope.launch {
            _uiState.value = PromoUiState.Loading
            try {
                val createdPromo = RemoteServicePromos.createPromotion(promo)
                _uiState.value = PromoUiState.SuccessSingle(createdPromo)
            } catch (e: Exception) {
                _uiState.value = PromoUiState.Error(e.message ?: "Error al crear la promoción")
            }
        }
    }

    // 🔹 Actualizar promoción existente
    fun updatePromotion(id: Int, update: Promotions) {
        viewModelScope.launch {
            _uiState.value = PromoUiState.Loading
            try {
                val updatedPromo = RemoteServicePromos.updatePromotion(id, update)
                _uiState.value = PromoUiState.SuccessSingle(updatedPromo)
            } catch (e: Exception) {
                _uiState.value = PromoUiState.Error(e.message ?: "Error al actualizar la promoción")
            }
        }
    }

    // 🔹 Eliminar promoción
    fun deletePromotion(id: Int) {
        viewModelScope.launch {
            _uiState.value = PromoUiState.Loading
            try {
                RemoteServicePromos.deletePromotion(id)
                _uiState.value = PromoUiState.SuccessSingle(null)
            } catch (e: Exception) {
                _uiState.value = PromoUiState.Error(e.message ?: "Error al eliminar la promoción")
            }
        }
    }

    // 🔹 Resetear el estado
    fun resetState() {
        _uiState.value = PromoUiState.Idle
    }
}
