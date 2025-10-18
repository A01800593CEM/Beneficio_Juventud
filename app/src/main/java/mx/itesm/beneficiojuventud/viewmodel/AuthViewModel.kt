package mx.itesm.beneficiojuventud.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    /**
     * SignUp: usa AuthRepository.signUp(email, password) que devuelve Result<AuthSignUpResult>.
     * Desempaquetamos con getOrThrow() y tomamos result.userId como cognitoSub.
     */
    fun signUp(
        email: String,
        password: String,
        attributes: Map<String, String> = emptyMap() // se mantiene para compatibilidad, no se usa aquí
    ) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                // El repo devuelve Result<AuthSignUpResult>
                val result = authRepository.signUp(email, password)
                    .getOrThrow() // 👈 desempaquetar

                // Amplify.Auth.signUp() -> AuthSignUpResult
                // En tu repo ya logueas: isSignUpComplete, userId, nextStep
                val requiresConfirm = !result.isSignUpComplete

                if (requiresConfirm) {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            needsConfirmation = true,
                            error = null,
                            cognitoSub = result.userId // 👈 userSub correcto
                        )
                    }
                } else {
                    // Auto-confirm sin OTP
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            needsConfirmation = false,
                            error = null,
                            cognitoSub = result.userId // 👈 userSub correcto
                        )
                    }
                }
            } catch (e: Exception) {
                val msg = (e.message ?: e.toString())
                val looksLikeExists = msg.contains("UsernameExistsException", true) ||
                        msg.contains("Username already exists", true) ||
                        msg.contains("User already exists", true)

                if (looksLikeExists) {
                    // Si UNCONFIRMED → resend; si ya confirmado → error claro.
                    handleUsernameExists(email)
                } else {
                    _authState.update { it.copy(isLoading = false, error = msg) }
                }
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
            val priorSub = _authState.value.cognitoSub
            _authState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.resendSignUpCode(email)
            result.fold(
                onSuccess = {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            needsConfirmation = true,
                            error = null,
                            cognitoSub = priorSub // 👈 conservar sub
                        )
                    }
                },
                onFailure = { e ->
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "No se pudo reenviar el código",
                            cognitoSub = priorSub // 👈 conservar sub aunque falle
                        )
                    }
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

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    /** Intenta distinguir si el email existe como UNCONFIRMED o CONFIRMED. */
    private fun resolveExistingAccountOnSignUp(email: String) {
        val pw = _pendingPlainPassword

        // Sin password: fallback → reenviamos código y navegamos a confirm.
        if (pw.isNullOrBlank()) {
            resendSignUpCode(email)
            _authState.value = AuthState(needsConfirmation = true)
            return
        }

        viewModelScope.launch {
            val signInAttempt = authRepository.signIn(email, pw)
            signInAttempt.fold(
                onSuccess = { r ->
                    if (r.isSignedIn) {
                        // Confirmado y la contraseña coincide
                        _authState.value = AuthState(
                            isSuccess = false,
                            error = "Esta cuenta ya está confirmada. Inicia sesión."
                        )
                    } else {
                        // Estado intermedio (MFA/challenge) → tratamos como confirmado
                        _authState.value = AuthState(
                            isSuccess = false,
                            error = "Esta cuenta ya está confirmada. Inicia sesión."
                        )
                    }
                },
                onFailure = { e ->
                    val msg = e.message?.lowercase().orEmpty()
                    when {
                        "usernotconfirmed" in msg || "not confirmed" in msg -> {
                            // UNCONFIRMED → reenviar código y mandar a Confirm
                            resendSignUpCode(email)
                            _authState.value = AuthState(needsConfirmation = true)
                        }
                        "notauthorized" in msg || "incorrect username or password" in msg -> {
                            _authState.value = AuthState(
                                isSuccess = false,
                                error = "La cuenta ya existe y está confirmada. Inicia sesión o restablece tu contraseña."
                            )
                        }
                        else -> {
                            _authState.value = AuthState(
                                isSuccess = false,
                                error = e.message ?: "La cuenta ya existe. Intenta iniciar sesión."
                            )
                        }
                    }
                }
            )
        }
    }

    /** Marca el estado de autenticación como inactivo (detiene spinners). */
    fun markIdle() {
        _authState.update { it.copy(isLoading = false) }
    }


    /**
     * Maneja UsernameExistsException: intenta resend (si UNCONFIRMED) y setea needsConfirmation=true.
     * Si ya está confirmado, devolverá error claro.
     */
    private fun handleUsernameExists(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(needsConfirmation = false) }
            try {
                // Si el usuario está UNCONFIRMED, esto funciona y envía el código
                authRepository.resendSignUpCode(email).getOrThrow()

                // 👇 Estado correcto para que Register navegue a Confirm
                _authState.update {
                    it.copy(
                        isLoading = false,
                        needsConfirmation = true, // <- CLAVE
                        isSuccess = false,
                        error = null
                    )
                }
            } catch (ex: Exception) {
                val t = (ex.message ?: ex.toString()).lowercase()
                val alreadyConfirmed =
                    "already confirmed" in t ||
                            "invalidparameter" in t ||
                            "code delivery failure" in t ||
                            "cannot resend" in t

                _authState.update {
                    it.copy(
                        isLoading = false,
                        needsConfirmation = false,
                        cognitoSub = it.cognitoSub,
                        error = if (alreadyConfirmed)
                            "Esta cuenta ya está confirmada. Inicia sesión o restablece tu contraseña."
                        else ex.message ?: "No fue posible reenviar el código de confirmación."
                    )
                }
            }
        }
    }

}
