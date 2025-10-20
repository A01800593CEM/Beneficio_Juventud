package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.PromotionType
import mx.itesm.beneficiojuventud.model.promos.PromotionState
import mx.itesm.beneficiojuventud.model.promos.PromoTheme
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.model.webhook.ImageGenerationService
import mx.itesm.beneficiojuventud.model.categories.Category
import mx.itesm.beneficiojuventud.model.categories.RemoteServiceCategory

private val TextGrey = Color(0xFF616161)
private val LightGrey = Color(0xFFF5F5F5)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val Purple = Color(0xFF6200EE)

/**
 * Formulario completo para crear/editar promociones
 * Basado en la funcionalidad de la página web con todos los campos requeridos por la API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPromotionSheet(
    onClose: () -> Unit,
    viewModel: PromoViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    initialPromotionData: mx.itesm.beneficiojuventud.model.webhook.PromotionData? = null
) {
    val promo by viewModel.promoState.collectAsState()
    val scope = rememberCoroutineScope()

    // Obtener el collaboratorId del usuario autenticado
    LaunchedEffect(Unit) { authViewModel.getCurrentUser() }
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val collaboratorId = currentUserId ?: "anonymous"

    // Estados del formulario
    var title by rememberSaveable { mutableStateOf(initialPromotionData?.title ?: promo.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initialPromotionData?.description ?: promo.description.orEmpty()) }
    var startDate by rememberSaveable { mutableStateOf(initialPromotionData?.initialDate ?: promo.initialDate.orEmpty()) }
    var endDate by rememberSaveable { mutableStateOf(initialPromotionData?.endDate ?: promo.endDate.orEmpty()) }
    var totalStock by rememberSaveable { mutableStateOf(initialPromotionData?.totalStock?.toString() ?: promo.totalStock?.toString() ?: "100") }
    var limitPerUser by rememberSaveable { mutableStateOf(initialPromotionData?.limitPerUser?.toString() ?: promo.limitPerUser?.toString() ?: "1") }
    var dailyLimitPerUser by rememberSaveable { mutableStateOf(initialPromotionData?.dailyLimitPerUser?.toString() ?: promo.dailyLimitPerUser?.toString() ?: "1") }
    var promotionString by rememberSaveable { mutableStateOf(promo.promotionString.orEmpty()) }
    var imageUrl by rememberSaveable { mutableStateOf(promo.imageUrl.orEmpty()) }

    // Estados de error y carga
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var isGeneratingImage by remember { mutableStateOf(false) }

    // Tipo de promoción
    var promotionType by rememberSaveable {
        mutableStateOf(
            initialPromotionData?.promotionType?.let {
                when(it) {
                    "descuento" -> PromotionType.descuento
                    "multicompra" -> PromotionType.multicompra
                    "regalo" -> PromotionType.regalo
                    else -> PromotionType.otro
                }
            } ?: promo.promotionType ?: PromotionType.descuento
        )
    }

    // Estado de la promoción
    var promotionState by rememberSaveable {
        mutableStateOf(
            initialPromotionData?.promotionState?.let {
                when(it) {
                    "activa" -> PromotionState.activa
                    "inactiva" -> PromotionState.inactiva
                    "finalizada" -> PromotionState.finalizada
                    else -> PromotionState.activa
                }
            } ?: promo.promotionState ?: PromotionState.activa
        )
    }

    // Tema de la promoción
    var promoTheme by rememberSaveable {
        mutableStateOf(promo.theme ?: PromoTheme.light)
    }

    // Categorías y reservabilidad
    var availableCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategories by remember { mutableStateOf<Set<Int>>(promo.categories.mapNotNull { it.id }.toSet()) }
    var isBookable by rememberSaveable { mutableStateOf(promo.isBookable ?: false) }

    // Cargar categorías al iniciar
    LaunchedEffect(Unit) {
        runCatching {
            availableCategories = RemoteServiceCategory.getCategories()
        }.onFailure { error ->
            android.util.Log.e("NewPromotionSheet", "Error loading categories", error)
        }
    }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // ---------- HEADER Y CUERPO ----------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
            Text(
                text = if (initialPromotionData != null) "Nueva Promoción (IA)" else "Nueva Promoción",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(16.dp))

        // Mensaje de error
        if (showError && errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFC62828),
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // Formulario
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                // Título
                Text("Información Básica", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                Spacer(Modifier.height(12.dp))

                FormTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Título de la Promoción*",
                    placeholder = "Ej. 2x1 en Pizzas Familiares"
                )
                Spacer(Modifier.height(16.dp))

                FormTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción*",
                    placeholder = "Describe los detalles de la promoción",
                    singleLine = false
                )
                Spacer(Modifier.height(16.dp))

                // Tipo de Promoción
                Text("Tipo de Promoción*", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextGrey)
                Spacer(Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PromotionTypeChip(
                        text = "Descuento",
                        selected = promotionType == PromotionType.descuento,
                        onClick = { promotionType = PromotionType.descuento },
                        modifier = Modifier.weight(1f)
                    )
                    PromotionTypeChip(
                        text = "Multicompra",
                        selected = promotionType == PromotionType.multicompra,
                        onClick = { promotionType = PromotionType.multicompra },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PromotionTypeChip(
                        text = "Regalo",
                        selected = promotionType == PromotionType.regalo,
                        onClick = { promotionType = PromotionType.regalo },
                        modifier = Modifier.weight(1f)
                    )
                    PromotionTypeChip(
                        text = "Otro",
                        selected = promotionType == PromotionType.otro,
                        onClick = { promotionType = PromotionType.otro },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Código Promocional (opcional)
                FormTextField(
                    value = promotionString,
                    onValueChange = { promotionString = it.uppercase() },
                    label = "Código Promocional (opcional)",
                    placeholder = "Ej. PIZZA2X1"
                )

                Spacer(Modifier.height(24.dp))

                // Vigencia
                Text("Vigencia", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                Spacer(Modifier.height(12.dp))

                Row {
                    DatePickerField(
                        label = "Fecha Inicio*",
                        value = startDate,
                        onPicked = { startDate = it },
                        show = showStartPicker,
                        onOpen = { showStartPicker = true },
                        onDismiss = { showStartPicker = false },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    DatePickerField(
                        label = "Fecha Final*",
                        value = endDate,
                        onPicked = { endDate = it },
                        show = showEndPicker,
                        onOpen = { showEndPicker = true },
                        onDismiss = { showEndPicker = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Límites y Stock
                Text("Límites y Stock", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                Spacer(Modifier.height(12.dp))

                FormTextField(
                    value = totalStock,
                    onValueChange = { totalStock = it.filter(Char::isDigit) },
                    label = "Stock Total*",
                    placeholder = "100"
                )
                Spacer(Modifier.height(16.dp))

                Row {
                    FormTextField(
                        value = limitPerUser,
                        onValueChange = { limitPerUser = it.filter(Char::isDigit) },
                        label = "Límite por Usuario*",
                        placeholder = "1",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    FormTextField(
                        value = dailyLimitPerUser,
                        onValueChange = { dailyLimitPerUser = it.filter(Char::isDigit) },
                        label = "Límite Diario*",
                        placeholder = "1",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Imagen
                Text("Imagen de la Promoción", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                Spacer(Modifier.height(8.dp))
                Text("URL de imagen (opcional)", fontSize = 14.sp, color = TextGrey)
                Spacer(Modifier.height(8.dp))

                FormTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = "",
                    placeholder = "https://ejemplo.com/imagen.jpg"
                )
                Spacer(Modifier.height(12.dp))

                // Botón de generar imagen con IA
                Button(
                    onClick = {
                        if (title.isBlank() || description.isBlank()) {
                            errorMessage = "Necesitas título y descripción para generar imagen"
                            showError = true
                            return@Button
                        }

                        scope.launch {
                            isGeneratingImage = true
                            errorMessage = ""
                            showError = false

                            val result = ImageGenerationService.generatePromotionImage(
                                title = title,
                                description = description
                            )

                            isGeneratingImage = false

                            result.onSuccess { url ->
                                imageUrl = url
                            }.onFailure { error ->
                                errorMessage = "Error al generar imagen: ${error.message}"
                                showError = true
                            }
                        }
                    },
                    enabled = !isGeneratingImage && title.isNotBlank() && description.isNotBlank(),
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
                            Text("🤖", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isGeneratingImage) "Generando imagen..." else "Generar Imagen con IA",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Preview de imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(LightGrey, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotBlank()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "✓ Imagen configurada",
                                color = Teal,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = imageUrl.take(50) + if (imageUrl.length > 50) "..." else "",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "Sin Imagen",
                            color = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Estado de la promoción
                Text("Estado", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PromotionStateChip(
                        text = "Activa",
                        selected = promotionState == PromotionState.activa,
                        onClick = { promotionState = PromotionState.activa },
                        color = Teal,
                        modifier = Modifier.weight(1f)
                    )
                    PromotionStateChip(
                        text = "Inactiva",
                        selected = promotionState == PromotionState.inactiva,
                        onClick = { promotionState = PromotionState.inactiva },
                        color = Color(0xFFFFA726),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Tema de la promoción
                Text("Tema Visual", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                Spacer(Modifier.height(8.dp))
                Text("Selecciona el estilo visual del cupón", fontSize = 14.sp, color = TextGrey)
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PromotionStateChip(
                        text = "Claro",
                        selected = promoTheme == PromoTheme.light,
                        onClick = { promoTheme = PromoTheme.light },
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    PromotionStateChip(
                        text = "Oscuro",
                        selected = promoTheme == PromoTheme.dark,
                        onClick = { promoTheme = PromoTheme.dark },
                        color = Color(0xFF424242),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Categorías
                Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                Spacer(Modifier.height(8.dp))
                Text("Selecciona al menos una categoría*", fontSize = 14.sp, color = TextGrey)
                Spacer(Modifier.height(12.dp))

                if (availableCategories.isEmpty()) {
                    Text("Cargando categorías...", fontSize = 14.sp, color = Color.Gray)
                } else {
                    // Grid de categorías
                    availableCategories.chunked(2).forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCategories.forEach { category ->
                                category.id?.let { categoryId ->
                                    PromotionStateChip(
                                        text = category.name ?: "Sin nombre",
                                        selected = selectedCategories.contains(categoryId),
                                        onClick = {
                                            selectedCategories = if (selectedCategories.contains(categoryId)) {
                                                selectedCategories - categoryId
                                            } else {
                                                selectedCategories + categoryId
                                            }
                                        },
                                        color = Purple,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            // Relleno si hay impar
                            if (rowCategories.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Reservabilidad
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGrey
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "¿Cupón Reservable?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DarkBlue
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Permite a los usuarios reservar el cupón para usarlo después",
                                fontSize = 12.sp,
                                color = TextGrey
                            )
                        }
                        Switch(
                            checked = isBookable,
                            onCheckedChange = { isBookable = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Teal,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // Botón de crear
        MainButton(
            text = "Crear Nueva Promoción",
            onClick = {
                // Validar campos obligatorios
                if (collaboratorId == "anonymous") {
                    errorMessage = "Usuario no autenticado"
                    showError = true
                    return@MainButton
                }

                val tStock = totalStock.toIntOrNull()
                val lpu = limitPerUser.toIntOrNull()
                val dlpu = dailyLimitPerUser.toIntOrNull()

                if (title.isBlank()) {
                    errorMessage = "El título es obligatorio"
                    showError = true
                    return@MainButton
                }

                if (description.isBlank()) {
                    errorMessage = "La descripción es obligatoria"
                    showError = true
                    return@MainButton
                }

                if (startDate.isBlank() || endDate.isBlank()) {
                    errorMessage = "Las fechas son obligatorias"
                    showError = true
                    return@MainButton
                }

                if (tStock == null || tStock <= 0) {
                    errorMessage = "El stock total debe ser mayor a 0"
                    showError = true
                    return@MainButton
                }

                if (lpu == null || lpu <= 0) {
                    errorMessage = "El límite por usuario debe ser mayor a 0"
                    showError = true
                    return@MainButton
                }

                if (dlpu == null || dlpu <= 0) {
                    errorMessage = "El límite diario debe ser mayor a 0"
                    showError = true
                    return@MainButton
                }

                if (selectedCategories.isEmpty()) {
                    errorMessage = "Debes seleccionar al menos una categoría"
                    showError = true
                    return@MainButton
                }

                // Convertir fechas al formato ISO que espera el servidor
                val isoStartDate = if (startDate.isNotBlank()) {
                    "${startDate}T00:00:00.000Z"
                } else startDate

                val isoEndDate = if (endDate.isNotBlank()) {
                    "${endDate}T23:59:59.999Z"
                } else endDate

                // Crear lista de categorías
                val categoryList = selectedCategories.mapNotNull { categoryId ->
                    availableCategories.find { it.id == categoryId }
                }

                // Crear promoción
                val newPromo = Promotions(
                    collaboratorId = collaboratorId,
                    title = title.trim(),
                    description = description.trim(),
                    imageUrl = imageUrl.ifBlank { null },
                    initialDate = isoStartDate,
                    endDate = isoEndDate,
                    promotionType = promotionType,
                    promotionString = promotionString.trim().ifBlank { null },
                    totalStock = tStock,
                    availableStock = tStock,
                    limitPerUser = lpu,
                    dailyLimitPerUser = dlpu,
                    promotionState = promotionState,
                    theme = promoTheme,
                    isBookable = isBookable,
                    categories = categoryList
                )

                scope.launch {
                    runCatching { viewModel.createPromotion(newPromo) }
                        .onSuccess {
                            showError = false
                            onClose()
                        }
                        .onFailure { error ->
                            errorMessage = "Error al crear promoción: ${error.message}"
                            showError = true
                            android.util.Log.e("NewPromotionSheet", "Error creating promotion", error)
                        }
                }
            }
        )
    }
}

// Componentes auxiliares
@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    if (label.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextGrey)
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = singleLine,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = Color.LightGray
                )
            )
        }
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = Color.LightGray
            )
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
    val clickSrc = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextGrey)
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = {},               // readOnly: no edición directa
            readOnly = true,
            placeholder = { Text("YYYY-MM-DD", fontSize = 14.sp, color = Color.Gray) },
            leadingIcon = {
                IconButton(onClick = onOpen) {   // el ícono también abre
                    Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha", tint = Teal)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                // 1) Abre al recibir foco (tap)
                .onFocusChanged { if (it.isFocused) onOpen() }
                // 2) Abre con click explícito como respaldo
                .clickable(
                    interactionSource = clickSrc,
                    indication = null
                ) { onOpen() },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            )
        )
    }

    if (show) {
        // Si ya tienes un valor, úsalo como selección inicial
        val initialMillis = remember(value) {
            runCatching {
                if (value.isNotBlank())
                    java.time.LocalDate.parse(value).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                else null
            }.getOrNull()
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis ?: return@TextButton
                    val date = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .toString()
                    onPicked(date)
                    onDismiss()
                }) { Text("Aceptar", color = Teal) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
        ) {
            DatePicker(
                state = state,
                showModeToggle = true,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Teal,
                    todayContentColor = Teal,
                    todayDateBorderColor = Teal
                )
            )
        }
    }
}


@Composable
private fun PromotionTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Teal else Color.LightGray.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextGrey,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PromotionStateChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) color else Color.LightGray.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextGrey,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}
