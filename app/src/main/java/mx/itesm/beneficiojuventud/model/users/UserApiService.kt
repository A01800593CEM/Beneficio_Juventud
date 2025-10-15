package mx.itesm.beneficiojuventud.model.users

import mx.itesm.beneficiojuventud.model.promos.Promotions
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path


interface UserApiService {
    @GET("users/{cognitoId}")
    suspend fun getUserById(@Path("cognitoId") cognitoId: String): Response<UserProfile>

    @POST("users")
    suspend fun createUser(@Body user: UserProfile): Response<UserProfile>

    @PATCH("users/{cognitoId}")
    suspend fun updateUser(@Path("cognitoId") cognitoId: String, @Body update: UserProfile): Response<UserProfile>

    @DELETE("users/{cognitoId}")
    suspend fun deleteUser(@Path("cognitoId") cognitoId: String): Response<Unit>

    @PATCH("users/promotions/fav/{promotionId}/{cognitoId}")
    suspend fun favoritePromotion(
        @Path("promotionId") promotionId: Int,
        @Path("cognitoId") cognitoId: String): Response<Unit>

    @PATCH("users/promotions/unfav/{promotionId}/{cognitoId}")
    suspend fun unfavoritePromotion(
        @Path("promotionId") promotionId: Int,
        @Path("cognitoId") cognitoId: String): Response<Unit>

    @POST("users/collaborators/fav/{cognitoId}/{collaboratorId}")
    suspend fun favoriteCollaborator(
        @Path("collaboratorId") collaboratorId: Int,
        @Path("cognitoId") cognitoId: String): Response<Unit>

    @DELETE("users/collaborators/fav/{cognitoId}/{collaboratorId}")
    suspend fun unfavoriteCollaborator(
        @Path("collaboratorId") collaboratorId: Int,
        @Path("cognitoId") cognitoId: String): Response<Unit>

    @GET("users/promotions/fav/{cognitoId}")
    suspend fun getFavoritePromotions(@Path("cognitoId") cognitoId: String): Response<List<Promotions>>

    @GET("users/collaborators/fav/{cognitoId}")
    suspend fun getFavoriteCollabs(@Path("cognitoId") cognitoId: String): Response<List<Int>>

}
