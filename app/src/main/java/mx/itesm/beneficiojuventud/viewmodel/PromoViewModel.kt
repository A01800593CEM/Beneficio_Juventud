package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos

class PromoViewModel : ViewModel() {

    private val model = RemoteServicePromos

    private val _promoState = MutableStateFlow(Promotions())
    val promoState: StateFlow<Promotions> = _promoState
    private val _promoListState = MutableStateFlow<List<Promotions>>(emptyList())
    val promoListState: StateFlow<List<Promotions>> = _promoListState

    suspend fun getAllPromotions() {
        _promoListState.value = model.getAllPromotions()
    }

    suspend fun getPromotionById(id: Int) {
        _promoState.value = model.getPromotionById(id)
    }

    suspend fun getPromotionByCategory(category: String) {
        _promoListState.value = model.getPromotionByCategory(category)
    }

    suspend fun createPromotion(promo: Promotions) {
        _promoState.value = model.createPromotion(promo)
    }

    suspend fun updatePromotion(id: Int, update: Promotions) {
        _promoState.value = model.updatePromotion(id, update)
    }

    suspend fun deletePromotion(id: Int) {
        model.deletePromotion(id)
    }

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    fun isFavorite(promoId: Int): Boolean = _favoriteIds.value.contains(promoId)

    fun toggleFavorite(promoId: Int) = viewModelScope.launch {
        _favoriteIds.update { set ->
            if (set.contains(promoId)) set - promoId else set + promoId
        }

    }
}
