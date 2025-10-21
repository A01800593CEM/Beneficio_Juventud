package mx.itesm.beneficiojuventud.model.collaborators

import android.util.Log
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServiceCollab {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val collabApiService by lazy { retrofit.create(CollabApiService::class.java) }

    suspend fun getCollaboratorById(id: String): Collaborator {
        val response = collabApiService.getCollaboratorById(id)
        return response.body() ?: throw Exception("No se pudo obtener el colaborador")
    }
    suspend fun getCollaboratorsByCategory(categoryName: String): List<Collaborator> {
        val response = collabApiService.getCollaboratorsByCategory(categoryName)
        return response.body() ?: throw Exception("No se pudo obtener los colaboradores")
    }
    suspend fun createCollaborator(collaborator: Collaborator): Collaborator {
        Log.d("RemoteServiceCollab", "Creando colaborador:")
        Log.d("RemoteServiceCollab", "  cognitoId: ${collaborator.cognitoId}")
        Log.d("RemoteServiceCollab", "  businessName: ${collaborator.businessName}")
        Log.d("RemoteServiceCollab", "  rfc: ${collaborator.rfc}")
        Log.d("RemoteServiceCollab", "  representativeName: ${collaborator.representativeName}")
        Log.d("RemoteServiceCollab", "  phone: ${collaborator.phone}")
        Log.d("RemoteServiceCollab", "  email: ${collaborator.email}")
        Log.d("RemoteServiceCollab", "  address: ${collaborator.address}")
        Log.d("RemoteServiceCollab", "  postalCode: ${collaborator.postalCode}")
        Log.d("RemoteServiceCollab", "  description: ${collaborator.description}")
        Log.d("RemoteServiceCollab", "  state: ${collaborator.state}")
        Log.d("RemoteServiceCollab", "  categoryIds: ${collaborator.categoryIds}")

        val response = collabApiService.createCollaborator(collaborator)

        Log.d("RemoteServiceCollab", "Respuesta HTTP: ${response.code()}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e("RemoteServiceCollab", "Error body: $errorBody")
            throw Exception(
                "Error HTTP ${response.code()}: ${response.message()}. " +
                "Detalles: ${errorBody ?: "sin detalles"}"
            )
        }

        return response.body() ?: throw Exception(
            "Respuesta exitosa pero body vacío (código ${response.code()})"
        )
    }
    suspend fun updateCollaborator(id: String, update: Collaborator): Collaborator {
        val response = collabApiService.updateCollaborator(id, update)
        return response.body() ?: throw Exception("No se pudo actualizar el colaborador")
    }
    suspend fun deleteCollaborator(id: String) {
        val response = collabApiService.deleteCollaborator(id)
        if (!response.isSuccessful) {
            throw Exception("No se pudo eliminar el colaborador")
        }
    }

    suspend fun emailExists(email: String): Boolean {
        val response = collabApiService.emailExists(email)
        return response.body() ?: false
    }

    suspend fun getNearbyCollaborators(
        latitude: Double,
        longitude: Double,
        radius: Double = 3.0
    ): List<NearbyCollaborator> {
        android.util.Log.d("RemoteServiceCollab", "========== NEARBY COLLABORATORS REQUEST ==========")
        android.util.Log.d("RemoteServiceCollab", "Latitude: $latitude")
        android.util.Log.d("RemoteServiceCollab", "Longitude: $longitude")
        android.util.Log.d("RemoteServiceCollab", "Radius: $radius km")

        val response = collabApiService.getNearbyCollaborators(latitude, longitude, radius)

        android.util.Log.d("RemoteServiceCollab", "Response Code: ${response.code()}")
        android.util.Log.d("RemoteServiceCollab", "Response Success: ${response.isSuccessful}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string().orEmpty()
            android.util.Log.e("RemoteServiceCollab", "Error Body: $errorBody")
            throw Exception("Error ${response.code()}: $errorBody")
        }

        val body = response.body() ?: emptyList()
        android.util.Log.d("RemoteServiceCollab", "Collaborators found: ${body.size}")
        body.forEach { collab ->
            android.util.Log.d("RemoteServiceCollab", "  - ${collab.businessName} at ${collab.distance}km")
        }
        android.util.Log.d("RemoteServiceCollab", "==============================================")

        return body
    }
}