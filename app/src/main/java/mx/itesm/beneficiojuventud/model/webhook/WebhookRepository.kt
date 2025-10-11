package mx.itesm.beneficiojuventud.model.webhook

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WebhookRepository {
    private val api: WebhookApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://primary-production-0858b.up.railway.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(WebhookApiService::class.java)
    }

    suspend fun enviarDescripcion(descripcion: String): Result<PromotionData> {
        return try {
            val request = WebhookRequest(text = descripcion)
            Log.d("WebhookRepo", "Enviando request: $request")

            val response = api.enviarDescripcion(request)
            Log.d("WebhookRepo", "Response code: ${response.code()}")
            Log.d("WebhookRepo", "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("WebhookRepo", "Response body: $responseBody")
                Log.d("WebhookRepo", "Response body size: ${responseBody?.size}")

                if (responseBody != null && responseBody.isNotEmpty()) {
                    val promotionData = responseBody[0]
                    Log.d("WebhookRepo", "Primera promoción: $promotionData")
                    Log.d("WebhookRepo", "Title: ${promotionData.title}")
                    Log.d("WebhookRepo", "Description: ${promotionData.description}")
                    Log.d("WebhookRepo", "Categories: ${promotionData.categories}")

                    // Tomar el primer elemento del array
                    Result.success(promotionData)
                } else {
                    Log.e("WebhookRepo", "Respuesta vacía o nula del servidor")
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorMsg = "Error HTTP: ${response.code()} - ${response.message()}"
                Log.e("WebhookRepo", errorMsg)
                Log.e("WebhookRepo", "Error body: ${response.errorBody()?.string()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("WebhookRepo", "Exception en enviarDescripcion", e)
            Result.failure(e)
        }
    }
}