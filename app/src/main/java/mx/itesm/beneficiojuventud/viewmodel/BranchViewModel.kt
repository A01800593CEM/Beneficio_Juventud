package mx.itesm.beneficiojuventud.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.itesm.beneficiojuventud.model.Branch
import mx.itesm.beneficiojuventud.model.BranchState
import mx.itesm.beneficiojuventud.model.branch.CreateBranchRequest
import mx.itesm.beneficiojuventud.model.branch.RemoteServiceBranch
import mx.itesm.beneficiojuventud.model.branch.UpdateBranchRequest
import mx.itesm.beneficiojuventud.utils.BranchPreferences

private const val TAG = "BranchViewModel"

/**
 * ViewModel for managing branch-related operations and state.
 * Handles fetching, creating, updating, and deleting branches for collaborators.
 * Persists selected branch using SharedPreferences.
 */
class BranchViewModel : ViewModel() {

    private val model = RemoteServiceBranch

    private val _branchState = MutableStateFlow(Branch())
    val branchState: StateFlow<Branch> = _branchState

    private val _branchListState = MutableStateFlow<List<Branch>>(emptyList())
    val branchListState: StateFlow<List<Branch>> = _branchListState

    // Selected branch ID (for QR scanning, etc.)
    private val _selectedBranchId = MutableStateFlow<Int?>(null)
    val selectedBranchId: StateFlow<Int?> = _selectedBranchId.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun getAllBranches() {
        _branchListState.value = model.getAllBranches()
    }

    suspend fun getBranchById(id: Int) {
        _branchState.value = model.getBranchById(id)
    }

    suspend fun getBranchesByCollaborator(collaboratorId: String, context: Context? = null) {
        try {
            _isLoading.value = true
            _error.value = null
            Log.d(TAG, "Fetching branches for collaborator: $collaboratorId")
            val branches = model.getBranchesByCollaborator(collaboratorId)
            _branchListState.value = branches
            Log.d(TAG, "Loaded ${branches.size} branches for collaborator $collaboratorId")

            // Try to load saved branch from preferences first
            var savedBranchId: Int? = null
            context?.let {
                savedBranchId = BranchPreferences.getSelectedBranch(it, collaboratorId)
                Log.d(TAG, "Loaded saved branch ID from preferences: $savedBranchId")
            }

            // If we have a saved branch and it exists in the list, select it
            val validSavedBranch = savedBranchId?.let { id ->
                branches.firstOrNull { it.branchId == id && it.state == BranchState.ACTIVE }
            }

            if (validSavedBranch != null) {
                Log.d(TAG, "Selecting saved branch: ${validSavedBranch.branchId}")
                _selectedBranchId.value = validSavedBranch.branchId
            } else if (_selectedBranchId.value == null && branches.isNotEmpty()) {
                // Auto-select first active branch if none selected
                val firstActiveBranch = branches.firstOrNull { it.state == BranchState.ACTIVE }
                firstActiveBranch?.branchId?.let { branchId ->
                    Log.d(TAG, "Auto-selecting first active branch: $branchId")
                    _selectedBranchId.value = branchId
                    // Save the auto-selected branch
                    context?.let { BranchPreferences.saveSelectedBranch(it, collaboratorId, branchId) }
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Error al cargar sucursales del colaborador: ${e.message}"
            _error.value = errorMsg
            Log.e(TAG, errorMsg, e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createBranch(request: CreateBranchRequest): Branch {
        val created = model.createBranch(request)
        _branchState.value = created
        return created
    }

    suspend fun updateBranch(id: Int, update: UpdateBranchRequest): Branch {
        val updated = model.updateBranch(id, update)
        _branchState.value = updated
        return updated
    }

    suspend fun deleteBranch(id: Int) {
        model.deleteBranch(id)
    }

    /**
     * Sets the currently selected branch for QR scanning operations
     * @param branchId The ID of the branch to select
     * @param context Android context (optional, for persistence)
     * @param collaboratorId The collaborator's Cognito ID (optional, for persistence)
     */
    fun setSelectedBranch(branchId: Int, context: Context? = null, collaboratorId: String? = null) {
        _selectedBranchId.value = branchId
        Log.d(TAG, "Selected branch ID: $branchId")

        // Save to preferences if context and collaboratorId are provided
        if (context != null && collaboratorId != null) {
            BranchPreferences.saveSelectedBranch(context, collaboratorId, branchId)
            Log.d(TAG, "Saved branch selection to preferences for collaborator: $collaboratorId")
        }
    }

    /**
     * Load the saved branch from preferences
     * @param context Android context
     * @param collaboratorId The collaborator's Cognito ID
     */
    fun loadSavedBranch(context: Context, collaboratorId: String) {
        val savedBranchId = BranchPreferences.getSelectedBranch(context, collaboratorId)
        savedBranchId?.let {
            _selectedBranchId.value = it
            Log.d(TAG, "Loaded saved branch from preferences: $it")
        }
    }

    /**
     * Gets the currently selected branch object
     * @return The selected Branch or null if not found
     */
    fun getSelectedBranch(): Branch? {
        val selectedId = _selectedBranchId.value ?: return null
        return _branchListState.value.firstOrNull { it.branchId == selectedId }
    }

    /**
     * Clears the error message
     */
    fun clearError() {
        _error.value = null
    }
}
