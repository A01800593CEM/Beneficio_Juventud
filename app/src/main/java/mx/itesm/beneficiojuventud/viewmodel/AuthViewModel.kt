package mx.itesm.beneficiojuventud.viewmodel

import android.app.Activity
import android.content.Context
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
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.users.UserProfile
import mx.itesm.beneficiojuventud.utils.UserPreferencesManager
import java.util.UUID

/** Datos del usuario obtenidos de Google Sign-In */
data class GoogleUserData(
    val email: String,
    val givenName: String? = null,
    val familyName: String? = null,
    val name: String? = null
)

/** Estado global de la app. */
data class AppState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val hasCheckedAuth: Boolean = false
)

/** ViewModel unificado para autenticación y estado global de sesión. */
class AuthViewModel(private val context: Context? = null) : ViewModel() {

    private val authRepository = AuthRepository()
    private var preferencesManager: UserPreferencesManager? = null

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

    // Datos temporales durante registro de colaboradores
    private var _pendingCollabProfile: Collaborator? = null
    val pendingCollabProfile: Collaborator? get() = _pendingCollabProfile

    // Datos temporales de Google Sign-In
    private var _pendingGoogleUserData: GoogleUserData? = null
    val pendingGoogleUserData: GoogleUserData? get() = _pendingGoogleUserData


    // Credenciales temporales SOLO en memoria (no BD)
    private var _pendingEmail: String? = null
    private var _pendingPlainPassword: String? = null

    fun setPendingCredentials(email: String, password: String) {
        _pendingEmail = email
        _pendingPlainPassword = password
    }
    fun getPendingCredentials(): Pair<String?, String?> {
        return Pair(_pendingEmail, _pendingPlainPassword)
    }
    fun clearPendingCredentials() {
        _pendingEmail = null
        _pendingPlainPassword = null
    }

    init {
        context?.let {
            preferencesManager = UserPreferencesManager(it)
        }
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

    // Gestión del perfil de colaborador pendiente durante el registro
    fun savePendingCollabProfile(collab: Collaborator) {
        _pendingCollabProfile = collab
    }

    fun consumePendingCollabProfile(): Collaborator? {
        val profile = _pendingCollabProfile
        _pendingCollabProfile = null
        return profile
    }

    fun clearPendingCollabProfile() {
        _pendingCollabProfile = null
    }

    // Gestión de datos de Google Sign-In
    fun savePendingGoogleUserData(googleData: GoogleUserData) {
        Log.d("AuthViewModel", "💾 savePendingGoogleUserData() - Guardando: email=${googleData.email}, givenName=${googleData.givenName}, familyName=${googleData.familyName}")
        _pendingGoogleUserData = googleData
        Log.d("AuthViewModel", "💾 Después de guardar - _pendingGoogleUserData: ${_pendingGoogleUserData?.email}")
    }

    fun consumePendingGoogleUserData(): GoogleUserData? {
        val data = _pendingGoogleUserData
        Log.d("AuthViewModel", "📤 consumePendingGoogleUserData() - Consumiendo: ${data?.email}")
        _pendingGoogleUserData = null
        Log.d("AuthViewModel", "📤 Después de consumir - _pendingGoogleUserData: $_pendingGoogleUserData")
        return data
    }

    fun clearPendingGoogleUserData() {
        Log.d("AuthViewModel", "🗑️ clearPendingGoogleUserData() - Antes: ${_pendingGoogleUserData?.email}")
        Log.d("AuthViewModel", "🗑️ Stack trace:", Exception("Trace"))
        _pendingGoogleUserData = null
        Log.d("AuthViewModel", "🗑️ Después de limpiar - _pendingGoogleUserData: $_pendingGoogleUserData")
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
                                        error = translateAuthError(e.message ?: "No se pudo iniciar sesión automáticamente")
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
                        error = translateAuthError(e.message ?: "Código de confirmación inválido")
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
                            error = translateAuthError(e.message ?: "No se pudo reenviar el código"),
                            cognitoSub = priorSub // 👈 conservar sub aunque falle
                        )
                    }
                }
            )
        }
    }


    fun signIn(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Iniciando signIn para: $email")
                _authState.value = AuthState(isLoading = true)

                val result = authRepository.signIn(email, password)
                result.fold(
                    onSuccess = { r ->
                        Log.d("AuthViewModel", "SignIn exitoso: isSignedIn=${r.isSignedIn}")
                        if (r.isSignedIn) {
                            // Guardar credenciales si el usuario marcó "Recuérdame"
                            if (rememberMe) {
                                preferencesManager?.saveCredentials(email, password)
                                Log.d("AuthViewModel", "Credenciales guardadas para 'Recuérdame'")
                            } else {
                                // Limpiar credenciales si no marcó "Recuérdame"
                                preferencesManager?.clearCredentials()
                            }

                            _authState.value = AuthState(isSuccess = true)
                            _sessionKey.value = UUID.randomUUID().toString() // nueva sesión
                            refreshAuthState()
                        } else {
                            _authState.value = AuthState(needsConfirmation = true)
                        }
                    },
                    onFailure = { e ->
                        Log.e("AuthViewModel", "SignIn falló: ${e.message}", e)
                        val errorMessage = translateAuthError(e.message ?: "")
                        _authState.value = AuthState(error = errorMessage)
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error inesperado en signIn: ${e.message}", e)
                _authState.value = AuthState(error = "Error de conexión. Verifica tu internet e intenta de nuevo.")
            }
        }
    }

    /**
     * Iniciar sesión con Google usando AWS Cognito Hosted UI
     * Requiere Activity context para mostrar el WebView de autenticación
     *
     * Tras el login exitoso:
     * - Obtiene los atributos del usuario (email, nombre, apellidos)
     * - Los guarda temporalmente en _pendingGoogleUserData
     * - El estado isSuccess indica que la UI debe navegar (a GoogleRegister o PostLoginPermissions)
     */
    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "🔵 Iniciando Google Sign-In")
                Log.d("AuthViewModel", "🔍 AuthViewModel instance: ${this@AuthViewModel.hashCode()}")
                Log.d("AuthViewModel", "Activity: ${activity.javaClass.simpleName}")
                _authState.value = AuthState(isLoading = true)

                val result = authRepository.signInWithGoogle(activity)
                result.fold(
                    onSuccess = { r ->
                        Log.d("AuthViewModel", "✅ Google Sign-In exitoso: isSignedIn=${r.isSignedIn}")
                        if (r.isSignedIn) {
                            // Obtener atributos del usuario de Google
                            viewModelScope.launch {
                                val attrsResult = authRepository.fetchUserAttributesMap()
                                attrsResult.fold(
                                    onSuccess = { attrs ->
                                        val email = attrs["email"] ?: ""
                                        val givenName = attrs["given_name"]
                                        val familyName = attrs["family_name"]
                                        val name = attrs["name"]

                                        Log.d("AuthViewModel", "📧 Email: $email")
                                        Log.d("AuthViewModel", "👤 Given Name: $givenName")
                                        Log.d("AuthViewModel", "👤 Family Name: $familyName")
                                        Log.d("AuthViewModel", "👤 Full Name: $name")

                                        // Guardar datos de Google
                                        Log.d("AuthViewModel", "💾 signInWithGoogle() - Asignando directamente _pendingGoogleUserData")
                                        _pendingGoogleUserData = GoogleUserData(
                                            email = email,
                                            givenName = givenName,
                                            familyName = familyName,
                                            name = name
                                        )
                                        Log.d("AuthViewModel", "💾 Después de asignar - _pendingGoogleUserData: ${_pendingGoogleUserData?.email}")

                                        _authState.value = AuthState(isSuccess = true)
                                        _sessionKey.value = UUID.randomUUID().toString()
                                        refreshAuthState()
                                    },
                                    onFailure = { e ->
                                        Log.e("AuthViewModel", "❌ Error obteniendo atributos: ${e.message}", e)
                                        // Continuar de todos modos pero sin datos prellenados
                                        _authState.value = AuthState(isSuccess = true)
                                        _sessionKey.value = UUID.randomUUID().toString()
                                        refreshAuthState()
                                    }
                                )
                            }
                        } else {
                            Log.w("AuthViewModel", "⚠️ Sign-In no completado")
                            _authState.value = AuthState(
                                error = "No se pudo completar el inicio de sesión con Google"
                            )
                        }
                    },
                    onFailure = { e ->
                        Log.e("AuthViewModel", "❌ Google Sign-In falló: ${e.message}", e)
                        Log.e("AuthViewModel", "Error completo: $e")

                        val errorMsg = when {
                            e.message?.contains("cancelled", true) == true ->
                                "Inicio de sesión cancelado"
                            e.message?.contains("network", true) == true ->
                                "Error de conexión. Verifica tu internet"
                            e.message?.contains("oauth", true) == true ||
                            e.message?.contains("webdomain", true) == true ||
                            e.message?.contains("hosted ui", true) == true ->
                                "Google Sign-In no está configurado. Lee GOOGLE_SIGNIN_SETUP.md"
                            e.message?.contains("invalidparameter", true) == true ->
                                "Configuración de OAuth incompleta. Verifica amplifyconfiguration.json"
                            else -> "Error: ${e.message ?: "Error desconocido"}. Revisa GOOGLE_SIGNIN_SETUP.md"
                        }
                        _authState.value = AuthState(error = errorMsg)
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Error inesperado en Google Sign-In: ${e.message}", e)
                Log.e("AuthViewModel", "Stack trace: ", e)
                _authState.value = AuthState(
                    error = "Error: ${e.message ?: "Error desconocido"}. Verifica la configuración."
                )
            }
        }
    }

    /**
     * Intenta hacer login automático si el usuario tiene habilitado "Recuérdame".
     * @return true si se intentó el auto-login, false si no hay credenciales guardadas
     */
    fun tryAutoLogin(): Boolean {
        val credentials = preferencesManager?.getCredentials()
        return if (credentials != null) {
            val (email, password) = credentials
            Log.d("AuthViewModel", "Intentando auto-login con credenciales guardadas")
            signIn(email, password, rememberMe = true)
            true
        } else {
            Log.d("AuthViewModel", "No hay credenciales guardadas para auto-login")
            false
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
                    Log.d("AuthViewModel", "🚪 signOut() - Éxito, limpiando todos los datos")
                    _currentUser.value = null
                    _currentUserId.value = null
                    _pendingUserProfile = null
                    clearPendingCredentials()
                    clearPendingGoogleUserData()
                    preferencesManager?.clearCredentials() // Limpiar credenciales guardadas
                    _sessionKey.value = UUID.randomUUID().toString()
                    _authState.value = AuthState(isLoading = false)
                    refreshAuthState()
                    clearPendingCollabProfile() // <-- MODIFICACIÓN
                },
                onFailure = { e ->
                    val msg = e.message.orEmpty()
                    val alreadySignedOut =
                        msg.contains("SignedOutException", ignoreCase = true) ||
                                msg.contains("currently signed out", ignoreCase = true)

                    if (alreadySignedOut) {
                        Log.d("AuthViewModel", "🚪 signOut() - Usuario ya deslogueado, limpiando datos")
                        _currentUser.value = null
                        _currentUserId.value = null
                        _pendingUserProfile = null
                        clearPendingCredentials()
                        clearPendingGoogleUserData()
                        preferencesManager?.clearCredentials() // Limpiar credenciales guardadas
                        _sessionKey.value = UUID.randomUUID().toString()
                        _authState.value = AuthState(isLoading = false)
                        refreshAuthState()
                        clearPendingCollabProfile() // <-- MODIFICACIÓN
                    } else {
                        Log.e("AuthViewModel", "🚪 signOut() - Error: $msg")
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
                    _authState.value = AuthState(error = translateAuthError(e.message ?: "Error al resetear contraseña"))
                }
            )
        }
    }

    fun confirmResetPassword(email: String, confirmationCode: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            val result = authRepository.confirmResetPassword(email, confirmationCode, newPassword)
            result.fold(
                onSuccess = { _authState.value = AuthState(isSuccess = true) },
                onFailure = { e ->
                    _authState.value = AuthState(error = translateAuthError(e.message ?: "Error al confirmar nueva contraseña"))
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
                    _authState.value = AuthState(error = translateAuthError(e.message ?: "Error al actualizar contraseña"))
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
        Log.d("AuthViewModel", "🧹 clearState() - Limpiando estado completo")
        Log.d("AuthViewModel", "🧹 Antes - _pendingGoogleUserData: ${_pendingGoogleUserData?.email}")
        _authState.value = AuthState()
        _currentUser.value = null
        _currentUserId.value = null
        _pendingUserProfile = null
        clearPendingCollabProfile() // <-- MODIFICACIÓN
        // NOTE: NO limpia _pendingGoogleUserData
        Log.d("AuthViewModel", "🧹 Después - _pendingGoogleUserData: ${_pendingGoogleUserData?.email}")
        Log.d("AuthViewModel", "State cleared")
    }

    fun clearError() {
        Log.d("AuthViewModel", "❌ clearError() - Solo limpiando error, NO limpiando Google data")
        Log.d("AuthViewModel", "❌ _pendingGoogleUserData: ${_pendingGoogleUserData?.email}")
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
     * Elimina la cuenta del usuario de Cognito
     */
    suspend fun deleteUserAccount(): Result<Unit> {
        return authRepository.deleteUser()
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

    /**
     * Traduce los mensajes de error de AWS Cognito al español
     */
    private fun translateAuthError(errorMessage: String): String {
        val lowerMessage = errorMessage.lowercase()

        return when {
            // Credenciales incorrectas
            "not authorized" in lowerMessage ||
            "incorrect username or password" in lowerMessage ||
            "user is not authorized" in lowerMessage ->
                "Correo o contraseña incorrectos"

            // Usuario no encontrado
            "user does not exist" in lowerMessage ||
            "usernotfound" in lowerMessage ->
                "No existe una cuenta con este correo"

            // Contraseña incorrecta
            "incorrect password" in lowerMessage ->
                "Contraseña incorrecta"

            // Usuario no confirmado
            "user is not confirmed" in lowerMessage ||
            "usernotconfirmed" in lowerMessage ->
                "Tu cuenta aún no ha sido confirmada. Revisa tu correo."

            // Límite de intentos excedido
            "attempt limit exceeded" in lowerMessage ||
            "limitexceeded" in lowerMessage ->
                "Demasiados intentos fallidos. Intenta de nuevo más tarde."

            // Código inválido
            "invalid verification code" in lowerMessage ||
            "code mismatch" in lowerMessage ->
                "Código de verificación incorrecto"

            // Código expirado
            "expired code" in lowerMessage ->
                "El código de verificación ha expirado"

            // Usuario ya existe
            "user already exists" in lowerMessage ||
            "usernameexists" in lowerMessage ->
                "Ya existe una cuenta con este correo"

            // Error de red
            "network error" in lowerMessage ||
            "unable to resolve host" in lowerMessage ->
                "Error de conexión. Verifica tu internet e intenta de nuevo."

            // Contraseña no cumple requisitos
            "password does not conform" in lowerMessage ||
            "invalidpassword" in lowerMessage ->
                "La contraseña no cumple con los requisitos de seguridad"

            // Error genérico
            else -> "Error al iniciar sesión. Verifica tus credenciales."
        }
    }

}
