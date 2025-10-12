package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos

class PromoViewModel : ViewModel() {

    private val model = RemoteServicePromos

    private val _promoState = MutableStateFlow(Promotions())
    val promoState: StateFlow<Promotions> = _promoState
    private val _promoListState = MutableStateFlow<List<Promotions>>(emptyList())
    val promoListState: StateFlow<List<Promotions>> = _promoListState

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
}
