package mx.itesm.beneficiojuventud.model.collaborators

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CollabApiService {

    @GET("collaborators/{id}")
    suspend fun getCollaboratorById(@Path("id") id: String): Response<Collaborator>

    @GET("collaborators/category/{categoryName}")
    suspend fun getCollaboratorsByCategory(@Path("categoryName") categoryName: String): Response<List<Collaborator>>

    @GET("collaborators/active/all")
    suspend fun getAllActiveCollaborators(): Response<List<Collaborator>>

    @GET("collaborators/active/newest")
    suspend fun getAllActiveCollaboratorsByNewest(): Response<List<Collaborator>>

    @GET("collaborators/active/by-latest-promotion")
    suspend fun getAllActiveCollaboratorsByLatestPromotion(): Response<List<Collaborator>>

    @POST("collaborators")
    suspend fun createCollaborator(@Body collaborator: Collaborator): Response<Collaborator>

    @PATCH("collaborators/{id}")
    suspend fun updateCollaborator(@Path("id") id: String, @Body update: Collaborator) : Response<Collaborator>

    @DELETE("collaborators/{id}")
    suspend fun deleteCollaborator(@Path("id") id: String): Response<Unit>

    @GET("collaborators/email-exists/{email}")
    suspend fun emailExists(@Path("email") email: String): Response<Boolean>

    @GET("collaborators/nearby/search")
    suspend fun getNearbyCollaborators(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double? = 3.0
    ): Response<List<NearbyCollaborator>>
}