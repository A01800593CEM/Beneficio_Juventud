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

private const val TAG = "QRScannerViewModel"

/**
 * Data class representing parsed QR code data
 * Expected format: "bj|v=1|pid=123|uid=abc123|lpu=2|ts=1234567890|n=abc12345"
 */
data class QRData(
    val version: Int,
    val promotionId: Int,
    val userId: String,
    val limitPerUser: Int,
    val timestamp: Long,
    val nonce: String
)

class QRScannerViewModel : ViewModel() {
    private val redeemedCouponService = RemoteServiceRedeemedCoupon

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _scanResult = MutableStateFlow<RedeemedCoupon?>(null)
    val scanResult: StateFlow<RedeemedCoupon?> = _scanResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Process scanned QR code data
     * @param qrData The QR code string to process
     * @param branchId The branch ID where the QR is being scanned (from logged-in collaborator)
     */
    fun processQRCode(qrData: String, branchId: Int) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _error.value = null

                Log.d(TAG, "Processing QR Code: $qrData at branchId: $branchId")

                // Parse QR code
                val parsedData = parseQRCode(qrData)

                // Validate QR code
                validateQRCode(parsedData)

                // Create redeemed coupon with all QR data
                val redeemedCoupon = RedeemedCoupon(
                    userId = parsedData.userId,
                    promotionId = parsedData.promotionId,
                    branchId = branchId,
                    nonce = parsedData.nonce,
                    qrTimestamp = parsedData.timestamp
                )

                Log.d(TAG, "========== REDEEMING COUPON ==========")
                Log.d(TAG, "RedeemedCoupon object:")
                Log.d(TAG, "  userId: ${redeemedCoupon.userId}")
                Log.d(TAG, "  promotionId: ${redeemedCoupon.promotionId}")
                Log.d(TAG, "  branchId: ${redeemedCoupon.branchId}")
                Log.d(TAG, "  nonce: ${redeemedCoupon.nonce}")
                Log.d(TAG, "  qrTimestamp: ${redeemedCoupon.qrTimestamp}")
                Log.d(TAG, "Calling API endpoint: POST /redeemedcoupon")

                // Call API to redeem coupon
                val result = redeemedCouponService.createRedeemedCoupon(redeemedCoupon)

                Log.d(TAG, "API Response:")
                Log.d(TAG, "  usedId: ${result?.usedId}")
                Log.d(TAG, "  Result: $result")
                Log.d(TAG, "=======================================")

                if (result != null) {
                    _scanResult.value = result
                    Log.d(TAG, "Coupon redeemed successfully: usedId=${result.usedId}")
                } else {
                    _error.value = "Error al canjear el cupón. Intente nuevamente."
                }

            } catch (e: QRValidationException) {
                Log.e(TAG, "QR validation failed", e)
                _error.value = e.message
            } catch (e: Exception) {
                Log.e(TAG, "Error processing QR code", e)
                _error.value = "Error al procesar el código QR. Verifique e intente nuevamente."
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Parse QR code string into QRData object
     * Expected format: "bj|v=1|pid=123|uid=abc123|lpu=2|ts=1234567890|n=abc12345"
     */
    private fun parseQRCode(qrData: String): QRData {
        try {
            Log.d(TAG, "========== QR SCANNING DEBUG ==========")
            Log.d(TAG, "Raw QR Data: $qrData")

            val parts = qrData.split("|")
            Log.d(TAG, "Split parts: $parts")

            // Verify prefix
            if (parts.isEmpty() || parts[0] != "bj") {
                Log.e(TAG, "Invalid prefix: ${parts.getOrNull(0)}")
                throw QRValidationException("Código QR inválido: formato no reconocido")
            }

            // Parse key-value pairs
            val data = parts.drop(1).associate { part ->
                val (key, value) = part.split("=")
                key to value
            }
            Log.d(TAG, "Parsed data map: $data")

            // Extract and validate required fields
            val version = data["v"]?.toIntOrNull()
                ?: throw QRValidationException("Código QR inválido: versión faltante")

            val promotionId = data["pid"]?.toIntOrNull()
                ?: throw QRValidationException("Código QR inválido: ID de promoción faltante")

            val userId = data["uid"]
                ?: throw QRValidationException("Código QR inválido: ID de usuario faltante")

            val limitPerUser = data["lpu"]?.toIntOrNull()
                ?: throw QRValidationException("Código QR inválido: límite por usuario faltante")

            val timestamp = data["ts"]?.toLongOrNull()
                ?: throw QRValidationException("Código QR inválido: timestamp faltante")

            val nonce = data["n"]
                ?: throw QRValidationException("Código QR inválido: nonce faltante")

            val qrDataParsed = QRData(
                version = version,
                promotionId = promotionId,
                userId = userId,
                limitPerUser = limitPerUser,
                timestamp = timestamp,
                nonce = nonce
            )

            Log.d(TAG, "Parsed QRData:")
            Log.d(TAG, "  version: $version")
            Log.d(TAG, "  promotionId: $promotionId")
            Log.d(TAG, "  userId: $userId")
            Log.d(TAG, "  limitPerUser: $limitPerUser")
            Log.d(TAG, "  timestamp: $timestamp")
            Log.d(TAG, "  nonce: $nonce")
            Log.d(TAG, "=========================================")

            return qrDataParsed
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing QR: ${e.message}", e)
            if (e is QRValidationException) throw e
            throw QRValidationException("Error al parsear el código QR: ${e.message}")
        }
    }

    /**
     * Validate parsed QR code data
     */
    private fun validateQRCode(qrData: QRData) {
        // Check version compatibility
        if (qrData.version != 1) {
            throw QRValidationException("Versión de QR no compatible: ${qrData.version}")
        }

        // Check timestamp (QR should not be older than 24 hours)
        val currentTime = System.currentTimeMillis()
        val qrAge = currentTime - qrData.timestamp
        val maxAge = 24 * 60 * 60 * 1000L // 24 hours in milliseconds

        if (qrAge > maxAge) {
            throw QRValidationException("Código QR expirado. Por favor solicite uno nuevo.")
        }

        // Check if QR is from the future (clock skew tolerance: 5 minutes)
        val futureToleranceMs = 5 * 60 * 1000L
        if (qrData.timestamp > currentTime + futureToleranceMs) {
            throw QRValidationException("Código QR inválido: timestamp futuro")
        }

        // Additional validations can be added here
        // For example: checking if promotion is still active, stock validation, etc.
    }

    /**
     * Clear scan result
     */
    fun clearResult() {
        _scanResult.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * Custom exception for QR validation errors
 */
class QRValidationException(message: String) : Exception(message)
