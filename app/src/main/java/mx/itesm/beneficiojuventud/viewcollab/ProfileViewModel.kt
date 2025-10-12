package mx.itesm.beneficiojuventud.viewcollab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Branch(
    val id: Int,
    val name: String,
    val address: String,
    val phone: String,
    val isActive: Boolean
)
data class ProfileUiState(
    val contactName: String = "",
    val businessName: String = "",
    val email: String = "",
    val phone: String = "",
    val category: String = "",
    val description: String = "",
    val branches: List<Branch> = emptyList(),
    val isEditingBranch: Branch? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(
                contactName = "María González Abud",
                businessName = "La Bella Italia",
                email = "mariag@labellaitalia.com",
                phone = "+52 55 5678 1234",
                category = "Restaurante",
                description = "Restaurante italiano tradicional con más de 20 años de experiencia...",
                branches = listOf(
                    Branch(1, "Sucursal Arboledas", "Avenida de los Jinetes 123, Col. Las Arboledas...", "+52 55 5678 1234", true),
                    Branch(2, "Sucursal Esmeralda", "Paseo del Mirador 45, Fraccionamiento Vallescondido...", "+52 55 5678 9650", true)
                )
            )
        }
    }

    fun onEditBranchClicked(branch: Branch) {
        _uiState.update { it.copy(isEditingBranch = branch) }
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(isEditingBranch = null) }
    }

    fun onSaveChangesForBranch(updatedBranch: Branch) {
        val currentBranches = _uiState.value.branches.toMutableList()
        val index = currentBranches.indexOfFirst { it.id == updatedBranch.id }
        if (index != -1) {
            currentBranches[index] = updatedBranch
            _uiState.update { it.copy(branches = currentBranches, isEditingBranch = null) }
        }
    }

    fun onContactNameChange(newName: String) { _uiState.update { it.copy(contactName = newName) } }
    fun onBusinessNameChange(newName: String) { _uiState.update { it.copy(businessName = newName) } }
    fun onEmailChange(newEmail: String) { _uiState.update { it.copy(email = newEmail) } }
    fun onPhoneChange(newPhone: String) { _uiState.update { it.copy(phone = newPhone) } }
    fun onDescriptionChange(newDescription: String) { _uiState.update { it.copy(description = newDescription) } }
}