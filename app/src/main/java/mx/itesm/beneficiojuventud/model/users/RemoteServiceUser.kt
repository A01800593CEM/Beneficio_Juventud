package mx.itesm.beneficiojuventud.model.users

import com.google.gson.GsonBuilder
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

    suspend fun getUserById(id: String): UserProfile {
        val response = userApiService.getUserById(id)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al obtener el usuario $id")
    }

    suspend fun createUser(user: UserProfile): UserProfile {
        val response = userApiService.createUser(user)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al crear el usuario")
    }

    suspend fun updateUser(id: String, update: UserProfile): UserProfile {
        val response = userApiService.updateUser(id, update)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al actualizar el usuario $id")
    }

    suspend fun deleteUser(id: String) {
        val response = userApiService.deleteUser(id)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }
}
