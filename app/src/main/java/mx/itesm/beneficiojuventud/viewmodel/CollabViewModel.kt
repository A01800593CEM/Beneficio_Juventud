package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.Collaborator
import mx.itesm.beneficiojuventud.model.RemoteServiceCollab

// ---------------------------
// Estados de UI (mismo patrón)
// ---------------------------
sealed class CollaboratorUiState {
    object Idle : CollaboratorUiState()
    object Loading : CollaboratorUiState()
    data class SuccessSingle(val collaborator: Collaborator?) : CollaboratorUiState()
    data class SuccessList(val collaborators: List<Collaborator>) : CollaboratorUiState()
    data class Error(val message: String) : CollaboratorUiState()
}

// ---------------------------
// ViewModel principal
// ---------------------------
class CollaboratorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CollaboratorUiState>(CollaboratorUiState.Idle)
    val uiState: StateFlow<CollaboratorUiState> = _uiState

    // 🔹 Obtener colaborador por ID
    fun getCollaboratorById(id: Int) {
        viewModelScope.launch {
            _uiState.value = CollaboratorUiState.Loading
            try {
                val collaborator = RemoteServiceCollab.getCollaboratorById(id)
                _uiState.value = CollaboratorUiState.SuccessSingle(collaborator)
            } catch (e: Exception) {
                _uiState.value = CollaboratorUiState.Error(e.message ?: "Error al obtener colaborador")
            }
        }
    }

    // 🔹 Obtener colaboradores por categoría
    fun getCollaboratorsByCategory(categoryName: String) {
        viewModelScope.launch {
            _uiState.value = CollaboratorUiState.Loading
            try {
                val collaborators = RemoteServiceCollab.getCollaboratorsByCategory(categoryName)
                _uiState.value = CollaboratorUiState.SuccessList(collaborators)
            } catch (e: Exception) {
                _uiState.value = CollaboratorUiState.Error(e.message ?: "Error al obtener colaboradores")
            }
        }
    }

    // 🔹 Crear colaborador
    fun createCollaborator(collaborator: Collaborator) {
        viewModelScope.launch {
            _uiState.value = CollaboratorUiState.Loading
            try {
                val created = RemoteServiceCollab.createCollaborator(collaborator)
                _uiState.value = CollaboratorUiState.SuccessSingle(created)
            } catch (e: Exception) {
                _uiState.value = CollaboratorUiState.Error(e.message ?: "Error al crear colaborador")
            }
        }
    }

    // 🔹 Actualizar colaborador
    fun updateCollaborator(id: Int, updated: Collaborator) {
        viewModelScope.launch {
            _uiState.value = CollaboratorUiState.Loading
            try {
                val updatedCollab = RemoteServiceCollab.updateCollaborator(id, updated)
                _uiState.value = CollaboratorUiState.SuccessSingle(updatedCollab)
            } catch (e: Exception) {
                _uiState.value = CollaboratorUiState.Error(e.message ?: "Error al actualizar colaborador")
            }
        }
    }

    // 🔹 Eliminar colaborador
    fun deleteCollaborator(id: Int) {
        viewModelScope.launch {
            _uiState.value = CollaboratorUiState.Loading
            try {
                RemoteServiceCollab.deleteCollaborator(id)
                _uiState.value = CollaboratorUiState.SuccessSingle(null)
            } catch (e: Exception) {
                _uiState.value = CollaboratorUiState.Error(e.message ?: "Error al eliminar colaborador")
            }
        }
    }

    // 🔹 Resetear estado
    fun resetState() {
        _uiState.value = CollaboratorUiState.Idle
    }
}
