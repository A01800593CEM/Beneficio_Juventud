package mx.itesm.beneficiojuventud.model.redeemedcoupon

import mx.itesm.beneficiojuventud.model.promos.Promotions
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface RedeemedCouponApiService {
    @POST("redeemedcoupon")
    suspend fun createRedeemedCoupon(@Body redeemedCoupon: RedeemedCoupon): Response<RedeemedCoupon>
    @GET("redeemedcoupon/allcoupons/user/{userId}")
    suspend fun getRedeemedCouponsByUser(@Path("userId") userId: String): Response<List<RedeemedCoupon>>

    @GET("redeemedcoupon/{id}")
    suspend fun getRedeemedCouponByCouponId(@Path("id") promotionId: Int): Response<RedeemedCoupon>

    @PATCH("redeemedcoupon/{id}")
    suspend fun updateRedeemdCoupon(@Path("id") redeemedCouponId: Int, @Body redeemedCoupon: RedeemedCoupon): Response<RedeemedCoupon>

    @DELETE("redeemedcoupon/{id}")
    suspend fun deleteRedeemedCoupon(@Path("id") redeemedCouponId: Int): Response<RedeemedCoupon>
}