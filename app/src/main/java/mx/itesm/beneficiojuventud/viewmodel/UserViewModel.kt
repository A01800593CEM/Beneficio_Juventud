package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.itesm.beneficiojuventud.model.bookings.Booking
import mx.itesm.beneficiojuventud.model.bookings.RemoteServiceBooking
import mx.itesm.beneficiojuventud.model.SavedCouponRepository
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.history.HistoryService
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.model.users.UserProfile
import java.time.OffsetDateTime

class UserViewModel(
    private val repository: SavedCouponRepository,
    private val historyService: HistoryService? = null
) : ViewModel() {

    private val model = RemoteServiceUser

    private val _userState = MutableStateFlow(UserProfile())
    val userState: StateFlow<UserProfile> = _userState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _favoritePromotions = MutableStateFlow<List<Promotions>>(emptyList())
    val favoritePromotions: StateFlow<List<Promotions>> = _favoritePromotions

    // AHORA: lista de objetos Collaborator (antes List<Int>)
    private val _favoriteCollabs = MutableStateFlow<List<Collaborator>>(emptyList())
    val favoriteCollabs: StateFlow<List<Collaborator>> = _favoriteCollabs

    // Reservaciones del usuario
    private val _userBookings = MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> = _userBookings

    // Promociones reservadas (con detalles completos)
    private val _reservedPromotions = MutableStateFlow<List<Promotions>>(emptyList())
    val reservedPromotions: StateFlow<List<Promotions>> = _reservedPromotions

    /** Token para invalidar respuestas tardías cuando cambia de cuenta o se hace clear. */
    private var loadToken: Int = 0

    /** Limpia el perfil y **anula** cualquier request en curso. */
    fun clearUser() {
        loadToken++                 // invalida todas las respuestas pendientes
        _error.value = null
        _userState.value = UserProfile()
        _isLoading.value = false
    }

    /** Carga el usuario por su cognitoId; descarta respuestas tardías. */
    fun getUserById(cognitoId: String) {
        val myToken = ++loadToken
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            android.util.Log.d("UserViewModel", "🔍 Iniciando getUserById para: $cognitoId")
            val result = runCatching {
                withContext(Dispatchers.IO) { model.getUserById(cognitoId) }
            }
            if (myToken != loadToken) {
                android.util.Log.w("UserViewModel", "⚠️ Respuesta de getUserById llegó tarde, descartando")
                return@launch // llegó tarde, se descarta
            }

            result.fold(
                onSuccess = { user ->
                    android.util.Log.d("UserViewModel", "✅ Usuario cargado: ${user.email}, cognitoId: ${user.cognitoId}")
                    _userState.value = user
                },
                onFailure = { e ->
                    android.util.Log.e("UserViewModel", "❌ Error al cargar usuario: ${e.message}", e)
                    _error.value = e.message ?: "Error al cargar usuario"
                }
            )
            _isLoading.value = false
        }
    }

    fun createUser(user: UserProfile) {
        val myToken = ++loadToken
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            android.util.Log.d("UserViewModel", "🔄 Iniciando createUser para: ${user.email}")
            val result = runCatching {
                withContext(Dispatchers.IO) { model.createUser(user) }
            }
            if (myToken != loadToken) {
                android.util.Log.w("UserViewModel", "⚠️ Respuesta de createUser llegó tarde, descartando")
                return@launch
            }

            result.fold(
                onSuccess = { created ->
                    android.util.Log.d("UserViewModel", "✅ Usuario creado exitosamente: ${created.email}, cognitoId: ${created.cognitoId}")
                    _userState.value = created
                },
                onFailure = { e ->
                    android.util.Log.e("UserViewModel", "❌ Error al crear usuario: ${e.message}", e)
                    _error.value = e.message ?: "Error al crear usuario"
                }
            )
            _isLoading.value = false
        }
    }

    /**
     * Crea un usuario y espera a que se complete REALMENTE (bloqueante).
     * Devuelve (success: Boolean, user: UserProfile?, error: String?)
     * IMPORTANTE: Solo para GoogleRegister - NO usar en flujos normales
     */
    suspend fun createUserAndWait(user: UserProfile): Triple<Boolean, UserProfile?, String?> {
        android.util.Log.d("UserViewModel", "🔄 Iniciando createUserAndWait (SINCRÓNICO) para: ${user.email}")
        return try {
            val created = withContext(Dispatchers.IO) {
                model.createUser(user)
            }
            android.util.Log.d("UserViewModel", "✅ Usuario creado (sincrónico): ${created.email}, cognitoId: ${created.cognitoId}")
            _userState.value = created
            Triple(true, created, null)
        } catch (e: Exception) {
            android.util.Log.e("UserViewModel", "❌ Error creando usuario (sincrónico): ${e.message}", e)
            Triple(false, null, e.message)
        }
    }

    fun updateUser(cognitoId: String, update: UserProfile) {
        val myToken = ++loadToken
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.updateUser(cognitoId, update) }
            }
            if (myToken != loadToken) return@launch

            result.fold(
                onSuccess = { updated -> _userState.value = updated },
                onFailure = { e -> _error.value = e.message ?: "Error al actualizar usuario" }
            )
            _isLoading.value = false
        }
    }

    fun deleteUser(cognitoId: String) {
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.deleteUser(cognitoId) }
            }
            result.fold(
                onSuccess = {
                    clearUser()
                },
                onFailure = { e ->
                    _error.value = e.message ?: "Error al eliminar usuario"
                    _isLoading.value = false
                }
            )
        }
    }

    suspend fun emailExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching { model.emailExists(email) }
                .getOrElse { throw it } // deja que el UI muestre error si falla
        }
    }


    // ============== FAVORITOS: PROMOCIONES ==============

    fun favoritePromotion(promotionId: Int, cognitoId: String) {
        _error.value = null
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { repository.favoritePromotion(promotionId, cognitoId) }
            }

            result.onFailure { e ->
                _error.value = e.message ?: "Error al marcar favorito"
            }.onSuccess {
                // Recargar listas SINCRÓNICAMENTE y ACTUALIZAR estado antes de emitir evento
                val refreshedPromos = runCatching {
                    withContext(Dispatchers.IO) { model.getFavoritePromotions(cognitoId) }
                }.getOrElse { emptyList() }

                _favoritePromotions.value = refreshedPromos

                // Buscar la promo recién agregada para obtener título/negocio
                val added = refreshedPromos.firstOrNull { it.promotionId == promotionId }
                val title = added?.title ?: "Promoción $promotionId"
                val business = added?.businessName

                val timestampIso = OffsetDateTime.now().toString()
                _favoritePromoEvents.emit(
                    FavoritePromoEvent.Added(
                        promotionId = promotionId,
                        title = title,
                        businessName = business,
                        timestampIso = timestampIso
                    )
                )

                // Guardar en historial persistente
                historyService?.addHistoryEvent(
                    userId = cognitoId,
                    type = "FAVORITO_AGREGADO",
                    title = title,
                    subtitle = business ?: "Negocio",
                    iso = timestampIso,
                    promotionId = promotionId,
                    branchId = null
                )

                // Opcional: mantener también la lista de colaboradores favoritos al día
                runCatching {
                    withContext(Dispatchers.IO) { model.getFavoriteCollabs(cognitoId) }
                }.onSuccess { _favoriteCollabs.value = it }
            }
        }
    }

    fun unfavoritePromotion(promotionId: Int, cognitoId: String) {
        _error.value = null
        viewModelScope.launch {
            // Obtener datos de la promo ANTES de quitarla
            val removed = _favoritePromotions.value.firstOrNull { it.promotionId == promotionId }
            val titleBefore = removed?.title ?: "Promoción $promotionId"
            val businessBefore = removed?.businessName

            val result = runCatching {
                withContext(Dispatchers.IO) { repository.unfavoritePromotion(promotionId, cognitoId) }
            }

            result.onFailure { e ->
                _error.value = e.message ?: "Error al quitar favorito"
            }.onSuccess {
                // Recargar listas SINCRÓNICAMENTE y ACTUALIZAR estado antes de emitir evento
                val refreshedPromos = runCatching {
                    withContext(Dispatchers.IO) { model.getFavoritePromotions(cognitoId) }
                }.getOrElse { emptyList() }

                _favoritePromotions.value = refreshedPromos

                val timestampIso = OffsetDateTime.now().toString()
                _favoritePromoEvents.emit(
                    FavoritePromoEvent.Removed(
                        promotionId = promotionId,
                        title = titleBefore,
                        businessName = businessBefore,
                        timestampIso = timestampIso
                    )
                )

                // Guardar en historial persistente
                historyService?.addHistoryEvent(
                    userId = cognitoId,
                    type = "FAVORITO_QUITADO",
                    title = titleBefore,
                    subtitle = businessBefore ?: "Negocio",
                    iso = timestampIso,
                    promotionId = promotionId,
                    branchId = null
                )

                // Opcional: mantener también la lista de colaboradores favoritos al día
                runCatching {
                    withContext(Dispatchers.IO) { model.getFavoriteCollabs(cognitoId) }
                }.onSuccess { _favoriteCollabs.value = it }
            }
        }
    }

    fun getFavoritePromotions(cognitoId: String) {
        val myToken = ++loadToken
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { repository.getFavoritePromotions(cognitoId) }
            }
            if (myToken != loadToken) return@launch

            result.fold(
                onSuccess = { list -> _favoritePromotions.value = list },
                onFailure = { e -> _error.value = e.message ?: "Error al obtener promociones favoritas" }
            )
            _isLoading.value = false
        }
    }

    /** Conveniencia para refrescar ambas listas de favoritos sin pelear con el token global (asincrónico). */
    fun refreshFavorites(cognitoId: String) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { repository.getFavoritePromotions(cognitoId) }
            }.onSuccess { _favoritePromotions.value = it }
                .onFailure { e -> _error.value = e.message ?: "Error al refrescar promociones favoritas" }

            runCatching {
                withContext(Dispatchers.IO) { model.getFavoriteCollabs(cognitoId) }
            }.onSuccess { _favoriteCollabs.value = it }
                .onFailure { e -> _error.value = e.message ?: "Error al refrescar colaboradores favoritos" }

            // Reservaciones
            runCatching {
                withContext(Dispatchers.IO) { RemoteServiceBooking.getUserBookings(cognitoId) }
            }.onSuccess { bookings ->
                _userBookings.value = bookings
                // Cargar detalles de las promociones reservadas
                loadReservedPromotionsDetails(bookings)
            }.onFailure { e -> _error.value = e.message ?: "Error al refrescar reservaciones" }
        }
    }

    /** Carga los detalles completos de las promociones reservadas */
    private suspend fun loadReservedPromotionsDetails(bookings: List<Booking>) {
        val promos = bookings.mapNotNull { booking ->
            booking.promotionId?.let { promotionId ->
                runCatching {
                    withContext(Dispatchers.IO) { RemoteServicePromos.getPromotionById(promotionId) }
                }.getOrNull()
            }
        }
        _reservedPromotions.value = promos
    }

    /** Obtiene las reservaciones del usuario */
    fun getUserBookings(cognitoId: String) {
        viewModelScope.launch {
            _error.value = null
            runCatching {
                withContext(Dispatchers.IO) { RemoteServiceBooking.getUserBookings(cognitoId) }
            }.onSuccess { bookings ->
                _userBookings.value = bookings
                loadReservedPromotionsDetails(bookings)
            }.onFailure { e ->
                _error.value = e.message ?: "Error al obtener reservaciones"
            }
        }
    }

    /** Verifica si una promoción está reservada por el usuario */
    fun isPromotionReserved(promotionId: Int): Boolean {
        return _userBookings.value.any { it.promotionId == promotionId }
    }

    // ============== FAVORITOS: COLABORADORES ==============

    fun favoriteCollaborator(collaboratorId: String, cognitoId: String) {
        _error.value = null
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.favoriteCollaborator(collaboratorId, cognitoId) }
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Error al marcar colaborador como favorito"
            }.onSuccess {
                // Mantén el estado consistente
                refreshFavorites(cognitoId)
            }
        }
    }

    fun unfavoriteCollaborator(collaboratorId: String, cognitoId: String) {
        _error.value = null
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.unfavoriteCollaborator(collaboratorId, cognitoId) }
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Error al quitar colaborador de favoritos"
            }.onSuccess {
                // Mantén el estado consistente
                refreshFavorites(cognitoId)
            }
        }
    }

    fun toggleFavoriteCollaborator(collaboratorId: String, cognitoId: String) {
        val isFav = _favoriteCollabs.value.any { it.cognitoId == collaboratorId }
        if (isFav) unfavoriteCollaborator(collaboratorId, cognitoId)
        else favoriteCollaborator(collaboratorId, cognitoId)
    }

    // ============== EVENTOS PARA HISTORIAL ==============

    sealed class FavoritePromoEvent(
        val promotionId: Int,
        val title: String,
        val businessName: String?,
        val timestampIso: String
    ) {
        class Added(promotionId: Int, title: String, businessName: String?, timestampIso: String)
            : FavoritePromoEvent(promotionId, title, businessName, timestampIso)

        class Removed(promotionId: Int, title: String, businessName: String?, timestampIso: String)
            : FavoritePromoEvent(promotionId, title, businessName, timestampIso)
    }

    // Flujo de eventos: el History lo escuchará
    private val _favoritePromoEvents = MutableSharedFlow<FavoritePromoEvent>(replay = 20)
    val favoritePromoEvents: SharedFlow<FavoritePromoEvent> = _favoritePromoEvents
}
