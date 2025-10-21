package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import mx.itesm.beneficiojuventud.model.Branch

private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val TextColor = Color(0xFF616161)

/**
 * Dialog que muestra la ubicación de una sucursal en un mapa de solo lectura.
 *
 * @param branch Sucursal con la ubicación a mostrar
 * @param onDismiss Callback cuando se cierra el dialog
 */
@Composable
fun ViewBranchLocationDialog(
    branch: Branch,
    onDismiss: () -> Unit
) {
    // Parsear ubicación
    val parsedLocation = remember(branch.location) {
        branch.location?.let { parseLocationStringInternal(it) }
    }

    if (parsedLocation == null) {
        // Si no hay ubicación válida, mostrar mensaje de error
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.8f),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sin ubicación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColor
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Esta sucursal no tiene una ubicación definida en el mapa.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Teal
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
        return
    }

    val (lat, lon) = parsedLocation
    val position = LatLng(lat, lon)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.75f),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = branch.name ?: "Sucursal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ubicación en el mapa",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                // Map Content
                ViewBranchLocationContent(
                    branchName = branch.name ?: "Sucursal",
                    branchAddress = branch.address,
                    position = position,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ViewBranchLocationContent(
    branchName: String,
    branchAddress: String?,
    position: LatLng,
    onDismiss: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 16f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Branch info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = branchName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColor
                )
                branchAddress?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(8.dp))
                Divider(color = Color.LightGray)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Latitud: ${"%.6f".format(position.latitude)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = "Longitud: ${"%.6f".format(position.longitude)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
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
                    maxZoomPreference = 20f,
                    minZoomPreference = 10f
                )
            ) {
                // Marcador de la sucursal
                Marker(
                    state = MarkerState(position = position),
                    title = branchName,
                    snippet = branchAddress ?: "Ubicación de la sucursal"
                )

                // Círculo alrededor del marcador
                Circle(
                    center = position,
                    radius = 100.0, // 100 metros
                    fillColor = Teal.copy(alpha = 0.1f),
                    strokeColor = Teal,
                    strokeWidth = 2f
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Close button
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
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
                    "Cerrar",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Parsea una cadena de ubicación en formato "(longitude,latitude)"
 * y devuelve un par (latitude, longitude)
 */
private fun parseLocationStringInternal(locationStr: String): Pair<Double, Double>? {
    return try {
        val cleaned = locationStr.trim().removePrefix("(").removeSuffix(")")
        val parts = cleaned.split(",").map { it.trim().toDouble() }
        if (parts.size == 2) {
            val lon = parts[0]
            val lat = parts[1]
            if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                Pair(lat, lon)
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
