package mx.itesm.beneficiojuventud.utils

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Datos de una predicción de dirección desde Google Places
 */
data class PlacesPrediction(
    val placeId: String,
    val mainText: String,
    val secondaryText: String,
    val fullText: String
)

/**
 * Datos de detalles de un lugar desde Google Places
 */
data class PlacesDetails(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String
)

/**
 * Servicio para interactuar con Google Places Autocomplete API
 * Proporciona autocompletado de direcciones usando el SDK nativo de Android
 *
 * VENTAJAS:
 * - No requiere billing en backend
 * - Costo compartido con Maps (primeros 28,500 sesiones gratis/mes)
 * - Funciona offline parcialmente
 * - SDK optimizado para Android
 *
 * @param context Contexto de la aplicación
 */
class PlacesService(private val context: Context) {
    private val tag = "PlacesService"

    init {
        // Inicializar Places SDK si no está inicializado
        if (!Places.isInitialized()) {
            // La API key ya está configurada en AndroidManifest.xml
            Places.initialize(context)
        }
    }

    private val placesClient = Places.createClient(context)

    /**
     * Obtener predicciones de direcciones para un texto de entrada
     * @param input Texto de búsqueda (mínimo 2 caracteres)
     * @param country Código de país para filtrado (ej: "MX")
     * @param sessionToken Token de sesión para agrupar búsquedas (reduce costo)
     * @return Lista de predicciones de direcciones
     */
    suspend fun getAddressPredictions(
        input: String,
        country: String = "MX",
        sessionToken: AutocompleteSessionToken? = null
    ): List<PlacesPrediction> = suspendCancellableCoroutine { continuation ->
        if (input.length < 2) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }

        try {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(input)
                .setCountries(country)
                .setSessionToken(sessionToken)
                // Restringe a tipos específicos (direcciones)
                .setTypeFilter(com.google.android.libraries.places.api.model.TypeFilter.ADDRESS)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    val predictions = response.autocompletePredictions.map { prediction ->
                        PlacesPrediction(
                            placeId = prediction.placeId,
                            mainText = prediction.getPrimaryText(null).toString(),
                            secondaryText = prediction.getSecondaryText(null).toString(),
                            fullText = prediction.getFullText(null).toString()
                        )
                    }
                    Log.d(tag, "Predicciones encontradas: ${predictions.size}")
                    continuation.resume(predictions)
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error obteniendo predicciones: ${exception.message}", exception)
                    continuation.resumeWithException(exception)
                }
        } catch (e: Exception) {
            Log.e(tag, "Excepción en getAddressPredictions: ${e.message}", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Obtener detalles completos de un lugar (incluyendo coordenadas)
     * @param placeId ID del lugar (obtenido de una predicción)
     * @param sessionToken Token de sesión para facturación combinada
     * @return Detalles del lugar (dirección, latitud, longitud)
     */
    suspend fun getPlaceDetails(
        placeId: String,
        sessionToken: AutocompleteSessionToken? = null
    ): PlacesDetails = suspendCancellableCoroutine { continuation ->
        try {
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME
            )

            val request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(sessionToken)
                .build()

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val place = response.place
                    val details = PlacesDetails(
                        address = place.address ?: "",
                        latitude = place.latLng?.latitude ?: 0.0,
                        longitude = place.latLng?.longitude ?: 0.0,
                        placeId = place.id ?: placeId
                    )
                    Log.d(tag, "Detalles obtenidos: ${details.address}")
                    continuation.resume(details)
                }
                .addOnFailureListener { exception ->
                    Log.e(tag, "Error obteniendo detalles: ${exception.message}", exception)
                    continuation.resumeWithException(exception)
                }
        } catch (e: Exception) {
            Log.e(tag, "Excepción en getPlaceDetails: ${e.message}", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * Generar un nuevo token de sesión para agrupar búsquedas y selecciones
     * Reduce costos agrupando múltiples requests bajo una sesión
     * @return Token de sesión para usar en búsquedas y detalles
     */
    fun generateSessionToken(): AutocompleteSessionToken {
        return AutocompleteSessionToken.newInstance()
    }
}
