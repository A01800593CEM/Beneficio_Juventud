package mx.itesm.beneficiojuventud.model.promos

import com.google.gson.GsonBuilder
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServicePromos {

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL) // asegúrate de que tenga '/' al final
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val promoApiService by lazy { retrofit.create(PromoApiService::class.java) }


    suspend fun getAllPromotions(): List<Promotions> {
        val response = promoApiService.getAllPromotions()
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: emptyList()
    }


    suspend fun getPromotionById(id: Int): Promotions {
        val response = promoApiService.getPromotionById(id)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al obtener la promoción $id")
    }

    suspend fun getPromotionByCategory(category: String): List<Promotions> {
        val response = promoApiService.getPromotionByCategory(category)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: emptyList()
    }

    suspend fun createPromotion(promo: Promotions): Promotions {
        val response = promoApiService.createPromotion(promo)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al crear la promoción")
    }

    suspend fun updatePromotion(id: Int, update: Promotions): Promotions {
        val response = promoApiService.updatePromotion(id, update)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al actualizar la promoción $id")
    }

    suspend fun deletePromotion(id: Int) {
        val response = promoApiService.deletePromotion(id)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }
}
