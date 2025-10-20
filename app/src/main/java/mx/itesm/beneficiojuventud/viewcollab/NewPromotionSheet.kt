package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel

private val TextGrey = Color(0xFF616161)
private val LightGrey = Color(0xFFF5F5F5)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val Purple = Color(0xFF6200EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPromotionSheet(
    onClose: () -> Unit,
    viewModel: PromoViewModel = viewModel()
) {
    val promo by viewModel.promoState.collectAsState()
    val scope = rememberCoroutineScope()

    var title by rememberSaveable { mutableStateOf(promo.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(promo.description.orEmpty()) }
    var startDate by rememberSaveable { mutableStateOf(promo.initialDate.orEmpty()) }
    var endDate by rememberSaveable { mutableStateOf(promo.endDate.orEmpty()) }
    var totalStock by rememberSaveable { mutableStateOf(promo.totalStock?.toString().orEmpty()) }
    var limitPerUser by rememberSaveable { mutableStateOf(promo.limitPerUser?.toString().orEmpty()) }
    var imageUrl by rememberSaveable { mutableStateOf(promo.imageUrl.orEmpty()) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // ---------- HEADER ----------
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
            Text("Nueva Promoción", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))

        // ---------- BODY ----------
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                FormTextField(title, { title = it }, "Título de la Promoción*", "Ej. Martes 2x1")
                Spacer(Modifier.height(16.dp))

                FormTextField(description, { description = it }, "Descripción*", "Describe los detalles de la promoción", singleLine = false)
                Spacer(Modifier.height(16.dp))

                Row {
                    DatePickerField("Fecha Inicio*", startDate, { startDate = it }, showStartPicker, { showStartPicker = true }, { showStartPicker = false }, Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    DatePickerField("Fecha Final*", endDate, { endDate = it }, showEndPicker, { showEndPicker = true }, { showEndPicker = false }, Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))
                Row {
                    FormTextField(totalStock, { totalStock = it.filter(Char::isDigit) }, "Stock Total*", "Ej. 100", Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    FormTextField(limitPerUser, { limitPerUser = it.filter(Char::isDigit) }, "Max x Usuario*", "Ej. 2", Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))
                Text("Imagen de la promoción (URL opcional)", fontWeight = FontWeight.Bold, color = TextGrey)
                Spacer(Modifier.height(8.dp))
                FormTextField(imageUrl, { imageUrl = it }, "URL de imagen", "https://...")
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(LightGrey, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (imageUrl.isBlank()) "Sin Imagen" else "Vista previa no implementada", color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))

                Row {
                    GradientButton("Generar con IA", Brush.horizontalGradient(listOf(Purple, DarkBlue)), Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    GradientButton("Subir Imagen", Brush.horizontalGradient(listOf(DarkBlue, Teal)), Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------- MAIN BUTTON ----------
        MainButton(
            text = "Crear Nueva Promoción",
            onClick = {
                val tStock = totalStock.toIntOrNull()
                val lpu = limitPerUser.toIntOrNull()
//                if (title.isBlank() || description.isBlank() || startDate.isBlank() || endDate.isBlank() || tStock == null || lpu == null)
//                    return@MainButton

                val newPromo = Promotions(
                    title = title,
                    description = description,
                    imageUrl = imageUrl.ifBlank { null },
                    initialDate = startDate,
                    endDate = endDate,
                    totalStock = tStock,
                    limitPerUser = lpu
                )

                scope.launch {
                    runCatching { viewModel.createPromotion(newPromo) }
                        .onSuccess { onClose() }
//                        .onFailure { println("ERROR al crear cupon") }
                }
            }
        )
    }
}

// ------------------- HELPERS -------------------
@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextGrey)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onPicked: (String) -> Unit,
    show: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextGrey)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen() },
            shape = RoundedCornerShape(12.dp)
        )
    }
    if (show) {
        val state = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis ?: return@TextButton
                    val date = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .toString()
                    onPicked(date)
                    onDismiss()
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) {
            DatePicker(state = state)
        }
    }
}

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
