package mx.itesm.beneficiojuventud.model.promos

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PromoApiService {

    @GET("promotions")
    suspend fun getAllPromotions(): Response<List<Promotions>>
    @GET("promotions/{id}")
    suspend fun getPromotionById(@Path("id") id: Int): Response<Promotions>

    @GET("promotions/category/{category}")
    suspend fun getPromotionByCategory(@Path("category") category: String): Response<List<Promotions>>

    @POST("promotions")
    suspend fun createPromotion(@Body promotion: Promotions): Response<Promotions>

    @PATCH("promotions/{id}")
    suspend fun updatePromotion(@Path("id") id: Int, @Body update: Promotions) : Response<Promotions>

    @DELETE("promotions/{id}")
    suspend fun deletePromotion(@Path("id") id: Int): Response<Unit>

}