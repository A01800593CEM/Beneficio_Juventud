package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import mx.itesm.beneficiojuventud.model.Branch
import mx.itesm.beneficiojuventud.model.BranchState

private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val RedDelete = Color(0xFFE53935)
private val TextColor = Color(0xFF616161)
private val LabelColor = Color(0xFFAEAEAE)

@Composable
fun EditSucursalDialog(
    branch: Branch,
    onDismiss: () -> Unit,
    onSave: (Branch) -> Unit,
    onDelete: (Branch) -> Unit
) {
    var name by remember { mutableStateOf(branch.name ?: "") }
    var address by remember { mutableStateOf(branch.address ?: "") }
    var phone by remember { mutableStateOf(branch.phone ?: "") }
    var zipCode by remember { mutableStateOf(branch.zipCode ?: "") }
    var location by remember { mutableStateOf(branch.location) }
    var isActive by remember { mutableStateOf(branch.state == BranchState.ACTIVE) }
    var showLocationPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (branch.branchId == null) "Nueva Sucursal" else "Editar Sucursal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextColor,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextColor)
                    }
                }
                Spacer(Modifier.height(16.dp))

                StyledOutlinedTextField(value = name, onValueChange = { name = it }, label = "Nombre de la sucursal")
                Spacer(Modifier.height(16.dp))
                StyledOutlinedTextField(value = address, onValueChange = { address = it }, label = "Dirección")
                Spacer(Modifier.height(16.dp))
                StyledOutlinedTextField(value = phone, onValueChange = { phone = it }, label = "Teléfono")
                Spacer(Modifier.height(16.dp))
                StyledOutlinedTextField(value = zipCode, onValueChange = { zipCode = it }, label = "Código Postal")
                Spacer(Modifier.height(16.dp))

                // Location picker button
                LocationPickerButton(
                    location = location,
                    onClick = { showLocationPicker = true }
                )
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Teal,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Sucursal Activa",
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(24.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SaveChangesDialogButton(
                        onClick = {
                            val updatedBranch = branch.copy(
                                name = name.trim(),
                                address = address.trim(),
                                phone = phone.trim(),
                                zipCode = zipCode.trim(),
                                location = location,
                                state = if (isActive) BranchState.ACTIVE else BranchState.INACTIVE
                            )
                            onSave(updatedBranch)
                        }
                    )
                    if (branch.branchId != null) {
                        Spacer(Modifier.height(8.dp))
                        DeleteDialogButton(onClick = { onDelete(branch) })
                    }
                }
            }
        }

        // Location picker dialog
        if (showLocationPicker) {
            BranchLocationPickerDialog(
                initialLocation = location,
                onDismiss = { showLocationPicker = false },
                onLocationSelected = { newLocation ->
                    location = newLocation
                    showLocationPicker = false
                }
            )
        }
    }
}

@Composable
private fun StyledOutlinedTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                color = LabelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Teal,
            unfocusedBorderColor = Color.LightGray,
            focusedTextColor = TextColor,
            unfocusedTextColor = TextColor
        ),
        textStyle = TextStyle(
            color = TextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun SaveChangesDialogButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(DarkBlue, Teal))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Guardar",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun DeleteDialogButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RedDelete)
    ) {
        Text(
            "Eliminar",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun LocationPickerButton(
    location: String?,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Ubicación en el Mapa",
            color = LabelColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(onClick = onClick)
                .border(
                    width = 1.dp,
                    color = if (location != null) Teal else Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = if (location != null) Teal else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (location != null) {
                        "Ubicación definida"
                    } else {
                        "Toca para seleccionar ubicación"
                    },
                    color = if (location != null) TextColor else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = if (location != null) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (location != null) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Mostrar coordenadas si hay ubicación
        if (location != null) {
            parseLocationStringForDisplay(location)?.let { (lat, lon) ->
                Text(
                    text = "Lat: ${"%.6f".format(lat)}, Lon: ${"%.6f".format(lon)}",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }
    }
}

/**
 * Parsea una cadena de ubicación en formato "(longitude,latitude)"
 * y devuelve un par (latitude, longitude) para mostrar
 */
private fun parseLocationStringForDisplay(locationStr: String): Pair<Double, Double>? {
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
        null
    }
}