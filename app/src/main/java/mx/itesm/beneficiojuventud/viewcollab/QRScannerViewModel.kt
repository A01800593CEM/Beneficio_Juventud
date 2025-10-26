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
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos

private const val TAG = "QRScannerViewModel"

/**
 * Data class representing parsed QR code data
 * Expected format: "bj|v=1|pid=123|uid=abc123|cid=collab123|lpu=2|ts=1234567890|n=abc12345"
 */
data class QRData(
    val version: Int,
    val promotionId: Int,
    val userId: String,
    val collaboratorId: String,
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

    // Datos para la pantalla de confirmación
    private val _confirmationData = MutableStateFlow<QRConfirmationData?>(null)
    val confirmationData: StateFlow<QRConfirmationData?> = _confirmationData.asStateFlow()

    /**
     * Process scanned QR code data and prepare confirmation screen data
     * Obtiene información de la promoción desde la API
     * @param qrData The QR code string to process
     * @param branchId The branch ID where the QR is being scanned (from logged-in collaborator)
     * @param scanningCollaboratorId The collaborator ID who is scanning (from logged-in collaborator)
     * @param userName The name of the user (from QR data or API)
     */
    fun processQRCode(
        qrData: String,
        branchId: Int,
        scanningCollaboratorId: String?,
        userName: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _error.value = null

                Log.d(TAG, "Processing QR Code: $qrData at branchId: $branchId by collaborator: $scanningCollaboratorId")

                // Parse QR code
                val parsedData = parseQRCode(qrData)

                // Validate QR code
                validateQRCode(parsedData)

                // Validate collaborator match
                if (scanningCollaboratorId != null && parsedData.collaboratorId != scanningCollaboratorId) {
                    Log.e(TAG, "Collaborator mismatch! QR collaboratorId: ${parsedData.collaboratorId}, Scanner collaboratorId: $scanningCollaboratorId")
                    throw QRValidationException("Este cupón solo puede ser canjeado en el negocio correspondiente.")
                }

                Log.d(TAG, "QR Code validated successfully")

                // Obtener detalles de la promoción desde la API
                Log.d(TAG, "Fetching promotion details for promotionId: ${parsedData.promotionId}")
                val promotion = try {
                    RemoteServicePromos.getPromotionById(parsedData.promotionId)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch promotion details: ${e.message}")
                    null
                }

                // Usar datos de la promoción o valores por defecto
                val promotionTitle = promotion?.title ?: "Promoción"
                val collaboratorName = promotion?.businessName ?: "Colaborador"

                Log.d(TAG, "Fetched promotion data - Title: $promotionTitle, Collaborator: $collaboratorName")

                // Crear datos de confirmación para la pantalla
                val confirmData = QRConfirmationData(
                    userName = userName,
                    promotionTitle = promotionTitle,
                    collaboratorName = collaboratorName,
                    promotionId = parsedData.promotionId,
                    userId = parsedData.userId,
                    branchId = branchId,
                    nonce = parsedData.nonce,
                    qrTimestamp = parsedData.timestamp
                )

                _confirmationData.value = confirmData
                Log.d(TAG, "Confirmation data prepared: $confirmData")

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
     * Confirmar la redención del cupón (llamado desde QRConfirmationScreen)
     */
    fun confirmRedeemedCoupon(confirmationData: QRConfirmationData) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true

                Log.d(TAG, "========== CONFIRMING COUPON REDEMPTION ==========")
                Log.d(TAG, "Confirmation data:")
                Log.d(TAG, "  userId: ${confirmationData.userId}")
                Log.d(TAG, "  promotionId: ${confirmationData.promotionId}")
                Log.d(TAG, "  branchId: ${confirmationData.branchId}")

                // Create redeemed coupon with all QR data
                val redeemedCoupon = RedeemedCoupon(
                    userId = confirmationData.userId,
                    promotionId = confirmationData.promotionId,
                    branchId = confirmationData.branchId,
                    nonce = confirmationData.nonce,
                    qrTimestamp = confirmationData.qrTimestamp
                )

                Log.d(TAG, "Calling API endpoint: POST /redeemedcoupon")

                // Call API to redeem coupon
                val result = redeemedCouponService.createRedeemedCoupon(redeemedCoupon)

                Log.d(TAG, "API Response:")
                Log.d(TAG, "  usedId: ${result?.usedId}")
                Log.d(TAG, "  Result: $result")
                Log.d(TAG, "==================================================")

                if (result != null) {
                    _scanResult.value = result
                    Log.d(TAG, "Coupon redeemed successfully: usedId=${result.usedId}")
                } else {
                    _error.value = "Error al canjear el cupón. Intente nuevamente."
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error confirming coupon redemption", e)
                _error.value = "Error al confirmar la redención. Intente nuevamente."
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

            val collaboratorId = data["cid"]
                ?: throw QRValidationException("Código QR inválido: ID de colaborador faltante")

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
                collaboratorId = collaboratorId,
                limitPerUser = limitPerUser,
                timestamp = timestamp,
                nonce = nonce
            )

            Log.d(TAG, "Parsed QRData:")
            Log.d(TAG, "  version: $version")
            Log.d(TAG, "  promotionId: $promotionId")
            Log.d(TAG, "  userId: $userId")
            Log.d(TAG, "  collaboratorId: $collaboratorId")
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

    /**
     * Clear confirmation data
     */
    fun clearConfirmationData() {
        _confirmationData.value = null
    }

    /**
     * Cancel confirmation and reset all states
     */
    fun cancelConfirmation() {
        _confirmationData.value = null
        _scanResult.value = null
        _error.value = null
        _isProcessing.value = false
    }
}

/**
 * Custom exception for QR validation errors
 */
class QRValidationException(message: String) : Exception(message)
