package mx.itesm.beneficiojuventud.model.categories

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CategoryApiService {

    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Int): Response<Category>
}