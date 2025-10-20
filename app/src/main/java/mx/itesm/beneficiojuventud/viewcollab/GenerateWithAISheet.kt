package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val TextGrey = Color(0xFF616161)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val Purple = Color(0xFF6200EE)
private val LightGray = Color.LightGray

@Composable
private fun GradientButton(text: String, brush: Brush, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush), contentAlignment = Alignment.Center) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GenerateWithAISheet(
    onClose: () -> Unit,
    onGeneratePromotion: (String) -> Unit
) {
    var promptText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
            Spacer(Modifier.width(8.dp))
            Text("Generar con IA", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFFE0E0E0)
        )
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Describe la promoción que quieres crear y la IA la generará por ti:",
            fontSize = 14.sp,
            color = TextGrey
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            placeholder = { Text("Ej. Una promoción de 3x2 en pizzas grandes. Los días jueves y viernes a partir de las 12:00 pm") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = false,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkBlue,
                unfocusedBorderColor = LightGray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            )
        )
        Spacer(Modifier.height(32.dp))

        GradientButton(
            text = "Generar Nueva Promoción",
            brush = Brush.horizontalGradient(listOf(Purple, DarkBlue)),
            onClick = { onGeneratePromotion(promptText) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}