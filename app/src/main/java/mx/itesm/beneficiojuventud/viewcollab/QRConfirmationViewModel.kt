package mx.itesm.beneficiojuventud.viewcollab

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.redeemedcoupon.RedeemedCoupon
import mx.itesm.beneficiojuventud.model.redeemedcoupon.RemoteServiceRedeemedCoupon

private const val TAG = "QRConfirmationViewModel"

/**
 * ViewModel para manejar la lógica de confirmación de cupón QR escaneado
 */
class QRConfirmationViewModel : ViewModel() {
    private val redeemedCouponService = RemoteServiceRedeemedCoupon

    // Estado para almacenar los datos de confirmación actual
    private val _confirmationData = MutableStateFlow<QRConfirmationData?>(null)
    val confirmationData: StateFlow<QRConfirmationData?> = _confirmationData.asStateFlow()

    // Estado para confirmar el cupón
    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()

    // Estado de éxito
    private val _confirmationSuccess = MutableStateFlow<RedeemedCoupon?>(null)
    val confirmationSuccess: StateFlow<RedeemedCoupon?> = _confirmationSuccess.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Establecer datos de confirmación
     */
    fun setConfirmationData(data: QRConfirmationData) {
        _confirmationData.value = data
    }

    /**
     * Confirmar y registrar el uso del cupón
     */
    fun confirmCoupon(confirmationData: QRConfirmationData) {
        viewModelScope.launch {
            try {
                _isConfirming.value = true
                _error.value = null

                Log.d(TAG, "Confirming coupon redemption")
                Log.d(TAG, "userId: ${confirmationData.userId}")
                Log.d(TAG, "promotionId: ${confirmationData.promotionId}")
                Log.d(TAG, "branchId: ${confirmationData.branchId}")

                // Crear objeto RedeemedCoupon
                val redeemedCoupon = RedeemedCoupon(
                    userId = confirmationData.userId,
                    promotionId = confirmationData.promotionId,
                    branchId = confirmationData.branchId,
                    nonce = confirmationData.nonce,
                    qrTimestamp = confirmationData.qrTimestamp
                )

                Log.d(TAG, "========== REDEEMING COUPON ==========")
                Log.d(TAG, "RedeemedCoupon object:")
                Log.d(TAG, "  userId: ${redeemedCoupon.userId}")
                Log.d(TAG, "  promotionId: ${redeemedCoupon.promotionId}")
                Log.d(TAG, "  branchId: ${redeemedCoupon.branchId}")
                Log.d(TAG, "  nonce: ${redeemedCoupon.nonce}")
                Log.d(TAG, "  qrTimestamp: ${redeemedCoupon.qrTimestamp}")
                Log.d(TAG, "Calling API endpoint: POST /redeemedcoupon")

                // Realizar llamada a la API
                val result = redeemedCouponService.createRedeemedCoupon(redeemedCoupon)

                Log.d(TAG, "API Response:")
                Log.d(TAG, "  usedId: ${result?.usedId}")
                Log.d(TAG, "  Result: $result")
                Log.d(TAG, "=======================================")

                if (result != null) {
                    _confirmationSuccess.value = result
                    Log.d(TAG, "Coupon redeemed successfully: usedId=${result.usedId}")
                } else {
                    _error.value = "Error al canjear el cupón. Intente nuevamente."
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error confirming coupon", e)
                _error.value = "Error al confirmar el cupón. Intente nuevamente."
            } finally {
                _isConfirming.value = false
            }
        }
    }

    /**
     * Cancelar la confirmación
     */
    fun cancelConfirmation() {
        _confirmationData.value = null
        _confirmationSuccess.value = null
        _error.value = null
        _isConfirming.value = false
    }

    /**
     * Limpiar estado de éxito
     */
    fun clearSuccess() {
        _confirmationSuccess.value = null
    }

    /**
     * Limpiar estado de error
     */
    fun clearError() {
        _error.value = null
    }
}
