package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.itesm.beneficiojuventud.model.promos.NearbyPromotion
import mx.itesm.beneficiojuventud.utils.UserLocation

/**
 * Componente de mapa que muestra promociones cercanas con marcadores
 */
@Composable
fun NearbyPromotionsMap(
    userLocation: UserLocation?,
    nearbyPromotions: List<NearbyPromotion>,
    onPromotionMarkerClick: (NearbyPromotion) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val defaultLocation = LatLng(25.6866, -100.3161) // Monterrey por defecto
    val userLatLng = userLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 13f)
    }

    // Actualizar cámara cuando cambie la ubicación del usuario
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(it.latitude, it.longitude),
                13f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true,
                compassEnabled = true
            ),
            properties = MapProperties(
                isMyLocationEnabled = userLocation != null,
                maxZoomPreference = 18f,
                minZoomPreference = 10f
            )
        ) {
            // Marcador de ubicación del usuario (opcional, si no se usa isMyLocationEnabled)
            userLocation?.let {
                Marker(
                    state = MarkerState(position = userLatLng),
                    title = "Tu ubicación",
                    snippet = "Estás aquí"
                )
            }

            // Marcadores de promociones cercanas
            nearbyPromotions.forEach { promotion ->
                promotion.closestBranch?.location?.let { locationStr ->
                    // Parsear ubicación desde formato "(lon,lat)"
                    val coords = parseLocationString(locationStr)
                    coords?.let { (lat, lon) ->
                        val position = LatLng(lat, lon)

                        Marker(
                            state = MarkerState(position = position),
                            title = promotion.title ?: "Promoción",
                            snippet = "${promotion.businessName} - ${promotion.getFormattedDistance()}",
                            onClick = {
                                onPromotionMarkerClick(promotion)
                                true
                            }
                        )

                        // Círculo alrededor del marcador (opcional)
                        Circle(
                            center = position,
                            radius = 50.0, // 50 metros
                            fillColor = androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.1f),
                            strokeColor = androidx.compose.ui.graphics.Color.Blue,
                            strokeWidth = 2f
                        )
                    }
                }
            }

            // Círculo de radio de búsqueda alrededor del usuario
            userLocation?.let {
                Circle(
                    center = userLatLng,
                    radius = 3000.0, // 3 km
                    fillColor = androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.05f),
                    strokeColor = androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.3f),
                    strokeWidth = 2f
                )
            }
        }

        // Contador de promociones encontradas
        if (nearbyPromotions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "${nearbyPromotions.size} promociones cerca de ti",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Parsea string de ubicación PostgreSQL "(lon,lat)" a par de coordenadas
 * @return Pair<latitude, longitude> o null si el formato es inválido
 */
private fun parseLocationString(locationStr: String): Pair<Double, Double>? {
    return try {
        val cleaned = locationStr.trim('(', ')', ' ')
        val parts = cleaned.split(",")
        if (parts.size == 2) {
            val lon = parts[0].toDouble()
            val lat = parts[1].toDouble()
            Pair(lat, lon) // Retornar como (lat, lon) para LatLng
        } else null
    } catch (e: Exception) {
        null
    }
}

/**
 * Versión simplificada del mapa que solo muestra la ubicación del usuario
 */
@Composable
fun SimpleLocationMap(
    userLocation: UserLocation?,
    modifier: Modifier = Modifier
) {
    val defaultLocation = LatLng(25.6866, -100.3161)
    val userLatLng = userLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        ),
        properties = MapProperties(
            isMyLocationEnabled = userLocation != null
        )
    ) {
        userLocation?.let {
            Marker(
                state = MarkerState(position = userLatLng),
                title = "Tu ubicación"
            )
        }
    }
}
