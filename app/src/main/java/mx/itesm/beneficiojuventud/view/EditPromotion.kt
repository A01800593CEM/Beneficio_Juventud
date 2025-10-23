package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.model.webhook.PromotionData
import mx.itesm.beneficiojuventud.model.webhook.WebhookCategory
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de edición/creación de promociones generadas por IA.
 * Permite ajustar campos básicos y fechas antes de guardar.
 * @param nav Controlador de navegación.
 * @param promotionData Datos iniciales de la promoción; si es null se asume nueva.
 * @param modifier Modificador externo para layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPromotion(
    nav: NavHostController,
    promotionData: PromotionData? = null,
    viewModel: mx.itesm.beneficiojuventud.viewmodel.PromoViewModel? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BJTab.Home) }

    var title by remember { mutableStateOf(promotionData?.title ?: "") }
    var description by remember { mutableStateOf(promotionData?.description ?: "") }
    var initialDate by remember { mutableStateOf(promotionData?.initialDate ?: getDefaultInitialDate()) }
    var endDate by remember { mutableStateOf(promotionData?.endDate ?: getDefaultEndDate()) }
    var promotionType by remember { mutableStateOf(promotionData?.promotionType ?: "descuento") }
    var totalStock by remember { mutableStateOf(promotionData?.totalStock?.toString() ?: "100") }
    var limitPerUser by remember { mutableStateOf(promotionData?.limitPerUser?.toString() ?: "1") }
    var dailyLimitPerUser by remember { mutableStateOf(promotionData?.dailyLimitPerUser?.toString() ?: "1") }
    var promotionState by remember { mutableStateOf(promotionData?.promotionState ?: "activa") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showInitialDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val datePickerStateInitial = rememberDatePickerState()
    val datePickerStateEnd = rememberDatePickerState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBarSection(nav = nav)
        },
        bottomBar = {
            BJBottomBar(
                selected = selectedTab,
                onSelect = { tab ->
                    selectedTab = tab
                    when (tab) {
                        BJTab.Home      -> nav.navigate(Screens.Home.route)
                        BJTab.Coupons   -> nav.navigate(Screens.Coupons.route)
                        BJTab.Favorites -> nav.navigate(Screens.Favorites.route)
                        BJTab.Profile   -> nav.navigate(Screens.Profile.route)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            HeaderSection(isCreatingNew = promotionData == null)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Editar Promoción",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B2B2B)
                    )

                    Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    EditableField(
                        label = "Título de la promoción",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Ingresa el título de la promoción"
                    )

                    EditableField(
                        label = "Descripción",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Describe los detalles de la promoción",
                        maxLines = 4
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fecha de inicio",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2B2B2B)
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = formatDisplayDate(initialDate),
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showInitialDatePicker = true },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Seleccionar fecha",
                                        tint = Color(0xFF7B68EE)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF7B68EE),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fecha de fin",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2B2B2B)
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = formatDisplayDate(endDate),
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEndDatePicker = true },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Seleccionar fecha",
                                        tint = Color(0xFF7B68EE)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF7B68EE),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DropdownField(
                            label = "Tipo de promoción",
                            value = promotionType,
                            options = listOf("descuento", "multicompra", "gratis", "cashback"),
                            onValueChange = { promotionType = it },
                            modifier = Modifier.weight(1f)
                        )

                        DropdownField(
                            label = "Estado",
                            value = promotionState,
                            options = listOf("activa", "inactiva", "finalizada"),
                            onValueChange = { promotionState = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NumberField(
                            label = "Stock total",
                            value = totalStock,
                            onValueChange = { totalStock = it },
                            modifier = Modifier.weight(1f)
                        )

                        NumberField(
                            label = "Límite por usuario",
                            value = limitPerUser,
                            onValueChange = { limitPerUser = it },
                            modifier = Modifier.weight(1f)
                        )

                        NumberField(
                            label = "Límite diario",
                            value = dailyLimitPerUser,
                            onValueChange = { dailyLimitPerUser = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (promotionData?.categories?.isNotEmpty() == true) {
                        Column {
                            Text(
                                text = "Categorías:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2B2B2B)
                            )
                            Spacer(Modifier.height(8.dp))
                            promotionData.categories.forEach { category ->
                                Text(
                                    text = "• ${category.name}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF616161)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Error message display
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (promotionData != null && viewModel != null) {
                                isSaving = true
                                errorMessage = null

                                coroutineScope.launch {
                                    try {
                                        // Convert promotionType string to enum
                                        val typeEnum = when (promotionType) {
                                            "descuento" -> mx.itesm.beneficiojuventud.model.promos.PromotionType.descuento
                                            "multicompra" -> mx.itesm.beneficiojuventud.model.promos.PromotionType.multicompra
                                            "regalo" -> mx.itesm.beneficiojuventud.model.promos.PromotionType.regalo
                                            else -> mx.itesm.beneficiojuventud.model.promos.PromotionType.otro
                                        }

                                        // Convert promotionState string to enum
                                        val stateEnum = when (promotionState) {
                                            "activa" -> mx.itesm.beneficiojuventud.model.promos.PromotionState.activa
                                            "inactiva" -> mx.itesm.beneficiojuventud.model.promos.PromotionState.inactiva
                                            "finalizada" -> mx.itesm.beneficiojuventud.model.promos.PromotionState.finalizada
                                            else -> mx.itesm.beneficiojuventud.model.promos.PromotionState.activa
                                        }

                                        // Create updated Promotions object
                                        val updatedPromo = mx.itesm.beneficiojuventud.model.promos.Promotions(
                                            promotionId = promotionData.promotionId,
                                            collaboratorId = promotionData.collaboratorId,
                                            title = title,
                                            description = description,
                                            imageUrl = promotionData.imageUrl,
                                            initialDate = initialDate,
                                            endDate = endDate,
                                            promotionType = typeEnum,
                                            promotionString = promotionData.promotionString,
                                            totalStock = totalStock.toIntOrNull(),
                                            availableStock = promotionData.availableStock,
                                            limitPerUser = limitPerUser.toIntOrNull(),
                                            dailyLimitPerUser = dailyLimitPerUser.toIntOrNull(),
                                            promotionState = stateEnum,
                                            isBookable = promotionData.isBookable,
                                            theme = promotionData.theme?.let {
                                                when (it) {
                                                    "dark" -> mx.itesm.beneficiojuventud.model.promos.PromoTheme.dark
                                                    "light" -> mx.itesm.beneficiojuventud.model.promos.PromoTheme.light
                                                    else -> mx.itesm.beneficiojuventud.model.promos.PromoTheme.light
                                                }
                                            },
                                            categories = promotionData.categories.map {
                                                mx.itesm.beneficiojuventud.model.categories.Category(it.id, it.name)
                                            },
                                            branches = emptyList()
                                        )

                                        // Update promotion via ViewModel
                                        promotionData.promotionId?.let { id ->
                                            viewModel.updatePromotion(id, updatedPromo)
                                        }

                                        // Navigate back on success
                                        withContext(Dispatchers.Main) {
                                            isSaving = false
                                            nav.popBackStack()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isSaving = false
                                            errorMessage = "Error al guardar: ${e.message}"
                                        }
                                    }
                                }
                            } else {
                                errorMessage = "No se puede guardar: datos incompletos"
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B68EE)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isSaving) "Guardando..." else "Guardar Promoción",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showInitialDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                initialDate = formatApiDate(date)
                showInitialDatePicker = false
            },
            onDismiss = { showInitialDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = formatApiDate(date)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

/**
 * Barra superior con logo, botón de regreso y acceso a notificaciones.
 * @param nav Controlador de navegación para volver a la pantalla previa.
 */
@Composable
private fun TopBarSection(nav: NavHostController) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo_beneficio_joven),
                contentDescription = "Logo",
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(nav = nav)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Editar Promoción",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF616161)
                )
            }
        }

        GradientDivider(
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

/**
 * Encabezado visual que comunica el modo de creación o edición.
 * @param isCreatingNew Indica si se está creando una promoción nueva.
 */
@Composable
private fun HeaderSection(isCreatingNew: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Editar",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isCreatingNew)
                "Crea una nueva promoción personalizada"
            else
                "Revisa y edita los detalles de tu promoción",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Campo de texto genérico con etiqueta y placeholder.
 * @param label Etiqueta del campo.
 * @param value Valor actual.
 * @param onValueChange Callback al cambiar el valor.
 * @param placeholder Texto guía cuando está vacío.
 * @param maxLines Máximo de líneas permitidas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLines: Int = 1
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFAEAEAE)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7B68EE),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}

/**
 * Selector desplegable para opciones predefinidas.
 * @param label Etiqueta del campo.
 * @param value Opción seleccionada.
 * @param options Lista de opciones disponibles.
 * @param onValueChange Callback al seleccionar una opción.
 * @param modifier Modificador para layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B)
        )
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF7B68EE),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Campo numérico restringido a dígitos.
 * @param label Etiqueta del campo.
 * @param value Valor actual como texto.
 * @param onValueChange Callback al cambiar si el valor es válido.
 * @param modifier Modificador para layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B2B2B)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7B68EE),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}

/**
 * Diálogo de selección de fecha con confirmación y cancelación.
 * @param onDateSelected Devuelve el tiempo seleccionado en milisegundos UTC.
 * @param onDismiss Acción al cerrar el diálogo sin seleccionar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(it) }
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * Formatea fecha ISO a presentación dd/MM/yyyy.
 * @param isoDate Cadena ISO-8601 con sufijo Z.
 * @return Fecha formateada o el valor original si falla el parseo.
 */
private fun formatDisplayDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        isoDate
    }
}

/**
 * Formatea milisegundos a ISO-8601 UTC con milisegundos y sufijo Z.
 * @param timeInMillis Tiempo en milisegundos.
 * @return Cadena ISO-8601 en UTC.
 */
private fun formatApiDate(timeInMillis: Long): String {
    val date = Date(timeInMillis)
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(date)
}

/**
 * Obtiene la fecha actual en formato ISO-8601 UTC.
 * @return Fecha actual ISO-8601 en UTC.
 */
private fun getDefaultInitialDate(): String {
    val today = Date()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(today)
}

/**
 * Obtiene una fecha 30 días posterior a la actual en ISO-8601 UTC.
 * @return Fecha a +30 días en ISO-8601 UTC.
 */
private fun getDefaultEndDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 30)
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(calendar.time)
}

/**
 * Vista previa con datos de ejemplo para validar el diseño en el IDE.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditPromotionPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        val samplePromotion = PromotionData(
            title = "2x1 en Hamburguesas",
            description = "Promoción especial de hamburguesas para estudiantes",
            initialDate = "2024-06-20T00:00:00.000Z",
            endDate = "2024-06-28T23:59:59.000Z",
            promotionType = "multicompra",
            totalStock = 200,
            limitPerUser = 4,
            dailyLimitPerUser = 1,
            promotionState = "activa",
            categories = listOf(WebhookCategory(1, "Entretenimiento"))
        )
        EditPromotion(nav = nav, promotionData = samplePromotion)
    }
}
