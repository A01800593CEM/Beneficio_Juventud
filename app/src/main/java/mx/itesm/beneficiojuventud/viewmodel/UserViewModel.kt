package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.itesm.beneficiojuventud.model.SavedCouponRepository
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.model.users.UserProfile

class UserViewModel(private val repository: SavedCouponRepository) : ViewModel() {

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
            val result = runCatching {
                withContext(Dispatchers.IO) { model.getUserById(cognitoId) }
            }
            if (myToken != loadToken) return@launch // llegó tarde, se descarta

            result.fold(
                onSuccess = { user -> _userState.value = user },
                onFailure = { e -> _error.value = e.message ?: "Error al cargar usuario" }
            )
            _isLoading.value = false
        }
    }

    fun createUser(user: UserProfile) {
        val myToken = ++loadToken
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.createUser(user) }
            }
            if (myToken != loadToken) return@launch

            result.fold(
                onSuccess = { created -> _userState.value = created },
                onFailure = { e -> _error.value = e.message ?: "Error al crear usuario" }
            )
            _isLoading.value = false
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
        // Borrar no necesita tomar ownership del token; pero limpiamos estado local si aplica.
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.deleteUser(cognitoId) }
            }
            result.fold(
                onSuccess = {
                    // Si borraste el usuario actual, deja el state limpio
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


    fun favoritePromotion(promotionId: Int, cognitoId: String) {
        _error.value = null
        // No activamos el loading global para no bloquear la UI por una acción rápida.
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { repository.favoritePromotion(promotionId, cognitoId) }
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Error al marcar favorito"
            }.onSuccess {
                // Refrescamos listas para mantener consistencia
                refreshFavorites(cognitoId)
            }
        }
    }

    fun unfavoritePromotion(promotionId: Int, cognitoId: String) {
        _error.value = null
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { repository.unfavoritePromotion(promotionId, cognitoId) }
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Error al quitar favorito"
            }.onSuccess {
                // Refrescamos listas para mantener consistencia
                refreshFavorites(cognitoId)
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

    /** Conveniencia para refrescar ambas listas de favoritos sin pelear con el token global. */
    fun refreshFavorites(cognitoId: String) {
        // No tocamos loadToken aquí para no invalidar otras cargas largas.
        viewModelScope.launch {
            // Promos
            runCatching {
                withContext(Dispatchers.IO) { repository.getFavoritePromotions(cognitoId) }
            }.onSuccess { _favoritePromotions.value = it }
                .onFailure { e -> _error.value = e.message ?: "Error al refrescar promociones favoritas" }

            // Collabs (List<Collaborator>)
            runCatching {
                withContext(Dispatchers.IO) { model.getFavoriteCollabs(cognitoId) }
            }.onSuccess { _favoriteCollabs.value = it }
                .onFailure { e -> _error.value = e.message ?: "Error al refrescar colaboradores favoritos" }
        }
    }

    // --- COLLABORATORS FAVORITES ---

    // AHORA: collaboratorId es String (cognitoId), no Int
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
        // Como ahora _favoriteCollabs es List<Collaborator>, comparamos por cognitoId
        val isFav = _favoriteCollabs.value.any { it.cognitoId == collaboratorId }
        if (isFav) unfavoriteCollaborator(collaboratorId, cognitoId)
        else favoriteCollaborator(collaboratorId, cognitoId)
    }
}
