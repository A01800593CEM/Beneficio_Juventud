package mx.itesm.beneficiojuventud.model.users

import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServiceUser {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val userApiService by lazy { retrofit.create(UserApiService::class.java) }


    suspend fun getUserById(id: Int): UserProfile {
        val response = userApiService.getUserById(id)
        return response.body() ?: throw Exception("No se pudo obtener el usuario")
    }

    suspend fun createUser(user: UserProfile): UserProfile {
        val response = userApiService.createUser(user)
        return response.body() ?: throw Exception("No se pudo crear el usuario")
    }
    suspend fun updateUser(id: Int, update: UserProfile): UserProfile {
        val response = userApiService.updateUser(id, update)
        return response.body() ?: throw Exception("No se pudo actualizar el usuario")
    }

    suspend fun deleteUser(id: Int) {
        userApiService.deleteUser(id)
    }
}