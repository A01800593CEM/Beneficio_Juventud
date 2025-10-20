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
    val prompt: String
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
    val message: String? = null
) {
    // Obtener la URL de imagen independientemente del formato de respuesta
    fun extractImageUrl(): String? {
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
    // TODO: Configurar la URL correcta del webhook de generación de imágenes
    private const val IMAGE_WEBHOOK_URL = "https://primary-production-0858b.up.railway.app/webhook/image-generation"

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
            // Crear un prompt descriptivo para la IA
            val prompt = buildImagePrompt(title, description)

            val requestData = ImageGenerationRequest(prompt = prompt)
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

            // Intentar parsear la respuesta
            val imageResponse = try {
                gson.fromJson(responseBody, ImageGenerationResponse::class.java)
            } catch (e: Exception) {
                // Si no es JSON, asumir que es directamente la URL
                ImageGenerationResponse(url = responseBody)
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
     * Construye un prompt optimizado para generación de imágenes
     */
    private fun buildImagePrompt(title: String, description: String): String {
        // Crear un prompt que genere una imagen atractiva para la promoción
        return """
            Crea una imagen promocional atractiva y profesional para:

            Título: $title
            Descripción: $description

            Estilo: Moderno, colorido, atractivo para restaurantes y negocios.
            Formato: Banner horizontal para redes sociales.
            Elementos: Incluir el tema de la promoción de forma visual y llamativa.
        """.trimIndent()
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
