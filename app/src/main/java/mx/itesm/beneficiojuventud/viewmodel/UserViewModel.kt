package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
}
