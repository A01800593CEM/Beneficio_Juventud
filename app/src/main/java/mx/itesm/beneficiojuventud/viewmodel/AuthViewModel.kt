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
     * Registrar usuario siguiendo las mejores prácticas de Amplify
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
                onSuccess = { signUpResult ->
                    _authState.value = AuthState(
                        isSuccess = signUpResult.isSignUpComplete,
                        needsConfirmation = !signUpResult.isSignUpComplete
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
     * Iniciar sesión siguiendo las mejores prácticas de Amplify
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.signIn(email, password)

            result.fold(
                onSuccess = { signInResult ->
                    _authState.value = AuthState(
                        isSuccess = signInResult.isSignedIn,
                        needsConfirmation = !signInResult.isSignedIn
                    )
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
    fun signOut(globalSignOut: Boolean = true) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.signOut(globalSignOut)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState()
                },
                onFailure = { error ->
                    _authState.value = AuthState(
                        error = error.message ?: "Error al cerrar sesión"
                    )
                }
            )
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
     * Confirmar nueva contraseña
     */
    fun confirmResetPassword(email: String, newPassword: String, confirmationCode: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.confirmResetPassword(email, newPassword, confirmationCode)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { error ->
                    _authState.value = AuthState(
                        error = error.message ?: "Error al confirmar nueva contraseña"
                    )
                }
            )
        }
    }


    /**
     * Actualizar contraseña del usuario autenticado
     */
    fun updatePassword(existingPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.updatePassword(existingPassword, newPassword)

            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { error ->
                    _authState.value = AuthState(
                        error = error.message ?: "Error al actualizar contraseña"
                    )
                }
            )
        }
    }

    /**
     * Verificar si el usuario está autenticado al iniciar la app
     */
    fun checkAuthState() {
        viewModelScope.launch {
            val isSignedIn = authRepository.isUserSignedIn()
            if (isSignedIn) {
                _authState.value = AuthState(isSuccess = true)
            } else {
                _authState.value = AuthState()
            }
        }
    }

    /**
     * Limpiar estado
     */
    fun clearState() {
        _authState.value = AuthState()
    }
}