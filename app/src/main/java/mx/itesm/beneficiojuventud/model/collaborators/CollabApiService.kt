package mx.itesm.beneficiojuventud.model.collaborators

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CollabApiService {

    @GET("collaborators/{id}")
    suspend fun getCollaboratorById(@Path("id") id: Int): Response<Collaborator>

    @GET("collaborators/category/{categoryName}")
    suspend fun getCollaboratorsByCategory(@Path("categoryName") categoryName: String): Response<List<Collaborator>>

    @POST("collaborators")
    suspend fun createCollaborator(@Body collaborator: Collaborator): Response<Collaborator>

    @PATCH("collaborators/{id}")
    suspend fun updateCollaborator(@Path("id") id: Int, @Body update: Collaborator) : Response<Collaborator>

    @DELETE("collaborators/{id}")
    suspend fun deleteCollaborator(@Path("id") id: Int): Response<Unit>
}