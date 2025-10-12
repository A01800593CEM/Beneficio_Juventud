package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.model.users.UserProfile

// ---------------------------
// Estados de UI (mismo patrón)
// ---------------------------
sealed class UserUiState {
    object Idle : UserUiState()
    object Loading : UserUiState()
    data class Success(val user: UserProfile?) : UserUiState()
    data class Error(val message: String) : UserUiState()
}

// ---------------------------
// ViewModel principal
// ---------------------------
class UserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState

    // 🔹 Obtener usuario por ID
    fun getUserById(id: Int) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                val user = RemoteServiceUser.getUserById(id)
                _uiState.value = UserUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Error al obtener usuario")
            }
        }
    }

    // 🔹 Crear usuario
    fun createUser(user: UserProfile) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                val createdUser = RemoteServiceUser.createUser(user)
                _uiState.value = UserUiState.Success(createdUser)
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Error al crear usuario")
            }
        }
    }

    // 🔹 Actualizar usuario
    fun updateUser(id: Int, updatedUser: UserProfile) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                val newUser = RemoteServiceUser.updateUser(id, updatedUser)
                _uiState.value = UserUiState.Success(newUser)
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Error al actualizar usuario")
            }
        }
    }

    // 🔹 Eliminar usuario
    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                RemoteServiceUser.deleteUser(id)
                _uiState.value = UserUiState.Success(null)
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Error al eliminar usuario")
            }
        }
    }

    // 🔹 Resetear estado
    fun resetState() {
        _uiState.value = UserUiState.Idle
    }
}
