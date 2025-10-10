package mx.itesm.beneficiojuventud.model

import com.google.firebase.appdistribution.gradle.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteService {
    private const val BASE_URL = "http://54.87.143.88:3000/"

    // Instancia de Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Aquí defines tu interfaz de API
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}