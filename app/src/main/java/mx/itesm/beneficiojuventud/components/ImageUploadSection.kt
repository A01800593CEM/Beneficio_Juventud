package mx.itesm.beneficiojuventud.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

private val TextGrey = Color(0xFF616161)
private val LightGrey = Color(0xFFF5F5F5)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val Purple = Color(0xFF6200EE)

/**
 * Sección de manejo de imágenes para promociones
 * Incluye tanto generación con IA como subida manual desde el dispositivo
 */
@Composable
fun ImageUploadSection(
    imageUrl: String,
    title: String,
    description: String,
    onImageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isGeneratingImage by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Launcher para seleccionar imagen de la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploadingImage = true
                errorMessage = null

                try {
                    // Validar que sea una imagen válida
                    if (!mx.itesm.beneficiojuventud.model.webhook.S3ImageUploadService.isValidImageUri(context, selectedUri)) {
                        throw Exception("Por favor selecciona un archivo de imagen válido")
                    }

                    // Subir imagen a S3
                    val result = mx.itesm.beneficiojuventud.model.webhook.S3ImageUploadService.uploadImageFromUri(
                        context = context,
                        imageUri = selectedUri
                    )

                    result.onSuccess { uploadedImageUrl ->
                        onImageChange(uploadedImageUrl)
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Error al subir imagen"
                    }

                } catch (e: Exception) {
                    errorMessage = e.message ?: "Error al procesar imagen"
                } finally {
                    isUploadingImage = false
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Imagen de la Promoción",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
            }

            Spacer(Modifier.height(16.dp))

            // Área de imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightGrey)
                    .border(
                        width = 1.dp,
                        color = if (imageUrl.isNotBlank()) Teal else Color.LightGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = !isUploadingImage && !isGeneratingImage) {
                        imagePickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    isUploadingImage -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Teal,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Subiendo imagen...",
                                color = Teal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    imageUrl.isNotBlank() -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUrl),
                                contentDescription = "Imagen de promoción",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Overlay para cambiar imagen
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Toca para cambiar imagen",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Agregar Imagen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextGrey
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Toca para seleccionar desde tu dispositivo",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "JPG, PNG, WebP hasta 5MB",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Botón de generar con IA
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank()) {
                        errorMessage = "Necesitas título y descripción para generar imagen con IA"
                        return@Button
                    }

                    scope.launch {
                        isGeneratingImage = true
                        errorMessage = null

                        val result = mx.itesm.beneficiojuventud.model.webhook.ImageGenerationService.generatePromotionImage(
                            title = title,
                            description = description
                        )

                        isGeneratingImage = false

                        result.onSuccess { generatedImageUrl ->
                            onImageChange(generatedImageUrl)
                        }.onFailure { error ->
                            errorMessage = error.message ?: "Error al generar imagen con IA"
                        }
                    }
                },
                enabled = !isGeneratingImage && !isUploadingImage && title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isGeneratingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isGeneratingImage) "Generando imagen..." else "Generar Imagen con IA",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Nota sobre IA
            if (title.isBlank() || description.isBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Completa el título y descripción para generar con IA",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Mensaje de error
            errorMessage?.let { error ->
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            // URL manual (opcional)
            if (imageUrl.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "URL de imagen actual:",
                    fontSize = 12.sp,
                    color = TextGrey,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = imageUrl,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightGrey, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }
        }
    }
}