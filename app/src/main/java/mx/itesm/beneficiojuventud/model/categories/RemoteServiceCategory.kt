package mx.itesm.beneficiojuventud.model.categories

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import mx.itesm.beneficiojuventud.utils.Constants

object RemoteServiceCategory {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val categoryApiService by lazy { retrofit.create(CategoryApiService::class.java) }

    suspend fun getCategories(): List<Category> {
        val resp = categoryApiService.getCategories()
        if (!resp.isSuccessful) throw Exception(
            "Error ${resp.code()}: ${
                resp.errorBody()?.string().orEmpty()
            }"
        )
        return resp.body() ?: emptyList()
    }

    suspend fun getCategoryById(id: Int): Category {
        val resp = categoryApiService.getCategoryById(id)
        if (!resp.isSuccessful) throw Exception(
            "Error ${resp.code()}: ${
                resp.errorBody()?.string().orEmpty()
            }"
        )
        return resp.body() ?: throw Exception("Categoría $id no encontrada")
    }
}