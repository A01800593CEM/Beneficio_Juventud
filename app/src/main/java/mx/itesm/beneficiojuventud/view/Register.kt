package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.components.PasswordTextField
import mx.itesm.beneficiojuventud.model.UserProfile
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.AppViewModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun Register(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel? = null,
    authViewModel: AuthViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var apPaterno by remember { mutableStateOf("") }
    var apMaterno by remember { mutableStateOf("") }

    // Fecha de nacimiento: display (UI) + db (ISO para PostgreSQL)
    var fechaNacDisplay by remember { mutableStateOf("") }     // ej. "01/02/2003"
    var fechaNacDb by remember { mutableStateOf("") }          // ej. "2003-02-01"

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val scroll = rememberScrollState()

    val scope = rememberCoroutineScope()
    val bringNombre   = remember { BringIntoViewRequester() }
    val bringApPat    = remember { BringIntoViewRequester() }
    val bringApMat    = remember { BringIntoViewRequester() }
    val bringPhone    = remember { BringIntoViewRequester() }
    val bringEmail    = remember { BringIntoViewRequester() }
    val bringPassword = remember { BringIntoViewRequester() }

    // Manejar navegación después del registro exitoso
    LaunchedEffect(authState.needsConfirmation) {
        if (authState.needsConfirmation) {
            // Navegar a la pantalla de confirmación de registro (no recovery)
            nav.navigate(Screens.confirmSignUpWithEmail(email)) {
                popUpTo(Screens.Register.route) { inclusive = true }
            }
        }
    }

    // Manejar errores de registro
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            errorMessage = error
            showError = true
        }
    }

    // Validar campos
    val isFormValid = nombre.isNotBlank() &&
                     apPaterno.isNotBlank() &&
                     apMaterno.isNotBlank() &&
                     fechaNacDb.isNotBlank() &&
                     email.isNotBlank() &&
                     phone.isNotBlank() &&
                     password.isNotBlank() &&
                     acceptTerms

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .dismissKeyboardOnTap()
                .imePadding()
                .verticalScroll(scroll)
                .padding(top = 85.dp, bottom = 24.dp) // bottom para que no tape el botón
        ) {
            // Logo
            Box(modifier = Modifier.padding(horizontal = 30.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_beneficio_joven),
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
            }

            // Título
            Text(
                "Regístrate",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = Modifier.padding(24.dp, 18.dp, 20.dp, 14.dp)
            )

            // Nombre
            Label("Nombre")
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight)
                    .bringIntoViewRequester(bringNombre)
                    .onFocusEvent { if (it.isFocused) scope.launch { bringNombre.bringIntoView() } },
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                placeholder = { Text("Nombre", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors()
            )

            Label("Apellido Paterno", top = 20.dp)
            OutlinedTextField(
                value = apPaterno,
                onValueChange = { apPaterno = it },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight)
                    .bringIntoViewRequester(bringApPat)
                    .onFocusEvent { if (it.isFocused) scope.launch { bringApPat.bringIntoView() } },
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Apellido Paterno", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors()
            )

            Label("Apellido Materno", top = 20.dp)
            OutlinedTextField(
                value = apMaterno,
                onValueChange = { apMaterno = it },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight)
                    .bringIntoViewRequester(bringApMat)
                    .onFocusEvent { if (it.isFocused) scope.launch { bringApMat.bringIntoView() } },
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Apellido Materno", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors()
            )

            // Fecha de Nacimiento (UI + DB)
            Label("Fecha de Nacimiento", top = 20.dp)
            BirthDateField(
                value = fechaNacDisplay,
                onDateSelected = { localDate ->
                    fechaNacDisplay = localDate.format(displayFormatterEs)   // bonito para UI
                    fechaNacDb = localDate.format(storageFormatter)          // ISO para PostgreSQL
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // Número Telefónico
            Label("Número Telefónico", top = 20.dp)
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                singleLine = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(min = TextFieldDefaults.MinHeight)
                    .bringIntoViewRequester(bringPhone)
                    .onFocusEvent { if (it.isFocused) scope.launch { bringPhone.bringIntoView() } },
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                placeholder = { Text("55 1234 5678", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                colors = textFieldColors()
            )

            // Correo
            Label("Correo Electrónico", top = 20.dp)
            EmailTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.padding(horizontal = 24.dp)
                .bringIntoViewRequester(bringEmail)
                .onFocusEvent { if (it.isFocused) scope.launch { bringEmail.bringIntoView() } }
            )

            // Contraseña
            Label("Contraseña", top = 20.dp)
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.padding(horizontal = 24.dp)
                .bringIntoViewRequester(bringPassword)
                .onFocusEvent { if (it.isFocused) scope.launch { bringPassword.bringIntoView() } }
            )

            // Términos y condiciones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
                Text(
                    buildAnnotatedString {
                        append("Estoy de acuerdo con los ")
                        pushStyle(
                            SpanStyle(
                                color = Color(0xFF008D96),
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        append("términos y condiciones")
                        pop()
                    },
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF7D7A7A))
                )
            }

            // Mostrar error si existe
            if (showError && errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                showError = false
                                errorMessage = ""
                                authViewModel.clearState()
                            }
                        ) {
                            Text("✕", color = Color(0xFFD32F2F))
                        }
                    }
                }
            }

            // Botón Continuar
            MainButton(
                text = if (authState.isLoading) "Registrando..." else "Continuar",
                enabled = !authState.isLoading && isFormValid,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                showError = false

                // Crear el perfil de usuario para guardar después en BD
                val userProfile = UserProfile(
                    nombre = nombre,
                    apellidoPaterno = apPaterno,
                    apellidoMaterno = apMaterno,
                    fechaNacimiento = fechaNacDb,
                    telefono = phone,
                    email = email
                )

                // Guardar datos del usuario temporalmente en AppViewModel
                appViewModel?.savePendingUserProfile(userProfile)

                // Solo enviar email, password y teléfono a Amplify
                authViewModel.signUp(email, password, phone)
            }

            // ¿Ya tienes cuenta?
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes cuenta?  ",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A))
                )
                TextButton(onClick = { nav.navigate(Screens.Login.route) }) {
                    Text(
                        "Inicia Sesión",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF008D96))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateField(
    value: String,                       // lo que se muestra en el TextField
    onDateSelected: (LocalDate) -> Unit, // devuelve la fecha elegida
    modifier: Modifier = Modifier
) {
    val showDialog = remember { mutableStateOf(false) }

    // Limitar fechas: 1900..hoy
    val today = remember { LocalDate.now() }
    val zone = remember { ZoneId.systemDefault() }
    val minMillis = remember { LocalDate.of(1900, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli() }
    val maxMillis = remember { today.atStartOfDay(zone).toInstant().toEpochMilli() }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value.toMillisFromDisplayOrNull(zone),
        yearRange = 1900..today.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minMillis..maxMillis
            override fun isSelectableYear(year: Int): Boolean = year in 1900..today.year
        }
    )

    OutlinedTextField(
        value = value,
        onValueChange = { /* readOnly */ },
        readOnly = true,
        singleLine = true,
        placeholder = { Text("DD/MM/AAAA", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
        trailingIcon = {
            IconButton(onClick = { showDialog.value = true }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Elegir fecha")
            }
        },
        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        colors = textFieldColors(),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TextFieldDefaults.MinHeight)
            .clickable { showDialog.value = true } // tap en todo el campo abre el date picker
    )

    if (showDialog.value) {
        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                        onDateSelected(localDate)
                    }
                    showDialog.value = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }
}

/* ---------- utils para formateo/parsing ---------- */

// Locale recomendado (sin deprecated)
private val localeEsMx: Locale = Locale.Builder()
    .setLanguage("es")
    .setRegion("MX")
    .build()

// Formato para mostrar en UI: "01/febrero/2003"
private val displayFormatterEs: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", localeEsMx) // p. ej. 01/02/2003
// Formato para BD (PostgreSQL DATE): "2003-02-01"
private val storageFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

// Convierte el string de display (si no está vacío) a millis para preseleccionar el DatePicker
private fun String.toMillisFromDisplayOrNull(zone: ZoneId): Long? = runCatching {
    if (this.isBlank()) return null
    val parsed = LocalDate.parse(this, displayFormatterEs)
    parsed.atStartOfDay(zone).toInstant().toEpochMilli()
}.getOrNull()

/* ---------- helpers de estilo ---------- */

@Composable
private fun Label(text: String, top: Dp = 0.dp) {
    Text(
        text,
        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A)),
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = top, bottom = 8.dp)
    )
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color(0xFFE0E0E0),
    unfocusedIndicatorColor = Color(0xFFE0E0E0),
    cursorColor = Color(0xFF008D96),
    focusedLeadingIconColor = Color(0xFF7D7A7A),
    unfocusedLeadingIconColor = Color(0xFF7D7A7A),
    focusedTrailingIconColor = Color(0xFF7D7A7A),
    unfocusedTrailingIconColor = Color(0xFF7D7A7A),
    focusedPlaceholderColor = Color(0xFF7D7A7A),
    unfocusedPlaceholderColor = Color(0xFF7D7A7A),
)
