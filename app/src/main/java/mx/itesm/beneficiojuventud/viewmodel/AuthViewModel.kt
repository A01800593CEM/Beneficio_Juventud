package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.AuthState
import mx.itesm.beneficiojuventud.model.AuthRepository


class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Registrar usuario
     */
    fun signUp(
        email: String,
        password: String,
        nombreCompleto: String,
        telefono: String? = null
    ) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.signUp(email, password, nombreCompleto, telefono)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(
                        isSuccess = true,
                        needsConfirmation = true
                    )
                },
                onFailure = { error ->
                    _authState.value = AuthState(
                        error = error.message ?: "Error desconocido al registrar"
                    )
                }
            )
        }
    }

    /**
     * Confirmar registro
     */
    fun confirmSignUp(email: String, code: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.confirmSignUp(email, code)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { error ->
                    _authState.value = AuthState(
                        error = error.message ?: "Código de confirmación inválido"
                    )
                }
            )
        }
    }

    /**
     * Iniciar sesión
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.signIn(email, password)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { error ->
                    _authState.value = AuthState(
                        error = error.message ?: "Credenciales incorrectas"
                    )
                }
            )
        }
    }

    /**
     * Cerrar sesión
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState()
        }
    }

    /**
     * Resetear contraseña
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.resetPassword(email)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(
                        isSuccess = true,
                        needsConfirmation = true
                    )
                },
                onFailure = { error ->
                    _authState.value = AuthState(
                        error = error.message ?: "Error al resetear contraseña"
                    )
                }
            )
        }
    }


    /**
     * Limpiar estado
     */
    fun clearState() {
        _authState.value = AuthState()
    }
}