package mx.itesm.beneficiojuventud.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.IOException

/**
 * Datos de una predicción de dirección desde Mapbox Geocoding API
 */
data class PlacesPrediction(
    val placeId: String,
    val mainText: String,
    val secondaryText: String,
    val fullText: String
)

/**
 * Datos de detalles de un lugar desde Mapbox Geocoding API
 */
data class PlacesDetails(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String
)

/**
 * Servicio para interactuar con Mapbox Geocoding API
 * Proporciona autocompletado de direcciones sin problemas de billing de Google
 *
 * VENTAJAS:
 * - Sin restricciones de billing como Google Places
 * - API REST simple sin dependencias pesadas del SDK
 * - Excelente cobertura geográfica mundial
 * - Rápido y confiable
 * - Información de coordenadas incluida en respuestas
 *
 * @param context Contexto de la aplicación
 */
class PlacesService(private val context: Context) {
    private val tag = "PlacesService"
    private val httpClient = OkHttpClient()
    private var mapboxAccessToken: String = ""

    companion object {
        private const val MAPBOX_BASE_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places"
    }

    init {
        // Obtener Mapbox token de AndroidManifest.xml
        try {
            val ai = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            mapboxAccessToken = ai.metaData?.getString("MAPBOX_ACCESS_TOKEN") ?: ""
            if (mapboxAccessToken.isEmpty()) {
                Log.w(tag, "Mapbox access token not found in manifest")
            } else {
                Log.d(tag, "Mapbox Geocoding Service initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error initializing Mapbox token: ${e.message}", e)
        }
    }

    /**
     * Obtener predicciones de direcciones para un texto de entrada
     * @param input Texto de búsqueda (mínimo 2 caracteres)
     * @param country Código de país para filtrado (ej: "MX")
     * @param sessionToken Token de sesión (ignorado en Mapbox, mantenido para compatibilidad)
     * @return Lista de predicciones de direcciones
     */
    suspend fun getAddressPredictions(
        input: String,
        country: String = "MX",
        sessionToken: Any? = null
    ): List<PlacesPrediction> = suspendCancellableCoroutine { continuation ->
        if (input.length < 2) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }

        if (mapboxAccessToken.isEmpty()) {
            Log.e(tag, "Mapbox access token not configured")
            continuation.resumeWithException(
                Exception("Mapbox access token not configured in AndroidManifest.xml")
            )
            return@suspendCancellableCoroutine
        }

        try {
            // Construir URL con parámetros de Mapbox
            val query = input.replace(" ", "%20")
            val countryParam = country.lowercase()
            val url = "$MAPBOX_BASE_URL/$query.json?country=$countryParam&access_token=$mapboxAccessToken&limit=5"

            val request = Request.Builder()
                .url(url)
                .build()

            // Usar callbacks asincronos para evitar NetworkOnMainThreadException
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e(tag, "Error obteniendo predicciones: ${e.message}", e)
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    try {
                        if (!response.isSuccessful) {
                            throw Exception("HTTP Error ${response.code}: ${response.message}")
                        }

                        val body = response.body?.string() ?: "{}"
                        val jsonResponse = JSONObject(body)

                        if (!jsonResponse.has("features")) {
                            continuation.resume(emptyList())
                            return
                        }

                        val features = jsonResponse.getJSONArray("features")
                        val predictions = mutableListOf<PlacesPrediction>()

                        for (i in 0 until features.length()) {
                            val feature = features.getJSONObject(i)

                            val placeId = feature.optString("id", "")
                            val mainText = feature.optString("text", "")
                            val secondaryText = feature.optString("place_name", "")
                                .replace(mainText, "").trim()
                                .removePrefix(",").trim()
                            val fullText = feature.optString("place_name", mainText)

                            predictions.add(
                                PlacesPrediction(
                                    placeId = placeId,
                                    mainText = mainText,
                                    secondaryText = secondaryText,
                                    fullText = fullText
                                )
                            )
                        }

                        Log.d(tag, "Predicciones encontradas: ${predictions.size}")
                        continuation.resume(predictions)
                    } catch (e: Exception) {
                        Log.e(tag, "Error parseando respuesta: ${e.message}", e)
                        continuation.resumeWithException(e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Excepción en getAddressPredictions: ${e.message}", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Obtener detalles completos de un lugar (incluyendo coordenadas)
     * @param placeId ID del lugar (obtenido de una predicción)
     * @param sessionToken Token de sesión (ignorado en Mapbox, mantenido para compatibilidad)
     * @return Detalles del lugar (dirección, latitud, longitud)
     */
    suspend fun getPlaceDetails(
        placeId: String,
        sessionToken: Any? = null
    ): PlacesDetails = suspendCancellableCoroutine { continuation ->
        if (mapboxAccessToken.isEmpty()) {
            Log.e(tag, "Mapbox access token not configured")
            continuation.resumeWithException(
                Exception("Mapbox access token not configured in AndroidManifest.xml")
            )
            return@suspendCancellableCoroutine
        }

        try {
            // Hacer consulta a Mapbox para obtener detalles del lugar
            val url = "$MAPBOX_BASE_URL/$placeId.json?access_token=$mapboxAccessToken"

            val request = Request.Builder()
                .url(url)
                .build()

            // Usar callbacks asincronos para evitar NetworkOnMainThreadException
            httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e(tag, "Error obteniendo detalles: ${e.message}", e)
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    try {
                        if (!response.isSuccessful) {
                            throw Exception("HTTP Error ${response.code}: ${response.message}")
                        }

                        val body = response.body?.string() ?: "{}"
                        val jsonResponse = JSONObject(body)

                        if (!jsonResponse.has("features") || jsonResponse.getJSONArray("features").length() == 0) {
                            throw Exception("Place not found: $placeId")
                        }

                        val feature = jsonResponse.getJSONArray("features").getJSONObject(0)
                        val geometry = feature.getJSONObject("geometry")
                        val coordinates = geometry.getJSONArray("coordinates")

                        val details = PlacesDetails(
                            address = feature.optString("place_name", ""),
                            latitude = coordinates.getDouble(1),
                            longitude = coordinates.getDouble(0),
                            placeId = feature.optString("id", placeId)
                        )

                        Log.d(tag, "Detalles obtenidos: ${details.address} (${details.latitude}, ${details.longitude})")
                        continuation.resume(details)
                    } catch (e: Exception) {
                        Log.e(tag, "Error parseando respuesta: ${e.message}", e)
                        continuation.resumeWithException(e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(tag, "Excepción en getPlaceDetails: ${e.message}", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Generar un nuevo token de sesión para agrupar búsquedas y selecciones
     * Con Mapbox no es necesario, pero se mantiene para compatibilidad con el ViewModel
     * @return Token ficticio (Mapbox no requiere session tokens)
     */
    fun generateSessionToken(): String {
        return "mapbox_session_${System.currentTimeMillis()}"
    }
}
