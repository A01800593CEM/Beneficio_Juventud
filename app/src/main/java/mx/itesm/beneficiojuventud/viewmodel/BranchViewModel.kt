package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.itesm.beneficiojuventud.model.Branch
import mx.itesm.beneficiojuventud.model.branch.CreateBranchRequest
import mx.itesm.beneficiojuventud.model.branch.RemoteServiceBranch
import mx.itesm.beneficiojuventud.model.branch.UpdateBranchRequest

class BranchViewModel : ViewModel() {

    private val model = RemoteServiceBranch

    private val _branchState = MutableStateFlow(Branch())
    val branchState: StateFlow<Branch> = _branchState

    private val _branchListState = MutableStateFlow<List<Branch>>(emptyList())
    val branchListState: StateFlow<List<Branch>> = _branchListState

    suspend fun getAllBranches() {
        _branchListState.value = model.getAllBranches()
    }

    suspend fun getBranchById(id: Int) {
        _branchState.value = model.getBranchById(id)
    }

    suspend fun getBranchesByCollaborator(collaboratorId: String) {
        _branchListState.value = model.getBranchesByCollaborator(collaboratorId)
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
}
