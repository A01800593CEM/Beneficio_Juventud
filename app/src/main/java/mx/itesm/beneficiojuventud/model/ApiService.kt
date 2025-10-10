package mx.itesm.beneficiojuventud.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {


    @GET("users")
    fun obtenerUsuarios(): Call<List<Usuario>>

    @GET("users/{id}")
    fun obtenerUsuarioPorId(@Path("id") id: Int): Call<Usuario>

    @GET("promotions")
    fun obtenerPromociones(): Call<List<Promocion>>

    @GET("collaborators")
    fun obtenerColaboradores(): Call<List<Colaborador>>

}
