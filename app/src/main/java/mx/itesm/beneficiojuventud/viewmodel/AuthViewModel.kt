package mx.itesm.beneficiojuventud.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.AuthRepository
import mx.itesm.beneficiojuventud.model.AuthState
import mx.itesm.beneficiojuventud.model.UserProfile

// Estado global de la app
data class AppState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val hasCheckedAuth: Boolean = false
)

/**
 * ViewModel unificado para autenticación y estado global de sesión.
 */
class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // ===== Estado global de la app =====
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    // ===== Estado de autenticación =====
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // ===== Usuario actual =====
    private val _currentUser = MutableStateFlow<String?>(null)
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // ===== Datos temporales durante registro =====
    private var _pendingUserProfile: UserProfile? = null
    val pendingUserProfile: UserProfile? get() = _pendingUserProfile

    init {
        // Revisa el estado de autenticación al iniciar
        refreshAuthState()
    }

    // =========================================================
    // ========     MÉTODOS DE CONTROL GLOBAL (APP)     ========
    // =========================================================
    fun refreshAuthState() {
        viewModelScope.launch {
            // 1) Marca loading en la app
            _appState.value = _appState.value.copy(isLoading = true)

            // 2) Pregunta si hay sesión
            val isSignedIn = authRepository.isUserSignedIn()

            // 3) Actualiza estados de auth y app
            _authState.value = if (isSignedIn) {
                AuthState(isSuccess = true)
            } else {
                AuthState()
            }

            _appState.value = AppState(
                isLoading = false,
                isAuthenticated = isSignedIn,
                hasCheckedAuth = true
            )

            // 4) Carga usuario (opcional pero útil)
            getCurrentUser()
        }
    }

    fun savePendingUserProfile(userProfile: UserProfile) {
        _pendingUserProfile = userProfile
    }

    fun consumePendingUserProfile(): UserProfile? {
        val profile = _pendingUserProfile
        _pendingUserProfile = null
        return profile
    }

    fun clearPendingUserProfile() {
        _pendingUserProfile = null
    }

    // =========================================================
    // ========        FLUJO DE AUTENTICACIÓN          ========
    // =========================================================

    fun signUp(email: String, password: String, telefono: String? = null) {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Iniciando signUp para: $email")
                _authState.value = AuthState(isLoading = true)

                val result = authRepository.signUp(email, password, telefono)
                result.fold(
                    onSuccess = { r ->
                        Log.d("AuthViewModel", "SignUp exitoso: needsConfirmation=${!r.isSignUpComplete}")
                        _authState.value = AuthState(
                            isSuccess = r.isSignUpComplete,
                            needsConfirmation = !r.isSignUpComplete
                        )
                    },
                    onFailure = { e ->
                        Log.e("AuthViewModel", "SignUp falló: ${e.message}", e)
                        _authState.value = AuthState(error = e.message ?: "Error desconocido al registrar")
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error inesperado en signUp: ${e.message}", e)
                _authState.value = AuthState(error = "Error de conexión. Verifica tu internet e intenta de nuevo.")
            }
        }
    }

    fun confirmSignUp(email: String, code: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = authRepository.confirmSignUp(email, code)
            result.fold(
                onSuccess = { _authState.value = AuthState(isSuccess = true) },
                onFailure = { e ->
                    _authState.value = AuthState(error = e.message ?: "Código de confirmación inválido")
                }
            )
        }
    }

    fun resendSignUpCode(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = authRepository.resendSignUpCode(email)
            result.fold(
                onSuccess = {
                    _authState.value = AuthState(needsConfirmation = true)
                },
                onFailure = { e ->
                    _authState.value = AuthState(error = e.message ?: "No se pudo reenviar el código")
                }
            )
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Iniciando signIn para: $email")
                _authState.value = AuthState(isLoading = true)

                val result = authRepository.signIn(email, password)
                result.fold(
                    onSuccess = { r ->
                        Log.d("AuthViewModel", "SignIn exitoso: isSignedIn=${r.isSignedIn}")
                        if (r.isSignedIn) {
                            // Recalcula AppState y carga usuario
                            refreshAuthState()
                        } else {
                            _authState.value = AuthState(needsConfirmation = true)
                        }
                    },
                    onFailure = { e ->
                        Log.e("AuthViewModel", "SignIn falló: ${e.message}", e)
                        _authState.value = AuthState(error = e.message ?: "Credenciales incorrectas")
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error inesperado en signIn: ${e.message}", e)
                _authState.value = AuthState(error = "Error de conexión. Verifica tu internet e intenta de nuevo.")
            }
        }
    }

    fun signOut(globalSignOut: Boolean = true) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = authRepository.signOut(globalSignOut)
            result.fold(
                onSuccess = {
                    _authState.value = AuthState()
                    _currentUser.value = null
                    _currentUserId.value = null
                    _pendingUserProfile = null
                    // Recalcula AppState para mandar al Login
                    refreshAuthState()
                },
                onFailure = { e ->
                    _authState.value = AuthState(error = e.message ?: "Error al cerrar sesión")
                }
            )
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = authRepository.resetPassword(email)
            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true, needsConfirmation = true)
                },
                onFailure = { e ->
                    _authState.value = AuthState(error = e.message ?: "Error al resetear contraseña")
                }
            )
        }
    }

    fun confirmResetPassword(email: String, newPassword: String, confirmationCode: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = authRepository.confirmResetPassword(email, newPassword, confirmationCode)
            result.fold(
                onSuccess = { _authState.value = AuthState(isSuccess = true) },
                onFailure = { e ->
                    _authState.value = AuthState(error = e.message ?: "Error al confirmar nueva contraseña")
                }
            )
        }
    }

    fun updatePassword(existingPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = authRepository.updatePassword(existingPassword, newPassword)
            result.fold(
                onSuccess = { _authState.value = AuthState(isSuccess = true) },
                onFailure = { e ->
                    _authState.value = AuthState(error = e.message ?: "Error al actualizar contraseña")
                }
            )
        }
    }

    // =========================================================
    // ========        INFORMACIÓN DEL USUARIO         ========
    // =========================================================
    fun getCurrentUser() {
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user?.username
                    _currentUserId.value = user?.userId
                    Log.d(
                        "AuthViewModel",
                        "User loaded: username=${_currentUser.value}, userId=${_currentUserId.value}"
                    )
                },
                onFailure = {
                    _currentUser.value = null
                    _currentUserId.value = null
                    Log.d("AuthViewModel", "User not found or not authenticated")
                }
            )
        }
    }

    fun getCurrentUserName(): String? = _currentUser.value
    fun getCurrentUserId(): String? = _currentUserId.value

    fun clearState() {
        _authState.value = AuthState()
        _currentUser.value = null
        _currentUserId.value = null
        _pendingUserProfile = null
        Log.d("AuthViewModel", "State cleared")
    }
}
