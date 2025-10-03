package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.AuthRepository

data class AppState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val hasCheckedAuth: Boolean = false
)

class AppViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

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
}