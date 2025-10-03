package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.AuthRepository
import mx.itesm.beneficiojuventud.model.UserProfile

data class AppState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val hasCheckedAuth: Boolean = false
)

class AppViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    // Almacenamiento temporal para datos del usuario durante el registro
    private var _pendingUserProfile: UserProfile? = null
    val pendingUserProfile: UserProfile? get() = _pendingUserProfile

    init {
        checkAuthenticationStatus()
    }

    /**
     * Verificar si el usuario ya está autenticado al iniciar la app
     */
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _appState.value = AppState(isLoading = true)

            val isSignedIn = authRepository.isUserSignedIn()

            _appState.value = AppState(
                isLoading = false,
                isAuthenticated = isSignedIn,
                hasCheckedAuth = true
            )
        }
    }

    /**
     * Forzar verificación de autenticación (útil después de login/logout)
     */
    fun refreshAuthState() {
        // Solo refrescar si no estamos ya cargando
        if (!_appState.value.isLoading) {
            checkAuthenticationStatus()
        }
    }

    /**
     * Guardar temporalmente los datos del usuario durante el registro
     */
    fun savePendingUserProfile(userProfile: UserProfile) {
        _pendingUserProfile = userProfile
    }

    /**
     * Obtener y limpiar los datos del usuario después de que se complete el registro
     */
    fun consumePendingUserProfile(): UserProfile? {
        val profile = _pendingUserProfile
        _pendingUserProfile = null
        return profile
    }

    /**
     * Limpiar datos temporales (en caso de cancelación)
     */
    fun clearPendingUserProfile() {
        _pendingUserProfile = null
    }
}