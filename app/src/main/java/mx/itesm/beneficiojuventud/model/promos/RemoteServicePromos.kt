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
        // Convert Promotions to CreatePromotionRequest (with category names as strings)
        val request = promo.toCreateRequest()

        // Log detallado para debug
        android.util.Log.d("RemoteServicePromos", "=== Creating Promotion ===")
        android.util.Log.d("RemoteServicePromos", "collaboratorId: ${request.collaboratorId}")
        android.util.Log.d("RemoteServicePromos", "title: ${request.title}")
        android.util.Log.d("RemoteServicePromos", "description: ${request.description}")
        android.util.Log.d("RemoteServicePromos", "imageUrl: ${request.imageUrl}")
        android.util.Log.d("RemoteServicePromos", "initialDate: ${request.initialDate}")
        android.util.Log.d("RemoteServicePromos", "endDate: ${request.endDate}")
        android.util.Log.d("RemoteServicePromos", "promotionType: ${request.promotionType}")
        android.util.Log.d("RemoteServicePromos", "promotionString: ${request.promotionString}")
        android.util.Log.d("RemoteServicePromos", "totalStock: ${request.totalStock}")
        android.util.Log.d("RemoteServicePromos", "availableStock: ${request.availableStock}")
        android.util.Log.d("RemoteServicePromos", "limitPerUser: ${request.limitPerUser}")
        android.util.Log.d("RemoteServicePromos", "dailyLimitPerUser: ${request.dailyLimitPerUser}")
        android.util.Log.d("RemoteServicePromos", "promotionState: ${request.promotionState}")
        android.util.Log.d("RemoteServicePromos", "theme: ${request.theme}")
        android.util.Log.d("RemoteServicePromos", "isBookable: ${request.isBookable}")
        android.util.Log.d("RemoteServicePromos", "categories (names): ${request.categories}")

        // Serializar a JSON para ver qué se envía
        val jsonBody = gson.toJson(request)
        android.util.Log.d("RemoteServicePromos", "JSON Body: $jsonBody")

        val response = promoApiService.createPromotion(request)

        android.util.Log.d("RemoteServicePromos", "Response code: ${response.code()}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string().orEmpty()
            android.util.Log.e("RemoteServicePromos", "Error body: $errorBody")
            throw Exception("Error ${response.code()}: $errorBody")
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
