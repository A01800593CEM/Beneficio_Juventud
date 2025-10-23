package mx.itesm.beneficiojuventud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.RoomDB.LocalDatabase
import mx.itesm.beneficiojuventud.model.SavedCouponRepository
import mx.itesm.beneficiojuventud.model.bookings.Booking
import mx.itesm.beneficiojuventud.model.bookings.BookingStatus
import mx.itesm.beneficiojuventud.model.bookings.RemoteServiceBooking
import mx.itesm.beneficiojuventud.model.bookings.CooldownInfo
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos
import mx.itesm.beneficiojuventud.model.history.HistoryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Eventos de historial para bookings
 */
sealed class BookingEvent(
    val title: String,
    val businessName: String?,
    val timestampIso: String
) {
    class Reserved(title: String, businessName: String?, timestampIso: String)
        : BookingEvent(title, businessName, timestampIso)

    class Cancelled(title: String, businessName: String?, timestampIso: String)
        : BookingEvent(title, businessName, timestampIso)
}

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    // Database and Repository
    private val database: LocalDatabase by lazy {
        Room.databaseBuilder(
            application,
            LocalDatabase::class.java,
            "beneficio_juventud_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val repository: SavedCouponRepository by lazy {
        SavedCouponRepository(
            promotionDao = database.promotionDao(),
            bookingDao = database.bookingDao()
        )
    }

    private val historyService: HistoryService by lazy {
        HistoryService(historyDao = database.historyDao())
    }

    // State Flows
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _reservedPromotions = MutableStateFlow<List<Promotions>>(emptyList())
    val reservedPromotions: StateFlow<List<Promotions>> = _reservedPromotions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _bookingSuccess = MutableStateFlow(false)
    val bookingSuccess: StateFlow<Boolean> = _bookingSuccess.asStateFlow()

    // Cooldown y timer
    private val _cooldownInfo = MutableStateFlow<CooldownInfo?>(null)
    val cooldownInfo: StateFlow<CooldownInfo?> = _cooldownInfo.asStateFlow()

    private val _remainingTime = MutableStateFlow(0) // en segundos
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()

    private val _isAutoExpired = MutableStateFlow(false) // para notificar auto-expiración
    val isAutoExpired: StateFlow<Boolean> = _isAutoExpired.asStateFlow()

    // Eventos de historial para bookings
    // replay = 50 para que History pueda recibir eventos pasados al abrirse
    private val _bookingEvents = MutableSharedFlow<BookingEvent>(replay = 50, extraBufferCapacity = 50)
    val bookingEvents: SharedFlow<BookingEvent> = _bookingEvents

    /**
     * Reserva un cupón/promoción
     */
    fun reserveCoupon(promotion: Promotions, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _bookingSuccess.value = false

            try {
                // Crear objeto Booking con fechas actuales
                val booking = Booking(
                    userId = userId,
                    promotionId = promotion.promotionId,
                    bookingDate = getCurrentDateISO(),
                    limitUseDate = promotion.endDate ?: getDefaultLimitDate(),
                    status = BookingStatus.PENDING
                )

                // Crear reservación usando el repositorio
                repository.createBooking(booking)

                _message.value = "¡Cupón reservado exitosamente!"
                _bookingSuccess.value = true

                // Asegurar que la promoción se agregue a favoritos (auto-favorite)
                try {
                    repository.favoritePromotion(promotion.promotionId!!, userId)
                    Log.d("BookingViewModel", "✅ Auto-favorited promotion ${promotion.promotionId}")
                } catch (favError: Exception) {
                    Log.w("BookingViewModel", "⚠️ Failed to auto-favorite: ${favError.message}")
                    // Non-critical, don't throw
                }

                // Emitir evento de historial
                val event = BookingEvent.Reserved(
                    title = promotion.title ?: "Cupón",
                    businessName = promotion.businessName,
                    timestampIso = getCurrentDateISO()
                )
                Log.d("BookingViewModel", "Emitiendo evento Reserved: title=${event.title}, business=${event.businessName}")
                _bookingEvents.emit(event)
                Log.d("BookingViewModel", "Evento Reserved emitido exitosamente")

                // Guardar en historial persistente
                historyService.addHistoryEvent(
                    userId = userId,
                    type = "CUPON_RESERVADO",
                    title = promotion.title ?: "Cupón",
                    subtitle = promotion.businessName ?: "Negocio",
                    iso = event.timestampIso,
                    promotionId = promotion.promotionId,
                    branchId = null
                )

            } catch (e: Exception) {
                _error.value = "Error al reservar: ${e.message}"
                _bookingSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene las reservaciones del usuario
     */
    fun loadUserBookings(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val bookings = withContext(Dispatchers.IO) {
                    RemoteServiceBooking.getUserBookings(userId)
                }
                _bookings.value = bookings
            } catch (e: Exception) {
                _error.value = "Error al cargar reservaciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene las promociones reservadas con detalles completos
     */
    fun loadReservedPromotions(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val promos = repository.getReservedPromotions(userId)
                _reservedPromotions.value = promos
            } catch (e: Exception) {
                _error.value = "Error al cargar promociones reservadas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cancela una reservación
     */
    fun cancelBooking(booking: Booking) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Obtener los datos de la promoción antes de cancelar
                val promotionId = booking.promotionId ?: return@launch
                val promotion = withContext(Dispatchers.IO) {
                    try {
                        RemoteServicePromos.getPromotionById(promotionId)
                    } catch (e: Exception) {
                        null
                    }
                }

                repository.cancelBooking(
                    bookingId = booking.bookingId ?: return@launch,
                    promotionId = promotionId
                )

                _message.value = "Reservación cancelada"

                // Emitir evento de historial
                val event = BookingEvent.Cancelled(
                    title = promotion?.title ?: "Cupón",
                    businessName = promotion?.businessName,
                    timestampIso = getCurrentDateISO()
                )
                Log.d("BookingViewModel", "Emitiendo evento Cancelled: title=${event.title}, business=${event.businessName}")
                _bookingEvents.emit(event)
                Log.d("BookingViewModel", "Evento Cancelled emitido exitosamente")

                // Guardar en historial persistente
                historyService.addHistoryEvent(
                    userId = booking.userId ?: "",
                    type = "RESERVA_CANCELADA",
                    title = promotion?.title ?: "Cupón",
                    subtitle = promotion?.businessName ?: "Negocio",
                    iso = event.timestampIso,
                    promotionId = promotion?.promotionId,
                    branchId = null
                )

                // Recargar bookings después de cancelar
                booking.userId?.let { userId ->
                    Log.d("BookingViewModel", "Recargando bookings para usuario $userId después de cancelación")
                    loadUserBookings(userId)
                }

            } catch (e: Exception) {
                _error.value = "Error al cancelar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza el estado de un booking (ej: de PENDING a USED)
     */
    fun updateBooking(bookingId: Int, status: BookingStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updatedBooking = repository.updateBooking(bookingId, status)
                _message.value = "Booking actualizado"
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene un booking específico por ID
     */
    fun getBookingById(bookingId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val booking = repository.getBookingById(bookingId)
                // Aquí puedes emitir el booking a un StateFlow si lo necesitas
            } catch (e: Exception) {
                _error.value = "No se pudo cargar el booking: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Limpia el mensaje de éxito
     */
    fun clearMessage() {
        _message.value = null
    }

    /**
     * Resetea el estado de bookingSuccess
     */
    fun resetBookingSuccess() {
        _bookingSuccess.value = false
    }

    // ────────────────────────────────────────────────────────────────
    // Helper functions
    // ────────────────────────────────────────────────────────────────

    private fun getCurrentDateISO(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun getDefaultLimitDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 30) // 30 días desde hoy
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(calendar.time)
    }

    /**
     * Obtiene información del cooldown para una promoción
     */
    fun getCooldownInfo(userId: String, promotionId: Int) {
        viewModelScope.launch {
            try {
                val info = withContext(Dispatchers.IO) {
                    RemoteServiceBooking.getCooldownInfo(userId, promotionId)
                }
                _cooldownInfo.value = info

                // Si hay cooldown activo, inicia un timer
                if (info != null && info.isActive) {
                    startCooldownTimer(info.remainingSeconds)
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error al obtener cooldown info: ${e.message}")
                _cooldownInfo.value = null
            }
        }
    }

    /**
     * Timer para mostrar el tiempo de cooldown restante
     */
    private fun startCooldownTimer(initialSeconds: Int) {
        viewModelScope.launch {
            var remaining = initialSeconds
            while (remaining > 0) {
                _remainingTime.value = remaining
                delay(1000) // Esperar 1 segundo
                remaining--
            }
            _remainingTime.value = 0
            _cooldownInfo.value = null // Limpiar cuando termine
        }
    }

    /**
     * Timer para mostrar el tiempo de expiración automática de una reserva
     */
    fun startExpirationTimer(autoExpireDate: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")

            try {
                val expireTime = sdf.parse(autoExpireDate)?.time ?: return@launch

                while (true) {
                    val now = System.currentTimeMillis()
                    val remaining = (expireTime - now) / 1000

                    if (remaining <= 0) {
                        _isAutoExpired.value = true
                        break
                    }

                    _remainingTime.value = remaining.toInt()
                    delay(1000) // Actualizar cada segundo
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error en timer de expiración: ${e.message}")
            }
        }
    }

    /**
     * Resetea el flag de auto-expiración
     */
    fun resetAutoExpired() {
        _isAutoExpired.value = false
    }
}
