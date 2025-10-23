package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.itesm.beneficiojuventud.model.collaborators.NearbyCollaborator
import mx.itesm.beneficiojuventud.utils.UserLocation

/**
 * Componente de mapa que muestra colaboradores cercanos con marcadores
 */
@Composable
fun NearbyCollaboratorsMap(
    userLocation: UserLocation?,
    nearbyCollaborators: List<NearbyCollaborator>,
    onCollaboratorMarkerClick: (NearbyCollaborator) -> Unit = {},
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
            // Marcadores de colaboradores cercanos
            nearbyCollaborators.forEach { collaborator ->
                collaborator.closestBranch?.location?.let { locationStr ->
                    // Parsear ubicación desde formato "(lon,lat)"
                    val coords = parseLocationString(locationStr)
                    coords?.let { (lat, lon) ->
                        val position = LatLng(lat, lon)

                        Marker(
                            state = MarkerState(position = position),
                            title = collaborator.businessName ?: "Colaborador",
                            snippet = "${collaborator.getFormattedDistance()} - ${collaborator.closestBranch?.name}",
                            onClick = {
                                onCollaboratorMarkerClick(collaborator)
                                true
                            },
                            icon = BitmapDescriptorFactory.defaultMarker(230f) // Color 4B4C7E (azul oscuro)
                        )

                        // Círculo alrededor del marcador
                        Circle(
                            center = position,
                            radius = 50.0, // 50 metros
                            fillColor = Color(0xFF4B4C7E).copy(alpha = 0.1f),
                            strokeColor = Color(0xFF4B4C7E),
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
                    fillColor = Color.Green.copy(alpha = 0.05f),
                    strokeColor = Color.Green.copy(alpha = 0.3f),
                    strokeWidth = 2f
                )
            }
        }

        // Contador de colaboradores encontrados
        if (nearbyCollaborators.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${nearbyCollaborators.size} negocios cerca de ti",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val totalBranches = nearbyCollaborators.sumOf { it.totalBranches ?: 0 }
                    if (totalBranches > nearbyCollaborators.size) {
                        Text(
                            text = "$totalBranches sucursales totales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
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
 * Mapa combinado que muestra tanto promociones como colaboradores
 */
@Composable
fun CombinedNearbyMap(
    userLocation: UserLocation?,
    nearbyPromotions: List<mx.itesm.beneficiojuventud.model.promos.NearbyPromotion>,
    nearbyCollaborators: List<NearbyCollaborator>,
    onPromotionMarkerClick: (mx.itesm.beneficiojuventud.model.promos.NearbyPromotion) -> Unit = {},
    onCollaboratorMarkerClick: (NearbyCollaborator) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val defaultLocation = LatLng(25.6866, -100.3161)
    val userLatLng = userLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 13f)
    }

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
                isMyLocationEnabled = userLocation != null
            )
        ) {
            // Marcadores de promociones (color 008D96 - cyan/teal)
            nearbyPromotions.forEach { promotion ->
                promotion.closestBranch?.location?.let { locationStr ->
                    parseLocationString(locationStr)?.let { (lat, lon) ->
                        Marker(
                            state = MarkerState(position = LatLng(lat, lon)),
                            title = promotion.title ?: "Promoción",
                            snippet = promotion.businessName,
                            onClick = { onPromotionMarkerClick(promotion); true },
                            icon = BitmapDescriptorFactory.defaultMarker(180f) // Color 008D96
                        )
                    }
                }
            }

            // Marcadores de colaboradores (color 4B4C7E - azul oscuro)
            nearbyCollaborators.forEach { collaborator ->
                collaborator.closestBranch?.location?.let { locationStr ->
                    parseLocationString(locationStr)?.let { (lat, lon) ->
                        Marker(
                            state = MarkerState(position = LatLng(lat, lon)),
                            title = collaborator.businessName ?: "Colaborador",
                            snippet = collaborator.closestBranch?.name,
                            onClick = { onCollaboratorMarkerClick(collaborator); true },
                            icon = BitmapDescriptorFactory.defaultMarker(230f) // Color 4B4C7E
                        )
                    }
                }
            }

            // Círculo de búsqueda
            userLocation?.let {
                Circle(
                    center = userLatLng,
                    radius = 3000.0,
                    fillColor = Color.Green.copy(alpha = 0.05f),
                    strokeColor = Color.Green.copy(alpha = 0.3f),
                    strokeWidth = 2f
                )
            }
        }

        // Contador combinado
        if (nearbyPromotions.isNotEmpty() || nearbyCollaborators.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cerca de ti:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Indicador de promociones
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF008D96), CircleShape)
                            )
                            Text(
                                text = "${nearbyPromotions.size} promociones",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text("•", style = MaterialTheme.typography.bodyMedium)

                        // Indicador de negocios
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4B4C7E), CircleShape)
                            )
                            Text(
                                text = "${nearbyCollaborators.size} negocios",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
