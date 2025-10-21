package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.webhook.WebhookRepository
import mx.itesm.beneficiojuventud.model.webhook.PromotionData
import com.google.gson.Gson


private val TextGrey = Color(0xFF616161)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val Purple = Color(0xFF6200EE)
private val LightGray = Color.LightGray

@Composable
private fun GradientButton(
    text: String,
    brush: Brush,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        enabled = enabled
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush), contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GenerateWithAISheet(
    onClose: () -> Unit,
    onGeneratePromotion: (PromotionData) -> Unit
) {
    var promptText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val webhookRepository = remember { WebhookRepository() }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
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
            text = if (isLoading) "Generando..." else "Generar Nueva Promoción",
            brush = Brush.horizontalGradient(listOf(Purple, DarkBlue)),
            enabled = promptText.isNotBlank() && !isLoading,
            isLoading = isLoading,
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = ""

                    val result = webhookRepository.enviarDescripcion(promptText)

                    if (result.isSuccess) {
                        val promotionData = result.getOrNull()
                        promotionData?.let { data ->
                            // Llama al callback con los datos de la promoción generada
                            onGeneratePromotion(data)
                        }
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido al generar la promoción"
                    }

                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Muestra error en caso de fallo del webhook
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Error",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = Color(0xFF2B2B2B),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}