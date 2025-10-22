package mx.itesm.beneficiojuventud.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.utils.PlacesService
import mx.itesm.beneficiojuventud.utils.PlacesPrediction
import mx.itesm.beneficiojuventud.utils.PlacesDetails

/**
 * Estado del autocompletado de direcciones
 */
data class AddressAutocompleteState(
    val predictions: List<PlacesPrediction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedAddress: PlacesDetails? = null,
    val sessionToken: AutocompleteSessionToken? = null
)

/**
 * ViewModel para gestionar el autocompletado de direcciones usando Google Places API
 *
 * Responsabilidades:
 * - Manejar búsquedas de predicciones con debounce
 * - Obtener detalles de lugares seleccionados
 * - Gestionar sesiones para optimizar costos
 * - Exponer estado reactivo a la UI
 *
 * @param application Aplicación Android
 */
class PlacesAutocompleteViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "PlacesAutocompleteVM"
    private val placesService = PlacesService(application.applicationContext)

    // Estado público
    private val _state = MutableStateFlow(AddressAutocompleteState())
    val state: StateFlow<AddressAutocompleteState> = _state.asStateFlow()

    // Job para debounce de búsquedas
    private var searchJob: Job? = null

    init {
        // Generar token de sesión al iniciar
        _state.value = _state.value.copy(
            sessionToken = placesService.generateSessionToken()
        )
        Log.d(tag, "ViewModel inicializado con nuevo session token")
    }

    /**
     * Buscar predicciones de direcciones con debounce
     * @param query Texto de búsqueda
     * @param country Código de país para filtrado
     * @param debounceMillis Milisegundos de espera antes de hacer la búsqueda
     */
    fun searchAddresses(
        query: String,
        country: String = "MX",
        debounceMillis: Long = 300
    ) {
        // Cancelar búsqueda anterior si está en curso
        searchJob?.cancel()

        if (query.length < 2) {
            _state.value = _state.value.copy(
                predictions = emptyList(),
                error = null
            )
            return
        }

        searchJob = viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                // Esperar debounce
                delay(debounceMillis)

                Log.d(tag, "Buscando predicciones para: \"$query\"")

                val predictions = placesService.getAddressPredictions(
                    input = query,
                    country = country,
                    sessionToken = _state.value.sessionToken
                )

                _state.value = _state.value.copy(
                    predictions = predictions,
                    isLoading = false
                )

                Log.d(tag, "Encontradas ${predictions.size} predicciones")
            } catch (e: Exception) {
                Log.e(tag, "Error en búsqueda: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    /**
     * Obtener detalles completos de un lugar seleccionado
     * @param placeId ID del lugar desde predicción
     */
    fun selectAddress(placeId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                Log.d(tag, "Obteniendo detalles para place_id: $placeId")

                val details = placesService.getPlaceDetails(
                    placeId = placeId,
                    sessionToken = _state.value.sessionToken
                )

                _state.value = _state.value.copy(
                    selectedAddress = details,
                    predictions = emptyList(),
                    isLoading = false
                )

                Log.d(tag, "Dirección seleccionada: ${details.address}")

                // IMPORTANTE: Generar nuevo token después de selección
                // Esto finaliza la sesión y genera crédito de sesión en Google
                _state.value = _state.value.copy(
                    sessionToken = placesService.generateSessionToken()
                )
            } catch (e: Exception) {
                Log.e(tag, "Error seleccionando dirección: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    /**
     * Limpiar búsqueda y predicciones
     */
    fun clearSearch() {
        searchJob?.cancel()
        _state.value = _state.value.copy(
            predictions = emptyList(),
            error = null
        )
    }

    /**
     * Resetear estado completamente
     */
    fun reset() {
        searchJob?.cancel()
        _state.value = AddressAutocompleteState(
            sessionToken = placesService.generateSessionToken()
        )
    }
}
