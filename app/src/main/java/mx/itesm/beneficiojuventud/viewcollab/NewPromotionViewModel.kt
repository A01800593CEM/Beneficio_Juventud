package mx.itesm.beneficiojuventud.viewcollab

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
data class SelectableBranch(
    val branch: Branch,
    val isSelected: Boolean
)

data class NewPromotionUiState(
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val totalStock: String = "",
    val maxPerUser: String = "",
    val maxPerDay: String = "",
    val branches: List<SelectableBranch> = emptyList(),
    val allBranchesSelected: Boolean = false
)

class NewPromotionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NewPromotionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val initialBranches = listOf(
            Branch(1, "Sucursal Arboledas", "Avenida de los Jinetes 123...", "+52 55 5678 1234", true),
            Branch(2, "Sucursal Esmeralda", "Paseo del Mirador 45...", "+52 55 5678 9650", true)
        )
        _uiState.update {
            it.copy(
                branches = initialBranches.map { branch -> SelectableBranch(branch, isSelected = false) }
            )
        }
    }

    fun onTitleChange(newTitle: String) { _uiState.update { it.copy(title = newTitle) } }
    fun onDescriptionChange(newDescription: String) { _uiState.update { it.copy(description = newDescription) } }
}