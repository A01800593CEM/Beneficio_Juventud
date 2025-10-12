package mx.itesm.beneficiojuventud.model.users

import com.amplifyframework.analytics.UserProfile
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import kotlin.getValue


object RemoteServiceUser {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val userApiService by lazy { retrofit.create(RemoteServiceUser::class.java) }


    suspend fun getUserById(id: String): UserProfile {
        return userApiService.getUserById(id)
    }

    suspend fun createUser(user: UserProfile): UserProfile {
        return userApiService.createUser(user)
    }
    suspend fun updateUser(id: String, update: UserProfile): UserProfile {
        return userApiService.updateUser(id, update)
    }

    suspend fun deleteUser(id: String) {
        userApiService.deleteUser(id)
    }
}