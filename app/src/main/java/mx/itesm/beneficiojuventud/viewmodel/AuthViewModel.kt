package mx.itesm.beneficiojuventud.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.itesm.beneficiojuventud.model.auth.AuthRepository
import mx.itesm.beneficiojuventud.model.auth.AuthState
import mx.itesm.beneficiojuventud.model.users.UserProfile
import java.util.UUID

/** Estado global de la app. */
data class AppState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val hasCheckedAuth: Boolean = false
)

/** ViewModel unificado para autenticación y estado global de sesión. */
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

    // ===== Llave de sesión (para invalidar estados remember/rememberSaveable) =====
    private val _sessionKey = MutableStateFlow(UUID.randomUUID().toString())
    val sessionKey: StateFlow<String> = _sessionKey.asStateFlow()

    // ===== Datos temporales durante registro =====
    private var _pendingUserProfile: UserProfile? = null
    val pendingUserProfile: UserProfile? get() = _pendingUserProfile

    // Credenciales temporales SOLO en memoria (no BD)
    private var _pendingEmail: String? = null
    private var _pendingPlainPassword: String? = null

    fun setPendingCredentials(email: String, password: String) {
        _pendingEmail = email
        _pendingPlainPassword = password
    }
    fun clearPendingCredentials() {
        _pendingEmail = null
        _pendingPlainPassword = null
    }

    init {
        refreshAuthState()
    }

    // =========================================================
    // ========     MÉTODOS DE CONTROL GLOBAL (APP)     ========
    // =========================================================
    fun refreshAuthState() {
        viewModelScope.launch {
            _appState.value = _appState.value.copy(isLoading = true)
            val isSignedIn = authRepository.isUserSignedIn()

            _appState.value = AppState(
                isLoading = false,
                isAuthenticated = isSignedIn,
                hasCheckedAuth = true
            )
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
                        val sub = r.userId // sub de Cognito (cognitoId)
                        _currentUserId.value = sub
                        _authState.value = AuthState(
                            isSuccess = r.isSignUpComplete,
                            needsConfirmation = !r.isSignUpComplete,
                            cognitoSub = sub
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
            val priorSub = _authState.value.cognitoSub
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val result = authRepository.confirmSignUp(email, code)
            result.fold(
                onSuccess = { isComplete ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isSuccess = isComplete,
                        needsConfirmation = false,
                        cognitoSub = priorSub
                    )

                    val pendingEmail = _pendingEmail
                    val pendingPw = _pendingPlainPassword
                    if (isComplete && !pendingEmail.isNullOrBlank() && !pendingPw.isNullOrBlank()) {
                        viewModelScope.launch {
                            val signInResult = authRepository.signIn(pendingEmail, pendingPw)
                            signInResult.fold(
                                onSuccess = { r ->
                                    if (r.isSignedIn) {
                                        _sessionKey.value = UUID.randomUUID().toString()
                                        refreshAuthState()
                                    } else {
                                        _authState.value = _authState.value.copy(
                                            isSuccess = false,
                                            needsConfirmation = true
                                        )
                                    }
                                    clearPendingCredentials()
                                },
                                onFailure = { e ->
                                    _authState.value = _authState.value.copy(
                                        isSuccess = false,
                                        error = e.message ?: "No se pudo iniciar sesión automáticamente"
                                    )
                                    clearPendingCredentials()
                                }
                            )
                        }
                    }
                },
                onFailure = { e ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = e.message ?: "Código de confirmación inválido"
                    )
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
                            _authState.value = AuthState(isSuccess = true)
                            _sessionKey.value = UUID.randomUUID().toString() // nueva sesión
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
            if (_authState.value.isLoading) return@launch
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            val result = runCatching {
                withContext(Dispatchers.IO) {
                    authRepository.signOut(globalSignOut).getOrThrow()
                }
            }

            result.fold(
                onSuccess = {
                    _currentUser.value = null
                    _currentUserId.value = null
                    _pendingUserProfile = null
                    clearPendingCredentials()
                    _sessionKey.value = UUID.randomUUID().toString()
                    _authState.value = AuthState(isLoading = false)
                    refreshAuthState()
                },
                onFailure = { e ->
                    val msg = e.message.orEmpty()
                    val alreadySignedOut =
                        msg.contains("SignedOutException", ignoreCase = true) ||
                                msg.contains("currently signed out", ignoreCase = true)

                    if (alreadySignedOut) {
                        _currentUser.value = null
                        _currentUserId.value = null
                        _pendingUserProfile = null
                        clearPendingCredentials()
                        _sessionKey.value = UUID.randomUUID().toString()
                        _authState.value = AuthState(isLoading = false)
                        refreshAuthState()
                    } else {
                        _authState.value = AuthState(
                            isLoading = false,
                            error = msg.ifEmpty { "Error al cerrar sesión" }
                        )
                    }
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
