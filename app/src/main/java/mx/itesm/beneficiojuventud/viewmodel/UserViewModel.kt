package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.model.users.UserProfile

class UserViewModel : ViewModel() {

    private val model = RemoteServiceUser

    private val _userState = MutableStateFlow(UserProfile())
    val userState: StateFlow<UserProfile> = _userState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _favoritePromotions = MutableStateFlow<List<Promotions>>(emptyList())
    val favoritePromotions: StateFlow<List<Promotions>> = _favoritePromotions

    private val _favoriteCollabs = MutableStateFlow<List<String>>(emptyList())
    val favoriteCollabs: StateFlow<List<String>> = _favoriteCollabs

    /** Token para invalidar respuestas tardías cuando cambia de cuenta o se hace clear. */
    private var loadToken: Int = 0

    /** Limpia el perfil y **anula** cualquier request en curso. */
    fun clearUser() {
        loadToken++
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
            if (myToken != loadToken) return@launch

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
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.deleteUser(cognitoId) }
            }
            result.fold(
                onSuccess = { clearUser() },
                onFailure = { e ->
                    _error.value = e.message ?: "Error al eliminar usuario"
                    _isLoading.value = false
                }
            )
        }
    }

    fun favoritePromotion(promotionId: Int, cognitoId: String) {
        _error.value = null
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.favoritePromotion(promotionId, cognitoId) }
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Error al marcar favorito"
            }.onSuccess {
                refreshFavorites(cognitoId)
            }
        }
    }

    fun unfavoritePromotion(promotionId: Int, cognitoId: String) {
        _error.value = null
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.unfavoritePromotion(promotionId, cognitoId) }
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Error al quitar favorito"
            }.onSuccess {
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
                withContext(Dispatchers.IO) { model.getFavoritePromotions(cognitoId) }
            }
            if (myToken != loadToken) return@launch

            result.fold(
                onSuccess = { list -> _favoritePromotions.value = list },
                onFailure = { e -> _error.value = e.message ?: "Error al obtener promociones favoritas" }
            )
            _isLoading.value = false
        }
    }

    fun getFavoriteCollabs(cognitoId: String) {
        val myToken = ++loadToken
        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.getFavoriteCollabs(cognitoId) }
            }
            if (myToken != loadToken) return@launch

            result.fold(
                onSuccess = { ids -> _favoriteCollabs.value = ids },
                onFailure = { e -> _error.value = e.message ?: "Error al obtener colaboradores favoritos" }
            )
            _isLoading.value = false
        }
    }

    fun refreshFavorites(cognitoId: String) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { model.getFavoritePromotions(cognitoId) }
            }.onSuccess { _favoritePromotions.value = it }
                .onFailure { e -> _error.value = e.message ?: "Error al refrescar promociones favoritas" }

            runCatching {
                withContext(Dispatchers.IO) { model.getFavoriteCollabs(cognitoId) }
            }.onSuccess { _favoriteCollabs.value = it }
                .onFailure { e -> _error.value = e.message ?: "Error al refrescar colaboradores favoritos" }
        }
    }

    // --- COLLABORATORS FAVORITES ---

    fun favoriteCollaborator(collaboratorId: String, cognitoId: String) {
        _error.value = null
        viewModelScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { model.favoriteCollaborator(collaboratorId, cognitoId) }
            }
            result.onFailure { e ->
                _error.value = e.message ?: "Error al marcar colaborador como favorito"
            }.onSuccess {
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
                refreshFavorites(cognitoId)
            }
        }
    }

    fun toggleFavoriteCollaborator(collaboratorId: String, cognitoId: String) {
        val isFav = _favoriteCollabs.value.contains(collaboratorId)
        if (isFav) unfavoriteCollaborator(collaboratorId, cognitoId)
        else favoriteCollaborator(collaboratorId, cognitoId)
    }
}
