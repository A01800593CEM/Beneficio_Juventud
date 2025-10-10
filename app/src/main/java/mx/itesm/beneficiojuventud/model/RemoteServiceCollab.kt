package mx.itesm.beneficiojuventud.model

import mx.itesm.beneficiojuventud.model.collaborators.CollabApiService
import mx.itesm.beneficiojuventud.utils.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServiceCollab {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val collabApiService by lazy { retrofit.create(CollabApiService::class.java) }

    suspend fun getCollaboratorById(id: Int): Collaborator {
        val response = collabApiService.getCollaboratorById(id)
        return response.body() ?: throw Exception("No se pudo obtener el colaborador")
    }
    suspend fun getCollaboratorsByCategory(categoryName: String): List<Collaborator> {
        val response = collabApiService.getCollaboratorsByCategory(categoryName)
        return response.body() ?: throw Exception("No se pudo obtener los colaboradores")
    }
    suspend fun createCollaborator(collaborator: Collaborator): Collaborator {
        val response = collabApiService.createCollaborator(collaborator)
        return response.body() ?: throw Exception("No se pudo crear el colaborador")
    }
    suspend fun updateCollaborator(id: Int, update: Collaborator): Collaborator {
        val response = collabApiService.updateCollaborator(id, update)
        return response.body() ?: throw Exception("No se pudo actualizar el colaborador")
    }
    suspend fun deleteCollaborator(id: Int) {
        val response = collabApiService.deleteCollaborator(id)
        if (!response.isSuccessful) {
            throw Exception("No se pudo eliminar el colaborador")
        }
    }
}