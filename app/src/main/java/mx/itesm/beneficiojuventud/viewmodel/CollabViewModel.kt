package mx.itesm.beneficiojuventud.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mx.itesm.beneficiojuventud.model.categories.Category
import mx.itesm.beneficiojuventud.model.categories.RemoteServiceCategory
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.collaborators.RemoteServiceCollab

class CollabViewModel : ViewModel() {

    private val model = RemoteServiceCollab

    private val _collabState = MutableStateFlow(Collaborator())
    val collabState: StateFlow<Collaborator> = _collabState

    private val _collabListState = MutableStateFlow<List<Collaborator>>(emptyList())
    val collabListState: StateFlow<List<Collaborator>> = _collabListState

    suspend fun getCollaboratorById(id: String) {
        _collabState.value = model.getCollaboratorById(id)
    }

    suspend fun getCollaboratorsByCategory(categoryName: String) {
        _collabListState.value = model.getCollaboratorsByCategory(categoryName)
    }

    suspend fun createCollaborator(collaborator: Collaborator) {
        _collabState.value = model.createCollaborator(collaborator)
    }

    suspend fun updateCollaborator(id: String, update: Collaborator) {
        _collabState.value = model.updateCollaborator(id, update)
    }

    suspend fun deleteCollaborator(id: String) {
        model.deleteCollaborator(id)
    }

    fun clearCollaborator() {
        _collabState.value = Collaborator()
    }

    suspend fun emailExists(email: String): Boolean {
        return model.emailExists(email)
    }

    suspend fun getCategories(): List<Category> {
        return RemoteServiceCategory.getCategories()
    }
}
