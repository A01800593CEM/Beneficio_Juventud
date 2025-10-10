package mx.itesm.beneficiojuventud.viewcollab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
data class PromotionsUiState(
    val promotions: List<Promotion> = emptyList(),
    val isLoading: Boolean = true
)

class PromotionsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PromotionsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPromotions()
    }

    private fun loadPromotions() {
        viewModelScope.launch {
            val samplePromotions = listOf(
                Promotion("Martes 2x1", "Cine Stelar", "Compra un boleto y obtén el segundo gratis.", "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Activa"),
                Promotion("Postre Gratis", "La Bella Italia", "En la compra de cualquier pizza grande, llévate un tiramisú.", "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?q=80&w=880&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Activa"),
                Promotion("15% Descuento", "Librería El Saber", "Descuento en todos los libros de ciencia ficción.", "https://images.unsplash.com/photo-1618365908648-e71bd5716cba?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D", "Expirada")
            )
            _uiState.value = PromotionsUiState(promotions = samplePromotions, isLoading = false)
        }
    }
}