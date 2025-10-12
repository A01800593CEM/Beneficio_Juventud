package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.viewcollab.Branch

private val ValueColor = Color(0xFF616161)
private val BorderColor = Color(0xFFE0E0E0)
private val ActiveGreen = Color(0xFF4CAF50)
private val DetailsColor = Color(0xFF969696)

@Composable
fun SucursalCard(branch: Branch, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                branch.name,
                fontWeight = FontWeight.Black,
                color = ValueColor,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                branch.address,
                color = DetailsColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                branch.phone,
                color = DetailsColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (branch.isActive) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ActiveGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Activa", color = ActiveGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Editar Sucursal", tint = ValueColor)
        }
    }
}