package mx.itesm.beneficiojuventud.model.webhook

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Request para generar imagen con IA
 */
data class ImageGenerationRequest(
    val text: String
)

/**
 * Respuesta específica del webhook de N8N
 */
data class N8NImageResponse(
    @SerializedName("codigoUnico")
    val codigoUnico: String? = null,
    val success: Boolean? = null,
    val message: String? = null
)

/**
 * Respuesta del webhook de generación de imagen
 */
data class ImageGenerationResponse(
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("image_url")
    val image_url: String? = null,
    @SerializedName("url")
    val url: String? = null,
    val success: Boolean? = null,
    val message: String? = null,
    val originalResponse: List<N8NImageResponse>? = null
) {
    // Obtener la URL de imagen independientemente del formato de respuesta
    fun extractImageUrl(): String? {
        // Si tenemos la respuesta original de N8N, construir la URL
        originalResponse?.firstOrNull()?.codigoUnico?.let { codigo ->
            return "https://joven-atizapan-images.s3.us-east-2.amazonaws.com/${codigo}-image.png"
        }

        return imageUrl ?: image_url ?: url
    }
}

/**
 * Servicio para generar imágenes de promociones usando IA
 *
 * Webhooks comunes para generación de imágenes:
 * - DALL-E / OpenAI: Genera imágenes desde texto
 * - Stable Diffusion: Generación de imágenes de código abierto
 * - Midjourney: Generación artística de imágenes
 *
 * El webhook debe recibir un prompt y devolver una URL de imagen
 */
object ImageGenerationService {

    // URL del webhook para generar imágenes con IA
    private const val IMAGE_WEBHOOK_URL = "https://primary-production-0858b.up.railway.app/webhook/feb97458-c7ba-4f4c-8b2b-2664935ac260"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Genera una imagen usando IA basándose en la descripción de la promoción
     *
     * @param title Título de la promoción
     * @param description Descripción de la promoción
     * @return URL de la imagen generada o null si falla
     */
    suspend fun generatePromotionImage(
        title: String,
        description: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Crear el texto combinado para el webhook
            val combinedText = buildImagePrompt(title, description)

            val requestData = ImageGenerationRequest(text = combinedText)
            val jsonBody = gson.toJson(requestData)

            val requestBody = jsonBody.toRequestBody(
                "application/json; charset=utf-8".toMediaType()
            )

            val request = Request.Builder()
                .url(IMAGE_WEBHOOK_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Error desconocido"
                return@withContext Result.failure(
                    Exception("Error al generar imagen: ${response.code} - $errorBody")
                )
            }

            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(Exception("Respuesta vacía del servidor"))

            // Intentar parsear la respuesta como array de N8N
            val n8nResponse = try {
                gson.fromJson(responseBody, Array<N8NImageResponse>::class.java)
            } catch (e: Exception) {
                null
            }

            if (n8nResponse != null && n8nResponse.isNotEmpty()) {
                val codigoUnico = n8nResponse.first().codigoUnico
                if (!codigoUnico.isNullOrBlank()) {
                    val imageUrl = "https://joven-atizapan-images.s3.us-east-2.amazonaws.com/${codigoUnico}-image.png"
                    return@withContext Result.success(imageUrl)
                }
            }

            // Fallback: intentar parsear como respuesta estándar
            val imageResponse = try {
                gson.fromJson(responseBody, ImageGenerationResponse::class.java)
            } catch (e: Exception) {
                ImageGenerationResponse(url = responseBody.trim().removeSurrounding("\""))
            }

            val imageUrl = imageResponse.extractImageUrl()

            if (imageUrl.isNullOrBlank()) {
                return@withContext Result.failure(
                    Exception("No se recibió URL de imagen en la respuesta")
                )
            }

            Result.success(imageUrl)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Construye el texto combinado para el webhook de generación de imágenes
     */
    private fun buildImagePrompt(title: String, description: String): String {
        // Combinar título y descripción en un solo string como requiere el webhook
        return "$title $description"
    }

    /**
     * Genera una imagen simple basada solo en el título
     */
    suspend fun generateSimpleImage(title: String): Result<String> {
        return generatePromotionImage(
            title = title,
            description = "Promoción especial de $title"
        )
    }

    /**
     * Valida si una URL de imagen es válida
     */
    fun isValidImageUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        return try {
            val lowerUrl = url.lowercase()
            (lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://")) &&
            (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") ||
             lowerUrl.endsWith(".png") || lowerUrl.endsWith(".webp") ||
             lowerUrl.contains("/image/") || lowerUrl.contains("imgur") ||
             lowerUrl.contains("cloudinary") || lowerUrl.contains("s3"))
        } catch (e: Exception) {
            false
        }
    }
}
