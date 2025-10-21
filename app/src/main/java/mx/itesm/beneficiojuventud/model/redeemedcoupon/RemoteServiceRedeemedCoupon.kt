package mx.itesm.beneficiojuventud.model.redeemedcoupon

import com.google.gson.GsonBuilder
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Path
import kotlin.getValue

object RemoteServiceRedeemedCoupon {
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL) // asegúrate de que tenga '/' al final
            .addConverterFactory(GsonConverterFactory.create(RemoteServiceRedeemedCoupon.gson))
            .build()
    }

    private val redeemedCouponApiService by lazy { retrofit.create(RedeemedCouponApiService::class.java) }

    suspend fun getRedeemedCouponsByUser(userId: String): List<RedeemedCoupon> {
        val response = redeemedCouponApiService.getRedeemedCouponsByUser(userId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: emptyList()
    }

    suspend fun getRedeeemedCouponByCouponId(redeeemdCouponId: Int): RedeemedCoupon {
        val response = redeemedCouponApiService.getRedeemedCouponByCouponId(redeeemdCouponId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al obtener el cupon canjeado $redeeemdCouponId")
    }

    suspend fun createRedeemedCoupon(redeemedCoupon: RedeemedCoupon): RedeemedCoupon {
        android.util.Log.d("RemoteServiceRC", "========== API REQUEST ==========")
        android.util.Log.d("RemoteServiceRC", "Endpoint: POST ${Constants.BASE_URL}redeemedcoupon")
        android.util.Log.d("RemoteServiceRC", "Request Body: $redeemedCoupon")

        val response = redeemedCouponApiService.createRedeemedCoupon(redeemedCoupon)

        android.util.Log.d("RemoteServiceRC", "Response Code: ${response.code()}")
        android.util.Log.d("RemoteServiceRC", "Response Success: ${response.isSuccessful}")

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string().orEmpty()
            android.util.Log.e("RemoteServiceRC", "Error Body: $errorBody")
            android.util.Log.d("RemoteServiceRC", "=================================")
            throw Exception("Error ${response.code()}: $errorBody")
        }

        val body = response.body()
        android.util.Log.d("RemoteServiceRC", "Response Body: $body")
        android.util.Log.d("RemoteServiceRC", "=================================")

        return body ?: throw Exception("Respuesta vacía al canjear la promoción")
    }

    suspend fun updateRedeemedCoupon(redeeemdCouponId: Int, redeemedCoupon: RedeemedCoupon): RedeemedCoupon {
        val response = redeemedCouponApiService.updateRedeemdCoupon(redeeemdCouponId, redeemedCoupon)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al actualizar la promoción canjeada $redeeemdCouponId")
    }

    suspend fun deleteRedeemedCoupon(redeemedCouponId: Int) {
        val response = redeemedCouponApiService.deleteRedeemedCoupon(redeemedCouponId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }


}