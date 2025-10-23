package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.itesm.beneficiojuventud.model.history.HistoryService
import mx.itesm.beneficiojuventud.model.redeemedcoupon.RedeemedCoupon
import mx.itesm.beneficiojuventud.model.redeemedcoupon.RemoteServiceRedeemedCoupon
import android.util.Log

/**
 * ViewModel para gestionar cupones canjeados (historial).
 * Basado en el patrón de PromoViewModel (StateFlow + métodos suspend).
 */
class RedeemedCouponViewModel(private val historyService: HistoryService? = null) : ViewModel() {

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

    /** Obtiene todos los cupones canjeados por un usuario y guarda en historial. */
    suspend fun getRedeemedByUser(userId: String) {
        try {
            val redeemedCoupons = model.getRedeemedCouponsByUser(userId)
            _redeemedListState.value = redeemedCoupons

            // Guardar cada cupón redimido en el historial persistente
            redeemedCoupons.forEach { coupon ->
                try {
                    historyService?.addHistoryEvent(
                        userId = userId,
                        type = "CUPON_USADO",
                        title = coupon.promotion?.title ?: "Cupón",
                        subtitle = coupon.promotion?.businessName ?: "Negocio",
                        iso = coupon.usedAt ?: java.time.OffsetDateTime.now().toString(),
                        promotionId = coupon.promotionId,
                        branchId = coupon.branchId
                    )
                } catch (e: Exception) {
                    Log.e("RedeemedCouponViewModel", "Error saving redeemed coupon to history", e)
                }
            }
        } catch (e: Exception) {
            Log.e("RedeemedCouponViewModel", "Error getting redeemed coupons", e)
            throw e
        }
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
