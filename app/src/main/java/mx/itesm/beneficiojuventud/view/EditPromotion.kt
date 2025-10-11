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
import mx.itesm.beneficiojuventud.model.webhook.Category
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPromotion(
    nav: NavHostController,
    promotionData: PromotionData? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BJTab.Home) }

    // Estados editables con valores por defecto
    var title by remember { mutableStateOf(promotionData?.title ?: "") }
    var description by remember { mutableStateOf(promotionData?.description ?: "") }
    var initialDate by remember { mutableStateOf(promotionData?.initialDate ?: getDefaultInitialDate()) }
    var endDate by remember { mutableStateOf(promotionData?.endDate ?: getDefaultEndDate()) }
    var promotionType by remember { mutableStateOf(promotionData?.promotionType ?: "descuento") }
    var totalStock by remember { mutableStateOf(promotionData?.totalStock?.toString() ?: "100") }
    var limitPerUser by remember { mutableStateOf(promotionData?.limitPerUser?.toString() ?: "1") }
    var dailyLimitPerUser by remember { mutableStateOf(promotionData?.dailyLimitPerUser?.toString() ?: "1") }
    var promotionState by remember { mutableStateOf(promotionData?.promotionState ?: "activa") }

    // DatePicker states
    var showInitialDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val datePickerStateInitial = rememberDatePickerState()
    val datePickerStateEnd = rememberDatePickerState()

    val scrollState = rememberScrollState()

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

                    // Título
                    EditableField(
                        label = "Título de la promoción",
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "Ingresa el título de la promoción"
                    )

                    // Descripción
                    EditableField(
                        label = "Descripción",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Describe los detalles de la promoción",
                        maxLines = 4
                    )

                    // Fechas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Fecha inicial
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

                        // Fecha final
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

                    // Tipo y Estado
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
                            options = listOf("activa", "inactiva", "pausada", "vencida"),
                            onValueChange = { promotionState = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Límites
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

                    // Categorías (por ahora solo mostrar, después se puede hacer editable)
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

                    // Botón guardar
                    Button(
                        onClick = {
                            // TODO: Implementar guardado
                            nav.popBackStack()
                        },
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
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Guardar Promoción",
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

    // DatePicker para fecha inicial
    if (showInitialDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                initialDate = formatApiDate(date)
                showInitialDatePicker = false
            },
            onDismiss = { showInitialDatePicker = false }
        )
    }

    // DatePicker para fecha final
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
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = "Notificaciones",
                tint = Color(0xFF008D96),
                modifier = Modifier.size(26.dp)
            )
        }

        GradientDivider(
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

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

private fun formatApiDate(timeInMillis: Long): String {
    val date = Date(timeInMillis)
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(date)
}

private fun getDefaultInitialDate(): String {
    val today = Date()
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(today)
}

private fun getDefaultEndDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 30) // 30 días después
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(calendar.time)
}

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
            categories = listOf(Category(1, "Entretenimiento"))
        )
        EditPromotion(nav = nav, promotionData = samplePromotion)
    }
}