package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.android.awaitFrame
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
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

    // Navegación post-registro
    LaunchedEffect(authState.needsConfirmation) {
        if (authState.needsConfirmation) {
            nav.navigate(Screens.confirmSignUpWithEmail(email)) {
                popUpTo(Screens.Register.route) { inclusive = true }
            }
        }
    }

    // Errores de registro
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            errorMessage = error
            showError = true
        }
    }

    // Validación
    val isFormValid = nombre.isNotBlank() &&
            apPaterno.isNotBlank() &&
            apMaterno.isNotBlank() &&
            fechaNacDb.isNotBlank() &&
            email.isNotBlank() &&
            phone.isNotBlank() &&
            password.isNotBlank() &&
            acceptTerms

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // Footer en bottomBar: se eleva con IME sin empujar el contenido
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime) // pega al teclado
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                MainButton(
                    text = if (authState.isLoading) "Registrando..." else "Continuar",
                    enabled = !authState.isLoading && isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    showError = false
                    val userProfile = UserProfile(
                        nombre = nombre,
                        apellidoPaterno = apPaterno,
                        apellidoMaterno = apMaterno,
                        fechaNacimiento = fechaNacDb,
                        telefono = phone,
                        email = email
                    )
                    appViewModel?.savePendingUserProfile(userProfile)
                    authViewModel.signUp(email, password, phone)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 4.dp),
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
    ) { innerPadding ->

        // Contenido scrollable
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .dismissKeyboardOnTap(),
            contentPadding = PaddingValues(
                start = 24.dp, end = 24.dp,
                top = 24.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_beneficio_joven),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            item {
                Text(
                    "Regístrate",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )
            }

            // Nombre
            item { Label("Nombre", top = 4.dp) }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        singleLine = true,
                        modifier = it
                            .fillMaxWidth()
                            .heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        placeholder = { Text("Nombre", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Apellido paterno
            item { Label("Apellido Paterno") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = apPaterno,
                        onValueChange = { apPaterno = it },
                        singleLine = true,
                        modifier = it
                            .fillMaxWidth()
                            .heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        placeholder = { Text("Apellido Paterno", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Apellido materno
            item { Label("Apellido Materno") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = apMaterno,
                        onValueChange = { apMaterno = it },
                        singleLine = true,
                        modifier = it
                            .fillMaxWidth()
                            .heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        placeholder = { Text("Apellido Materno", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Fecha de nacimiento (readOnly)
            item { Label("Fecha de Nacimiento") }
            item {
                BirthDateField(
                    value = fechaNacDisplay,
                    onDateSelected = { localDate ->
                        fechaNacDisplay = localDate.format(displayFormatterEs)   // bonito para UI
                        fechaNacDb = localDate.format(storageFormatter)          // ISO para PostgreSQL
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Teléfono
            item { Label("Número Telefónico") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        singleLine = true,
                        modifier = it
                            .fillMaxWidth()
                            .heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                        placeholder = { Text("55 1234 5678", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Correo
            item { Label("Correo Electrónico") }
            item {
                FocusBringIntoView {
                    EmailTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = it.fillMaxWidth()
                    )
                }
            }

            // Contraseña
            item { Label("Contraseña") }
            item {
                FocusBringIntoView {
                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = it.fillMaxWidth()
                    )
                }
            }

            // Términos
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
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
            }

            // Error
            if (showError && errorMessage.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                            ) { Text("✕", color = Color(0xFFD32F2F)) }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* ---------- Focus helper: hace scroll estable hasta el campo enfocado ---------- */
@Composable
private fun FocusBringIntoView(
    delayMs: Long = 140,
    content: @Composable (Modifier) -> Unit
) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    // Solo usamos el requester y esperamos un frame + pequeño delay
    val mod = Modifier
        .bringIntoViewRequester(requester)
        .onFocusEvent { state ->
            if (state.isFocused) {
                scope.launch {
                    // Espera a que Compose mida con el IME abierto
                    awaitFrame()
                    delay(delayMs) // amortigua "saltos" en distintos OEM/teclados
                    requester.bringIntoView()
                }
            }
        }

    content(mod)
}

/* ------------------------------ DATE PICKER FIELD ------------------------------ */
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
            .clickable { showDialog.value = true }
    )

    if (showDialog.value) {
        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
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

/* ------------------------------ FORMATOS FECHA ------------------------------ */

// Locale recomendado (sin deprecated)
private val localeEsMx: Locale = Locale.Builder()
    .setLanguage("es")
    .setRegion("MX")
    .build()

// Formato para mostrar en UI: "01/02/2003"
private val displayFormatterEs: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", localeEsMx)
// Formato para BD (PostgreSQL DATE): "2003-02-01"
private val storageFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

// Convierte el string de display (si no está vacío) a millis para preseleccionar el DatePicker
private fun String.toMillisFromDisplayOrNull(zone: ZoneId): Long? = runCatching {
    if (this.isBlank()) return null
    val parsed = LocalDate.parse(this, displayFormatterEs)
    parsed.atStartOfDay(zone).toInstant().toEpochMilli()
}.getOrNull()

/* ------------------------------ ESTILO ------------------------------ */

@Composable
private fun Label(text: String, top: Dp = 0.dp) {
    Text(
        text,
        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A)),
        modifier = Modifier.padding(top = top, bottom = 8.dp)
    )
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,

    focusedIndicatorColor = Color(0xFFD3D3D3),
    unfocusedIndicatorColor = Color(0xFFD3D3D3),

    cursorColor = Color(0xFF008D96),

    focusedLeadingIconColor = Color(0xFF7D7A7A),
    unfocusedLeadingIconColor = Color(0xFF7D7A7A),

    focusedTrailingIconColor = Color(0xFF7D7A7A),
    unfocusedTrailingIconColor = Color(0xFF7D7A7A),

    focusedPlaceholderColor = Color(0xFF616161),
    unfocusedPlaceholderColor = Color(0xFF616161),
)
