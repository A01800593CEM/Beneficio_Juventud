package mx.itesm.beneficiojuventud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.RoomDB.LocalDatabase
import mx.itesm.beneficiojuventud.model.SavedCouponRepository
import mx.itesm.beneficiojuventud.model.bookings.Booking
import mx.itesm.beneficiojuventud.model.bookings.BookingStatus
import mx.itesm.beneficiojuventud.model.promos.Promotions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
            categoryDao = database.categoryDao(),
            promotionCategoriesDao = database.promotionCategoriesDao(),
            bookingDao = database.bookingDao()
        )
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
                    status = BookingStatus.ACTIVE
                )

                // Crear reservación usando el repositorio
                repository.createBooking(booking)

                _message.value = "¡Cupón reservado exitosamente!"
                _bookingSuccess.value = true

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
                val bookings = repository.getBookings(userId)
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
                repository.cancelBooking(
                    bookingId = booking.bookingId ?: return@launch,
                    promotionId = booking.promotionId ?: return@launch
                )

                _message.value = "Reservación cancelada"

                // Recargar bookings después de cancelar
                booking.userId?.let { loadUserBookings(it) }

            } catch (e: Exception) {
                _error.value = "Error al cancelar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza el estado de un booking (ej: de ACTIVE a USED)
     */
    fun updateBooking(bookingId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updatedBooking = repository.updateBooking(bookingId)
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
        return sdf.format(Date())
    }

    private fun getDefaultLimitDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 30) // 30 días desde hoy
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}
