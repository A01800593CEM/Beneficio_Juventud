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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import mx.itesm.beneficiojuventud.viewcollab.Branch

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
    var name by remember { mutableStateOf(branch.name) }
    var address by remember { mutableStateOf(branch.address) }
    var phone by remember { mutableStateOf(branch.phone) }
    var isActive by remember { mutableStateOf(branch.isActive) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar Sucursal",
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
                            val updatedBranch = branch.copy(name = name, address = address, phone = phone, isActive = isActive)
                            onSave(updatedBranch)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    DeleteDialogButton(onClick = { onDelete(branch) })
                }
            }
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