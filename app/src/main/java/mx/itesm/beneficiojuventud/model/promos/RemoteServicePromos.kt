package mx.itesm.beneficiojuventud.model.promos

import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RemoteServicePromos {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val promoApiService by lazy { retrofit.create(PromoApiService::class.java) }

    suspend fun getPromotionById(id: Int): Promotions {
        val response = promoApiService.getPromotionById(id)
        return response.body() ?: throw Exception("No se pudo obtener la promoción")
    }

    suspend fun getPromotionByCategory(category: String): List<Promotions> {
        val response = promoApiService.getPromotionByCategory(category)
        return response.body() ?: throw Exception("No se pudo obtener la promoción")
    }

    suspend fun createPromotion(promo: Promotions): Promotions {
        val response = promoApiService.createPromotion(promo)
        return response.body() ?: throw Exception("No se pudo crear la promoción")
    }

    suspend fun updatePromotion(id: Int, update: Promotions): Promotions {
        val response = promoApiService.updatePromotion(id, update)
        return response.body() ?: throw Exception("No se pudo actualizar la promoción")
    }

    suspend fun deletePromotion(id: Int) {
        promoApiService.deletePromotion(id)
    }
}