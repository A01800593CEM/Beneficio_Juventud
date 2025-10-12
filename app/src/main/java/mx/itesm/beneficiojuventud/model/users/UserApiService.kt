package mx.itesm.beneficiojuventud.model.users

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
}
