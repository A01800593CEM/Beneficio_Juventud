package mx.itesm.beneficiojuventud.viewcollab

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await

private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val TextColor = Color(0xFF616161)

/**
 * Dialog que muestra un mapa para seleccionar la ubicación de una sucursal.
 * Permite hacer clic en el mapa para definir la ubicación.
 *
 * @param initialLocation Ubicación inicial en formato "(longitude,latitude)" o null
 * @param onDismiss Callback cuando se cierra el dialog
 * @param onLocationSelected Callback con la ubicación seleccionada en formato "(longitude,latitude)"
 */
@Composable
fun BranchLocationPickerDialog(
    initialLocation: String?,
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(DarkBlue, Teal)
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seleccionar Ubicación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                // Map Content
                BranchLocationPickerContent(
                    initialLocation = initialLocation,
                    onLocationSelected = onLocationSelected,
                    onCancel = onDismiss
                )
            }
        }
    }
}

@Composable
private fun BranchLocationPickerContent(
    initialLocation: String?,
    onLocationSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    // Parsear ubicación inicial
    val parsedLocation = remember(initialLocation) {
        initialLocation?.let { parseLocationString(it) }
    }

    val defaultLocation = LatLng(25.6866, -100.3161) // Monterrey por defecto
    val initialLatLng = parsedLocation?.let { (lat, lon) -> LatLng(lat, lon) } ?: defaultLocation

    var selectedPosition by remember { mutableStateOf(initialLatLng) }
    var currentUserLocation by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
    }

    // Obtener ubicación actual del usuario
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    currentUserLocation = LatLng(it.latitude, it.longitude)
                    // Si no hay ubicación inicial, centrar en el usuario
                    if (initialLocation == null) {
                        selectedPosition = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            15f
                        )
                    }
                }
            } catch (e: Exception) {
                println("Error obteniendo ubicación: ${e.message}")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Instructions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Toca el mapa para seleccionar la ubicación de la sucursal",
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextColor
            )
        }

        // Map
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = true
                ),
                properties = MapProperties(
                    isMyLocationEnabled = currentUserLocation != null,
                    maxZoomPreference = 20f,
                    minZoomPreference = 10f
                ),
                onMapClick = { latLng ->
                    selectedPosition = latLng
                }
            ) {
                // Marcador de la posición seleccionada
                val markerState = remember { MarkerState(position = selectedPosition) }

                // Actualizar la posición del marcador cuando cambie selectedPosition
                LaunchedEffect(selectedPosition) {
                    markerState.position = selectedPosition
                }

                Marker(
                    state = markerState,
                    title = "Ubicación de la sucursal",
                    snippet = "Lat: ${"%.6f".format(selectedPosition.latitude)}, Lon: ${"%.6f".format(selectedPosition.longitude)}"
                )

                // Círculo alrededor del marcador
                Circle(
                    center = selectedPosition,
                    radius = 100.0, // 100 metros
                    fillColor = Teal.copy(alpha = 0.1f),
                    strokeColor = Teal,
                    strokeWidth = 2f
                )
            }

            // Floating action button to center on current location
            currentUserLocation?.let { userLoc ->
                FloatingActionButton(
                    onClick = {
                        selectedPosition = userLoc
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLoc, 15f)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = Teal
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = Color.White
                    )
                }
            }
        }

        // Coordinates display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Coordenadas seleccionadas:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Latitud: ${"%.6f".format(selectedPosition.latitude)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextColor
                )
                Text(
                    text = "Longitud: ${"%.6f".format(selectedPosition.longitude)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextColor
                )
            }
        }

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel button
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Teal
                )
            ) {
                Text(
                    "Cancelar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Confirm button
            Button(
                onClick = {
                    // Formato: "(longitude,latitude)" para coincidir con el formato de PostgreSQL Point
                    val locationString = "(${selectedPosition.longitude},${selectedPosition.latitude})"
                    onLocationSelected(locationString)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(DarkBlue, Teal))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Confirmar",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

/**
 * Parsea una cadena de ubicación en formato "(longitude,latitude)" o "(lat,lon)"
 * y devuelve un par (latitude, longitude)
 */
private fun parseLocationString(locationStr: String): Pair<Double, Double>? {
    return try {
        // Remover paréntesis y espacios
        val cleaned = locationStr.trim().removePrefix("(").removeSuffix(")")
        val parts = cleaned.split(",").map { it.trim().toDouble() }

        if (parts.size == 2) {
            // Asumir formato (longitude, latitude) - formato PostgreSQL Point
            val lon = parts[0]
            val lat = parts[1]

            // Validar que las coordenadas son razonables
            if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                Pair(lat, lon) // Retornar (latitude, longitude)
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        println("Error parseando ubicación: ${e.message}")
        null
    }
}
