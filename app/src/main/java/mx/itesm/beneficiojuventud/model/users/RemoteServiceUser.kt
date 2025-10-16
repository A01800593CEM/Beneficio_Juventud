package mx.itesm.beneficiojuventud.model.users

import com.google.gson.GsonBuilder
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServiceUser {

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val userApiService by lazy { retrofit.create(UserApiService::class.java) }

    suspend fun getUserById(cognitoId: String): UserProfile {
        val response = userApiService.getUserById(cognitoId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al obtener el usuario $cognitoId")
    }

    suspend fun createUser(user: UserProfile): UserProfile {
        val response = userApiService.createUser(user)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al crear el usuario")
    }

    suspend fun updateUser(cognitoId: String, update: UserProfile): UserProfile {
        val response = userApiService.updateUser(cognitoId, update)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al actualizar el usuario $cognitoId")
    }

    suspend fun deleteUser(cognitoId: String) {
        val response = userApiService.deleteUser(cognitoId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }

    // --- Promos favoritas (sin cambios en IDs de promo) ---
    suspend fun favoritePromotion(promotionId: Int, cognitoId: String) {
        val response = userApiService.favoritePromotion(promotionId, cognitoId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }

    suspend fun unfavoritePromotion(promotionId: Int, cognitoId: String) {
        val response = userApiService.unfavoritePromotion(promotionId, cognitoId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }

    suspend fun getFavoritePromotions(cognitoId: String): List<Promotions> {
        val response = userApiService.getFavoritePromotions(cognitoId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al obtener las promociones favoritas")
    }

    // --- Colaboradores favoritos ahora como String ---
    suspend fun getFavoriteCollabs(cognitoId: String): List<Collaborator> {
        val response = userApiService.getFavoriteCollabs(cognitoId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al obtener los colaboradores favoritos")
    }

    suspend fun favoriteCollaborator(collaboratorId: String, cognitoId: String) {
        val response = userApiService.favoriteCollaborator(collaboratorId, cognitoId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }

    suspend fun unfavoriteCollaborator(collaboratorId: String, cognitoId: String) {
        val response = userApiService.unfavoriteCollaborator(collaboratorId, cognitoId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }
}
