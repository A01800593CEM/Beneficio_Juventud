package mx.itesm.beneficiojuventud.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("usuarios")
    fun obtenerUsuarios(): Call<List<Usuario>>

    @GET("promociones")
    fun obtenerPromociones(): Call<List<Promocion>>

    @GET("colaboradores")
    fun obtenerColaboradores(): Call<List<Colaborador>>

    @GET("usuarios/{id}")
    fun obtenerUsuarioPorId(@Path("id") id: Int): Call<Usuario>
}
