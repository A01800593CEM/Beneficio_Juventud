package mx.itesm.beneficiojuventud.viewmodel

import android.util.Log
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
        telefono: String? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Iniciando signUp para: $email")
                _authState.value = AuthState(isLoading = true)

                val result = authRepository.signUp(email, password, telefono)

                result.fold(
                    onSuccess = { signUpResult ->
                        Log.d("AuthViewModel", "SignUp exitoso: needsConfirmation=${!signUpResult.isSignUpComplete}")
                        _authState.value = AuthState(
                            isSuccess = signUpResult.isSignUpComplete,
                            needsConfirmation = !signUpResult.isSignUpComplete
                        )
                    },
                    onFailure = { error ->
                        Log.e("AuthViewModel", "SignUp falló: ${error.message}", error)
                        _authState.value = AuthState(
                            error = error.message ?: "Error desconocido al registrar"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error inesperado en signUp: ${e.message}", e)
                _authState.value = AuthState(
                    error = "Error de conexión. Verifica tu internet e intenta de nuevo."
                )
            }
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

    // en AuthViewModel
    fun resendSignUpCode(email: String) {
        viewModelScope.launch {
            // mostramos loading, pero NO marcamos isSuccess para no navegar
            _authState.value = AuthState(isLoading = true)

            val result = authRepository.resendSignUpCode(email)

            result.fold(
                onSuccess = {
                    // Limpia a un estado "neutro" que puedes interpretar como "listo, re-enviado"
                    // Si quieres mostrar un mensaje de éxito, puedes manejarlo en UI con un Snackbar/Toast.
                    _authState.value = AuthState(needsConfirmation = true)
                },
                onFailure = { error ->
                    _authState.value = AuthState(
                        error = error.message ?: "No se pudo reenviar el código"
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
            try {
                Log.d("AuthViewModel", "Iniciando signIn para: $email")
                _authState.value = AuthState(isLoading = true)

                val result = authRepository.signIn(email, password)

                result.fold(
                    onSuccess = { signInResult ->
                        Log.d("AuthViewModel", "SignIn exitoso: isSignedIn=${signInResult.isSignedIn}")
                        _authState.value = AuthState(
                            isSuccess = signInResult.isSignedIn,
                            needsConfirmation = !signInResult.isSignedIn
                        )
                    },
                    onFailure = { error ->
                        Log.e("AuthViewModel", "SignIn falló: ${error.message}", error)
                        _authState.value = AuthState(
                            error = error.message ?: "Credenciales incorrectas"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error inesperado en signIn: ${e.message}", e)
                _authState.value = AuthState(
                    error = "Error de conexión. Verifica tu internet e intenta de nuevo."
                )
            }
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
     * Obtener información del usuario actual
     */
    fun getCurrentUser() {
        // Solo obtener si no hemos obtenido ya o si estamos en un estado inicial
        if (currentUser == null && !_authState.value.isLoading) {
            viewModelScope.launch {
                val result = authRepository.getCurrentUser()
                result.fold(
                    onSuccess = { user ->
                        // Usuario obtenido exitosamente, mantener estado actual
                        currentUser = user?.username
                    },
                    onFailure = { error ->
                        // Error obteniendo usuario, posiblemente no autenticado
                        currentUser = null
                    }
                )
            }
        }
    }

    private var currentUser: String? = null

    fun getCurrentUserName(): String? = currentUser

    /**
     * Limpiar estado
     */
    fun clearState() {
        _authState.value = AuthState()
        // También limpiar información del usuario al hacer logout
        if (!_authState.value.isSuccess) {
            currentUser = null
        }
    }
}