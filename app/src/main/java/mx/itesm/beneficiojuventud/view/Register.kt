package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.components.PasswordTextField
import mx.itesm.beneficiojuventud.model.users.UserProfile
import mx.itesm.beneficiojuventud.model.users.AccountState
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.utils.isValidEmail
import mx.itesm.beneficiojuventud.utils.getEmailErrorMessage
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModelFactory
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla de registro de usuario con campos personales, fecha de nacimiento, contacto y credenciales.
 * Valida el formulario, persiste un perfil preliminar y dispara el sign-up; al requerir confirmación navega al OTP.
 * Maneja también el caso de signUp completo sin confirmación (auto sign-in, creación en BD y navegación).
 */
@Composable
fun Register(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current))
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var apPaterno by rememberSaveable { mutableStateOf("") }
    var apMaterno by rememberSaveable { mutableStateOf("") }

    // Fecha de nacimiento: display (UI) + db (ISO)
    var fechaNacDisplay by rememberSaveable { mutableStateOf("") }     // ej. "01/02/2003"
    var fechaNacDb by rememberSaveable { mutableStateOf("") }          // ej. "2003-02-01"
    // Error visible inmediato bajo el campo
    var birthDateErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    // Latch: evita re-navegar automáticamente al volver de Confirm;
    // se resetea en cada nuevo intento (al presionar "Continuar").
    var didNavigateToConfirm by rememberSaveable { mutableStateOf(false) }

    // Parche: si Cognito crea y confirma sin OTP
    var didCreateDirect by rememberSaveable { mutableStateOf(false) }

    // Edad válida si no hay error y hay fecha
    val isAgeValid = birthDateErrorMessage == null && fechaNacDb.isNotBlank()

    var isCheckingEmail by rememberSaveable { mutableStateOf(false) }

    // Parche: al volver del ConfirmSignUp, aseguramos que no quede en "Registrando."
    LaunchedEffect(Unit) {
        authViewModel.markIdle()
    }

    // Navegación a Confirm (idempotente por intento)
    LaunchedEffect(authState.needsConfirmation, didNavigateToConfirm) {
        if (authState.needsConfirmation && !didNavigateToConfirm) {
            didNavigateToConfirm = true
            nav.navigate(Screens.confirmSignUpWithEmail(email)) {
                // Mantener Register en el back stack para poder regresar
                popUpTo(Screens.Register.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    // SignUp completo sin confirmación (auto sign-in, crear usuario y navegar)
    LaunchedEffect(authState.isSuccess, authState.needsConfirmation) {
        if (didCreateDirect) return@LaunchedEffect
        if (authState.isSuccess && !authState.needsConfirmation) {
            val pending = authViewModel.consumePendingUserProfile()
            val sub = authState.cognitoSub

            if (pending == null || sub.isNullOrBlank()) return@LaunchedEffect

            didCreateDirect = true

            // Auto sign-in con lo que está en pantalla
            authViewModel.signIn(email.trim(), password)

            scope.launch {
                try {
                    // ⭐️ SINCRÓNICO: Esperar a que createUserAndWait() complete REALMENTE
                    val (success, createdUser, error) = userViewModel.createUserAndWait(
                        pending.copy(
                            cognitoId = sub,
                            email = email.trim(),
                            accountState = AccountState.activo
                        )
                    )

                    if (!success || error != null) {
                        throw Exception(error ?: "No se pudo crear el usuario en el servidor")
                    }

                    authViewModel.clearPendingCredentials()
                    nav.navigate(Screens.Onboarding.route) {
                        popUpTo(Screens.LoginRegister.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    didCreateDirect = false
                    showError = true
                    errorMessage = e.message ?: "No se pudo crear el perfil en la BD."
                }
            }
        }
    }

    // Errores: deja que el VM resuelva UsernameExists (UNCONFIRMED vs CONFIRMED)
    LaunchedEffect(authState.error) {
        authState.error?.let { raw ->
            val lower = raw.lowercase()
            val looksLikeExists =
                "usernameexistsexception" in lower ||
                        "already exists" in lower ||
                        ("email" in lower && "exists" in lower)

            if (looksLikeExists) return@LaunchedEffect // VM hará resend + needsConfirmation o error confirmado

            val friendly = mapAuthErrorToFriendly(raw)
            errorMessage = friendly
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
            acceptTerms &&
            isAgeValid

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                MainButton(
                    text = when {
                        authState.isLoading -> "Registrando."
                        isCheckingEmail     -> "Verificando correo."
                        else                -> "Continuar"
                    },
                    enabled = !authState.isLoading && !isCheckingEmail && isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Validar email antes de continuar
                    if (!isValidEmail(email)) {
                        emailError = true
                        return@MainButton
                    }

                    if (!isAgeValid) {
                        showError = true
                        errorMessage = birthDateErrorMessage ?: "Para registrarte debes tener entre 12 y 29 años."
                        return@MainButton
                    }

                    // 👇 FIX: nuevo intento → resetea el latch para permitir re-navegar a Confirm
                    didNavigateToConfirm = false

                    // Guardar datos temporales como ya lo haces
                    val userProfile = UserProfile(
                        name = nombre,
                        lastNamePaternal = apPaterno,
                        lastNameMaternal = apMaterno,
                        birthDate = fechaNacDb,
                        phoneNumber = phone,
                        email = email
                    )
                    authViewModel.savePendingUserProfile(userProfile)
                    authViewModel.setPendingCredentials(email, password)

                    // Pre-checar si el correo ya existe en la BD
                    scope.launch {
                        showError = false
                        isCheckingEmail = true
                        try {
                            val exists = userViewModel.emailExists(email.trim())
                            if (exists) {
                                showError = true
                                errorMessage = "Este correo ya está registrado. Inicia sesión o restablece tu contraseña."
                                return@launch
                            }
                        } catch (e: Exception) {
                            showError = true
                            errorMessage = "No se pudo verificar el correo. Revisa tu conexión e inténtalo de nuevo."
                            return@launch
                        } finally {
                            isCheckingEmail = false
                        }

                        // Si no existe → continuar con registro Cognito
                        authViewModel.signUp(email, password)
                    }
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

        // Contenido
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
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF2F2F2F)
                        ),
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
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        placeholder = { Text("Apellido Paterno", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF2F2F2F)
                        ),
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
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        placeholder = { Text("Apellido Materno", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF2F2F2F)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Fecha de nacimiento (readOnly con error visible inmediato)
            item { Label("Fecha de Nacimiento") }
            item {
                BirthDateField(
                    value = fechaNacDisplay,
                    onDateSelected = { localDate ->
                        // Actualiza display y storage
                        fechaNacDisplay = localDate.format(displayFormatterEs)
                        fechaNacDb = localDate.format(storageFormatter)

                        // Valida edad al momento
                        val edad = computeAgeFromIso(fechaNacDb)
                        birthDateErrorMessage = when {
                            edad == null -> "Selecciona una fecha válida."
                            edad < 12 -> "Debes tener al menos 12 años para registrarte."
                            edad > 29 -> "El programa está limitado a jóvenes de hasta 29 años."
                            else -> null
                        }
                    },
                    errorMessage = birthDateErrorMessage,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Teléfono
            item { Label("Número Telefónico") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { input ->
                            // Acepta solo dígitos y limita a 10 (55 5555 5555)
                            val digits = input.filter { it.isDigit() }.take(10)
                            phone = digits
                        },
                        singleLine = true,
                        modifier = it
                            .fillMaxWidth()
                            .heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                        placeholder = { Text("55 5555 5555", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF2F2F2F)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors(),
                        visualTransformation = MxPhoneVisualTransformation()
                    )
                }
            }

            // Correo
            item { Label("Correo Electrónico") }
            item {
                FocusBringIntoView {
                    EmailTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                        },
                        modifier = it.fillMaxWidth(),
                        isError = emailError,
                        errorMessage = if (emailError) getEmailErrorMessage() else ""
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
                        style = TextStyle(fontSize = 14.sp, color = Color(0xFF7D7A7A)),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            nav.navigate(Screens.Terms.route)
                        }
                    )
                }
            }

            // Error global (auth / backend)
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

internal class MxPhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = buildString {
            for (i in raw.indices) {
                append(raw[i])
                if (i == 1 && raw.length > 2) append(' ')
                if (i == 5 && raw.length > 6) append(' ')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var o = offset
                if (offset > 2) o += 1   // espacio después de 2 dígitos
                if (offset > 6) o += 1   // espacio después de 6 dígitos
                return o
            }
            override fun transformedToOriginal(offset: Int): Int {
                var o = offset
                if (offset > 2) o -= 1   // quita espacio tras 2 dígitos
                if (offset > 7) o -= 1   // quita espacio tras 6 dígitos (considerando el primer espacio)
                return o.coerceIn(0, 10)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/**
 * Helper que desplaza el campo enfocado dentro de la vista visible cuando aparece el IME.
 * Espera un frame y un pequeño retardo para evitar saltos y luego solicita bringIntoView.
 */
@Composable
internal fun FocusBringIntoView(
    delayMs: Long = 140,
    content: @Composable (Modifier) -> Unit
) {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val mod = Modifier
        .bringIntoViewRequester(requester)
        .onFocusEvent { state ->
            if (state.isFocused) {
                scope.launch {
                    awaitFrame()
                    delay(delayMs)
                    requester.bringIntoView()
                }
            }
        }

    content(mod)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BirthDateField(
    value: String,
    onDateSelected: (LocalDate) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val showDialog = remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    val zone = remember { ZoneId.systemDefault() }
    val minMillis = remember { LocalDate.of(1900, 1, 1).atStartOfDay(zone).toInstant().toEpochMilli() }
    val maxMillis = remember { today.atStartOfDay(zone).toInstant().toEpochMilli() }

    // El state del DatePicker se crea UNA vez y se usa en el diálogo
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value.toMillisFromDisplayOrNull(zone),
        yearRange = 1900..today.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minMillis..maxMillis
            override fun isSelectableYear(year: Int): Boolean = year in 1900..today.year
        }
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { /* readOnly */ },
            readOnly = true,
            singleLine = true,
            // El propio TextField es clickeable para abrir el diálogo
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TextFieldDefaults.MinHeight)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { showDialog.value = true },
            shape = RoundedCornerShape(18.dp),
            // El icono izquierdo también abre el diálogo
            leadingIcon = {
                IconButton(onClick = { showDialog.value = true }) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = "Elegir fecha")
                }
            },
            placeholder = { Text("DD/MM/AAAA", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF2F2F2F)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            colors = textFieldColors(),
            isError = errorMessage != null // activa borde rojo si hay error
        )

        // Mensaje visible justo debajo si hay error
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFD32F2F),
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }

    if (showDialog.value) {
        DatePickerDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                        onDateSelected(localDate) // aquí se valida y se actualiza el error en Register
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

/** Locale ES-MX para formateo. */
internal val localeEsMx: Locale = Locale.Builder().setLanguage("es").setRegion("MX").build()

/** Formato UI dd/MM/yyyy. */
internal val displayFormatterEs: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeEsMx)

/** Formato ISO yyyy-MM-dd. */
internal val storageFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

internal fun String.toMillisFromDisplayOrNull(zone: ZoneId): Long? = runCatching {
    if (this.isBlank()) return null
    val parsed = LocalDate.parse(this, displayFormatterEs)
    parsed.atStartOfDay(zone).toInstant().toEpochMilli()
}.getOrNull()

/** Calcula edad en años desde una fecha ISO (yyyy-MM-dd). Devuelve null si no es válida. */
internal fun computeAgeFromIso(isoDate: String, today: LocalDate = LocalDate.now()): Int? = runCatching {
    if (isoDate.isBlank()) return null
    val birth = LocalDate.parse(isoDate, storageFormatter)
    val p = Period.between(birth, today)
    p.years
}.getOrNull()

/** Mapea errores de Auth a mensajes amigables. */
private fun mapAuthErrorToFriendly(raw: String): String {
    val lower = raw.lowercase()
    return when {
        // Ignorado aquí: lo resuelve el VM (UNCONFIRMED vs CONFIRMED)
        "usernameexistsexception" in lower || "already exists" in lower || ("email" in lower && "exists" in lower) ->
            raw
        "invalidpassword" in lower || ("password" in lower && ("invalid" in lower || "weak" in lower)) ->
            "La contraseña no cumple los requisitos. Prueba con una más fuerte."
        else -> raw
    }
}

@Composable
internal fun Label(text: String, top: Dp = 0.dp) {
    Text(
        text,
        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A)),
        modifier = Modifier.padding(top = top, bottom = 8.dp)
    )
}

@Composable
internal fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedIndicatorColor = Color(0xFFD3D3D3),
    unfocusedIndicatorColor = Color(0xFFD3D3D3),
    cursorColor = Color(0xFF008D96),
    focusedLeadingIconColor = Color(0xFF7D7A7A),
    unfocusedLeadingIconColor = Color(0xFF7D7A7A),
    focusedTrailingIconColor = Color(0xFF7D7A7A),
    unfocusedTrailingIconColor = Color(0xFF7D7A7A),
    focusedPlaceholderColor = Color(0xFF7D7A7A),
    unfocusedPlaceholderColor = Color(0xFF7D7A7A)
)