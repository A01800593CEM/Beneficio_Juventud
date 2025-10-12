package mx.itesm.beneficiojuventud.model.users

import com.amplifyframework.analytics.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path


interface UserApiService {

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserProfile

    @POST("users")
    suspend fun createUser(@Body user: UserProfile): UserProfile

    @PATCH("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body update: UserProfile) : UserProfile

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Unit
}