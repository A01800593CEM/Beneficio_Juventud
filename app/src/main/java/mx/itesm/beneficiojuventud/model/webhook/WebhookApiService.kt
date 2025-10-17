package mx.itesm.beneficiojuventud.model.webhook

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class WebhookRequest(
    val text: String
)

data class WebhookCategory(
    val id: Int,
    val name: String
)

data class PromotionData(
    val title: String,
    val description: String,
    val initialDate: String,
    val endDate: String,
    val promotionType: String,
    val totalStock: Int,
    val limitPerUser: Int,
    val dailyLimitPerUser: Int,
    val promotionState: String,
    val categories: List<WebhookCategory>
)

data class WebhookResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: PromotionData? = null
)

interface WebhookApiService {
    @POST("webhook/bdd4b48a-4f48-430f-a443-a14a19009340")
    suspend fun enviarDescripcion(@Body request: WebhookRequest): Response<List<PromotionData>>
}