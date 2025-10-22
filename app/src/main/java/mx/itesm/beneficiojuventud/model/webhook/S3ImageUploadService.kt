package mx.itesm.beneficiojuventud.model.webhook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Respuesta del webhook de carga de imagen
 */
data class WebhookImageResponse(
    val headers: Map<String, String>? = null,
    val params: Map<String, String>? = null,
    val query: Map<String, String>? = null,
    val body: Map<String, Any>? = null,
    val webhookUrl: String? = null,
    val executionMode: String? = null,
    val codigoUnico: String? = null
)

/**
 * Respuesta del endpoint de subida de imagen a S3 (para fallback)
 */
data class S3UploadResponse(
    val success: Boolean? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    val fileName: String? = null,
    val originalName: String? = null,
    val size: Long? = null,
    val type: String? = null,
    val error: String? = null,
    val details: String? = null
)

/**
 * Servicio para subir imágenes a S3 a través del webhook
 */
object S3ImageUploadService {

    // URL del webhook para subir imágenes como binario
    private const val WEBHOOK_UPLOAD_URL = "https://primary-production-0858b.up.railway.app/webhook/d4e2e473-8dcf-4cca-aea2-ba25ff544450"

    // URL del endpoint para subir imágenes al S3 (fallback)
    // Usa la constante BASE_URL que se configura en tiempo de compilación
    private fun getUploadUrl(): String {
        return "${mx.itesm.beneficiojuventud.utils.Constants.BASE_URL}upload"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Sube una imagen a S3 desde una URI (directo al webhook en binario)
     *
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen seleccionada
     * @return URL de la imagen subida o error
     */
    suspend fun uploadImageFromUri(
        context: Context,
        imageUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Convertir URI a archivo temporal
            val tempFile = createTempImageFile(context, imageUri)
                ?: return@withContext Result.failure(Exception("Error al procesar la imagen"))

            // Enviar directamente al webhook en binario (sin pasar por el servidor)
            val result = uploadImageToWebhook(tempFile)

            // Limpiar archivo temporal
            tempFile.delete()

            result

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sube una imagen al webhook como binario puro
     * No pasa por el servidor, envía directamente a S3 vía webhook
     *
     * @param imageFile Archivo de imagen a subir
     * @return URL de la imagen subida o error
     */
    private suspend fun uploadImageToWebhook(
        imageFile: File
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!imageFile.exists()) {
                return@withContext Result.failure(Exception("El archivo no existe"))
            }

            // Validar tipo de archivo
            val mimeType = getMimeType(imageFile.name)
            if (!isValidImageType(mimeType)) {
                return@withContext Result.failure(
                    Exception("Tipo de archivo no válido. Solo se permiten JPG, PNG, WebP")
                )
            }

            // Validar tamaño (5MB máximo)
            val maxSize = 5 * 1024 * 1024 // 5MB
            if (imageFile.length() > maxSize) {
                return@withContext Result.failure(
                    Exception("El archivo es demasiado grande. Máximo 5MB")
                )
            }

            // Leer el contenido del archivo como binario
            val imageBytes = imageFile.readBytes()

            android.util.Log.d("S3ImageUploadService", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            android.util.Log.d("S3ImageUploadService", "📤 ENVIANDO IMAGEN AL WEBHOOK:")
            android.util.Log.d("S3ImageUploadService", "  - URL: $WEBHOOK_UPLOAD_URL")
            android.util.Log.d("S3ImageUploadService", "  - Archivo: ${imageFile.name}")
            android.util.Log.d("S3ImageUploadService", "  - MIME Type: $mimeType")
            android.util.Log.d("S3ImageUploadService", "  - Tamaño: ${imageBytes.size} bytes (${imageBytes.size / 1024} KB)")
            android.util.Log.d("S3ImageUploadService", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Crear request con el binario puro
            val requestBody = okhttp3.RequestBody.create(mimeType.toMediaType(), imageBytes)
            val request = Request.Builder()
                .url(WEBHOOK_UPLOAD_URL)
                .post(requestBody)
                .addHeader("Content-Type", mimeType)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Error desconocido"
                android.util.Log.e("S3ImageUploadService", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                android.util.Log.e("S3ImageUploadService", "❌ ERROR DEL WEBHOOK:")
                android.util.Log.e("S3ImageUploadService", "  - HTTP Code: ${response.code}")
                android.util.Log.e("S3ImageUploadService", "  - Message: ${response.message}")
                android.util.Log.e("S3ImageUploadService", "  - Body: $errorBody")
                android.util.Log.e("S3ImageUploadService", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                return@withContext Result.failure(
                    Exception("Error al subir imagen: ${response.code} - $errorBody")
                )
            }

            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(Exception("Respuesta vacía del webhook"))

            android.util.Log.d("S3ImageUploadService", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            android.util.Log.d("S3ImageUploadService", "📥 RESPUESTA DEL WEBHOOK (RAW):")
            android.util.Log.d("S3ImageUploadService", responseBody)
            android.util.Log.d("S3ImageUploadService", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Parsear la respuesta como array
            val webhookResponseArray = try {
                gson.fromJson(responseBody, Array<WebhookImageResponse>::class.java)
            } catch (e: Exception) {
                android.util.Log.e("S3ImageUploadService", "❌ ERROR PARSING JSON ARRAY: ${e.message}")
                android.util.Log.e("S3ImageUploadService", "Response body was: $responseBody")
                return@withContext Result.failure(
                    Exception("Error al parsear respuesta: ${e.message}")
                )
            }

            // Validar que el array no esté vacío
            if (webhookResponseArray.isEmpty()) {
                android.util.Log.e("S3ImageUploadService", "❌ El webhook devolvió un array vacío")
                return@withContext Result.failure(
                    Exception("El webhook devolvió un array vacío")
                )
            }

            // Extraer el primer elemento del array
            val webhookResponse = webhookResponseArray[0]

            android.util.Log.d("S3ImageUploadService", "✅ JSON PARSEADO EXITOSAMENTE:")
            android.util.Log.d("S3ImageUploadService", "Objeto parseado: $webhookResponse")
            android.util.Log.d("S3ImageUploadService", "JSON formateado: ${gson.toJson(webhookResponse)}")
            android.util.Log.d("S3ImageUploadService", "  - codigoUnico: ${webhookResponse.codigoUnico}")
            android.util.Log.d("S3ImageUploadService", "  - executionMode: ${webhookResponse.executionMode}")
            android.util.Log.d("S3ImageUploadService", "  - webhookUrl: ${webhookResponse.webhookUrl}")

            // Extraer el codigoUnico y construir la URL de S3
            val codigoUnico = webhookResponse.codigoUnico
            if (codigoUnico.isNullOrBlank()) {
                android.util.Log.e("S3ImageUploadService", "❌ No se recibió codigoUnico en la respuesta")
                android.util.Log.e("S3ImageUploadService", "Respuesta completa: $webhookResponse")
                return@withContext Result.failure(
                    Exception("No se recibió codigoUnico en la respuesta")
                )
            }

            // Construir la URL de S3 con el codigoUnico
            val imageUrl = "https://joven-atizapan-images.s3.us-east-2.amazonaws.com/$codigoUnico-image.png"

            android.util.Log.d("S3ImageUploadService", "✅ URL construida desde codigoUnico:")
            android.util.Log.d("S3ImageUploadService", "  - codigoUnico: $codigoUnico")
            android.util.Log.d("S3ImageUploadService", "  - imageUrl: $imageUrl")
            android.util.Log.d("S3ImageUploadService", "✅ Imagen subida exitosamente")
            Result.success(imageUrl)

        } catch (e: Exception) {
            android.util.Log.e("S3ImageUploadService", "Error uploading to webhook", e)
            Result.failure(e)
        }
    }

    /**
     * Sube una imagen a S3 desde un archivo
     *
     * @param imageFile Archivo de imagen a subir
     * @return URL de la imagen subida o error
     */
    suspend fun uploadImageFile(
        imageFile: File
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!imageFile.exists()) {
                return@withContext Result.failure(Exception("El archivo no existe"))
            }

            // Validar tipo de archivo
            val mimeType = getMimeType(imageFile.name)
            if (!isValidImageType(mimeType)) {
                return@withContext Result.failure(
                    Exception("Tipo de archivo no válido. Solo se permiten JPG, PNG, WebP")
                )
            }

            // Validar tamaño (5MB máximo)
            val maxSize = 5 * 1024 * 1024 // 5MB
            if (imageFile.length() > maxSize) {
                return@withContext Result.failure(
                    Exception("El archivo es demasiado grande. Máximo 5MB")
                )
            }

            // Crear el cuerpo de la petición multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    imageFile.name,
                    imageFile.asRequestBody(mimeType.toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(getUploadUrl())
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Error desconocido"
                return@withContext Result.failure(
                    Exception("Error al subir imagen: ${response.code} - $errorBody")
                )
            }

            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(Exception("Respuesta vacía del servidor"))

            // Parsear la respuesta
            val uploadResponse = try {
                gson.fromJson(responseBody, S3UploadResponse::class.java)
            } catch (e: Exception) {
                return@withContext Result.failure(
                    Exception("Error al parsear respuesta: ${e.message}")
                )
            }

            if (uploadResponse.success != true) {
                return@withContext Result.failure(
                    Exception(uploadResponse.error ?: "Error desconocido al subir imagen")
                )
            }

            val imageUrl = uploadResponse.imageUrl
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
     * Crea un archivo temporal de imagen desde una URI
     */
    private suspend fun createTempImageFile(
        context: Context,
        imageUri: Uri
    ): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext null

            // Decodificar la imagen para optimizarla
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) return@withContext null

            // Crear archivo temporal
            val tempFile = File.createTempFile(
                "temp_image_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )

            // Comprimir y guardar
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            // Liberar memoria del bitmap
            bitmap.recycle()

            tempFile

        } catch (e: Exception) {
            android.util.Log.e("S3ImageUploadService", "Error creating temp file", e)
            null
        }
    }

    /**
     * Obtiene el tipo MIME de un archivo basado en su extensión
     */
    private fun getMimeType(fileName: String): String {
        return when (fileName.lowercase().substringAfterLast('.', "")) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }

    /**
     * Valida si el tipo MIME es de imagen soportada
     */
    private fun isValidImageType(mimeType: String): Boolean {
        return mimeType in listOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
        )
    }

    /**
     * Valida si el tamaño del archivo está dentro del límite
     */
    fun isValidFileSize(fileSize: Long, maxSizeMB: Int = 5): Boolean {
        val maxBytes = maxSizeMB * 1024 * 1024
        return fileSize <= maxBytes
    }

    /**
     * Valida si una URI es de imagen válida
     */
    fun isValidImageUri(context: Context, uri: Uri): Boolean {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            isValidImageType(mimeType ?: "")
        } catch (e: Exception) {
            false
        }
    }
}