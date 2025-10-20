package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.itesm.beneficiojuventud.model.redeemedcoupon.RedeemedCoupon
import mx.itesm.beneficiojuventud.model.redeemedcoupon.RemoteServiceRedeemedCoupon

/**
 * ViewModel para gestionar cupones canjeados (historial).
 * Basado en el patrón de PromoViewModel (StateFlow + métodos suspend).
 */
class RedeemedCouponViewModel : ViewModel() {

    private val model = RemoteServiceRedeemedCoupon

    // Estado de un cupón canjeado (detalle)
    private val _redeemedState = MutableStateFlow(RedeemedCoupon())
    val redeemedState: StateFlow<RedeemedCoupon> = _redeemedState

    // Estado de lista de cupones canjeados (historial)
    private val _redeemedListState = MutableStateFlow<List<RedeemedCoupon>>(emptyList())
    val redeemedListState: StateFlow<List<RedeemedCoupon>> = _redeemedListState

    // ─────────────────────────────────────────────────────────────────────────────
    // Consultas / CRUD
    // ─────────────────────────────────────────────────────────────────────────────

    /** Obtiene todos los cupones canjeados por un usuario. */
    suspend fun getRedeemedByUser(userId: String) {
        _redeemedListState.value = model.getRedeemedCouponsByUser(userId)
    }

    /** Obtiene detalle de un canje por ID de cupón (promotionId o usedId según API). */
    suspend fun getRedeemedByCouponId(id: Int) {
        _redeemedState.value = model.getRedeeemedCouponByCouponId(id)
    }

    /** Crea un registro de cupón canjeado. */
    suspend fun createRedeemed(payload: RedeemedCoupon) {
        _redeemedState.value = model.createRedeemedCoupon(payload)
    }

    /** Actualiza un registro de canje. */
    suspend fun updateRedeemed(id: Int, update: RedeemedCoupon) {
        _redeemedState.value = model.updateRedeemedCoupon(id, update)
    }

    /** Elimina un registro de canje. */
    suspend fun deleteRedeemed(id: Int) {
        model.deleteRedeemedCoupon(id)
        // Opcional: refrescar lista si la tuvieras cargada con anterioridad
        // _redeemedListState.value = _redeemedListState.value.filterNot { it.usedId == id }
    }
}
