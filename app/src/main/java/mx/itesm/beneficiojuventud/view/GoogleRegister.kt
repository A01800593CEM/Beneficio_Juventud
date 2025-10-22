package mx.itesm.beneficiojuventud.view

import android.util.Log
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.model.users.UserProfile
import mx.itesm.beneficiojuventud.model.users.AccountState
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModelFactory
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla de registro para usuarios que inician sesión con Google.
 * Los campos de nombre, apellido y correo se autocompletan con datos de Google,
 * pero el usuario puede modificarlos.
 */
@Composable
fun GoogleRegister(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current))
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Obtener datos de Google (NO usar remember, leer directamente)
    val googleData = authViewModel.pendingGoogleUserData

    // Log inmediato para debugging
    Log.d("GoogleRegister", "🔵 GoogleRegister iniciado")
    Log.d("GoogleRegister", "🔍 AuthViewModel instance: ${authViewModel.hashCode()}")
    Log.d("GoogleRegister", "📦 pendingGoogleUserData: ${if (googleData == null) "NULL ❌" else "OK ✅"}")
    if (googleData != null) {
        Log.d("GoogleRegister", "  → Email: ${googleData.email}")
        Log.d("GoogleRegister", "  → Name: ${googleData.name}")
        Log.d("GoogleRegister", "  → GivenName: ${googleData.givenName}")
        Log.d("GoogleRegister", "  → FamilyName: ${googleData.familyName}")
    }

    // Estado para almacenar el cognitoSub
    var cognitoSub by remember { mutableStateOf<String?>(null) }

    // Estados del formulario
    var nombre by rememberSaveable { mutableStateOf("") }
    var apPaterno by rememberSaveable { mutableStateOf("") }
    var apMaterno by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    // Obtener el cognitoSub del usuario actual
    LaunchedEffect(Unit) {
        scope.launch {
            Log.d("GoogleRegister", "🔵 Obteniendo cognitoSub del usuario actual")
            authViewModel.getCurrentUser()
            delay(200) // Espera para que se actualice
            cognitoSub = authViewModel.currentUserId.value
            Log.d("GoogleRegister", "✅ CognitoSub obtenido: $cognitoSub")
        }
    }

    // Autocompletar campos cuando lleguen los datos de Google
    LaunchedEffect(googleData) {
        Log.d("GoogleRegister", "🔵 LaunchedEffect ejecutado - googleData: ${if (googleData == null) "NULL" else "NOT NULL"}")

        if (googleData != null) {
            Log.d("GoogleRegister", "📧 Google Data received:")
            Log.d("GoogleRegister", "  Email: ${googleData.email}")
            Log.d("GoogleRegister", "  Given Name: ${googleData.givenName}")
            Log.d("GoogleRegister", "  Family Name: ${googleData.familyName}")
            Log.d("GoogleRegister", "  Full Name: ${googleData.name}")

            // Autocompletar email
            if (email.isBlank() && googleData.email.isNotBlank()) {
                email = googleData.email
                Log.d("GoogleRegister", "✅ Email autocompletado: $email")
            }

            // Autocompletar nombre y apellidos
            if (nombre.isBlank()) {
                nombre = googleData.givenName ?: googleData.name?.split(" ")?.firstOrNull() ?: ""
                Log.d("GoogleRegister", "✅ Nombre autocompletado: $nombre")
            }

            if (apPaterno.isBlank()) {
                apPaterno = googleData.familyName ?: googleData.name?.split(" ")?.getOrNull(1) ?: ""
                Log.d("GoogleRegister", "✅ Apellido Paterno autocompletado: $apPaterno")
            }

            // Apellido materno solo si hay más de 2 palabras en el nombre
            if (apMaterno.isBlank() && googleData.name != null) {
                val parts = googleData.name.split(" ").filter { it.isNotBlank() }
                if (parts.size > 2) {
                    apMaterno = parts.drop(2).joinToString(" ")
                    Log.d("GoogleRegister", "✅ Apellido Materno autocompletado: $apMaterno")
                }
            }
        }
    }

    // Fecha de nacimiento: display (UI) + db (ISO)
    var fechaNacDisplay by rememberSaveable { mutableStateOf("") }
    var fechaNacDb by rememberSaveable { mutableStateOf("") }
    var birthDateErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var phone by rememberSaveable { mutableStateOf("") }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    // Edad válida si no hay error y hay fecha
    val isAgeValid = birthDateErrorMessage == null && fechaNacDb.isNotBlank()

    var isCreatingUser by rememberSaveable { mutableStateOf(false) }

    // Validación (apellido materno es OPCIONAL)
    val isFormValid = nombre.isNotBlank() &&
            apPaterno.isNotBlank() &&
            // apMaterno es OPCIONAL, no se requiere
            fechaNacDb.isNotBlank() &&
            email.isNotBlank() &&
            phone.isNotBlank() &&
            acceptTerms &&
            isAgeValid

    // Log de validación para debugging
    LaunchedEffect(nombre, apPaterno, apMaterno, fechaNacDb, email, phone, acceptTerms, isAgeValid, cognitoSub) {
        Log.d("GoogleRegister", "🔍 Validación del formulario:")
        Log.d("GoogleRegister", "  Nombre: ${nombre.isNotBlank()} ($nombre)")
        Log.d("GoogleRegister", "  ApPaterno: ${apPaterno.isNotBlank()} ($apPaterno)")
        Log.d("GoogleRegister", "  ApMaterno: ($apMaterno)")
        Log.d("GoogleRegister", "  FechaNac: ${fechaNacDb.isNotBlank()} ($fechaNacDb)")
        Log.d("GoogleRegister", "  Email: ${email.isNotBlank()} ($email)")
        Log.d("GoogleRegister", "  Phone: ${phone.isNotBlank()} ($phone)")
        Log.d("GoogleRegister", "  Terms: $acceptTerms")
        Log.d("GoogleRegister", "  AgeValid: $isAgeValid (error: $birthDateErrorMessage)")
        Log.d("GoogleRegister", "  CognitoSub: ${cognitoSub != null} ($cognitoSub)")
        Log.d("GoogleRegister", "  ✅ isFormValid: $isFormValid")
    }

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
                        isCreatingUser -> "Creando perfil..."
                        cognitoSub == null -> "Cargando..."
                        else -> "Continuar"
                    },
                    enabled = !isCreatingUser && isFormValid && cognitoSub != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isAgeValid) {
                        showError = true
                        errorMessage = birthDateErrorMessage ?: "Para registrarte debes tener entre 12 y 29 años."
                        return@MainButton
                    }

                    if (cognitoSub.isNullOrBlank()) {
                        Log.e("GoogleRegister", "❌ CognitoSub is null or blank!")
                        Log.e("GoogleRegister", "CurrentUserId: ${authViewModel.currentUserId.value}")
                        showError = true
                        errorMessage = "Espera un momento mientras cargamos tu información..."

                        // Intentar obtener el cognitoSub nuevamente
                        scope.launch {
                            delay(500)
                            authViewModel.getCurrentUser()
                            delay(200)
                            val newSub = authViewModel.currentUserId.value
                            if (!newSub.isNullOrBlank()) {
                                cognitoSub = newSub
                                showError = false
                                Log.d("GoogleRegister", "✅ CognitoSub recuperado: $newSub")
                            } else {
                                errorMessage = "No se pudo obtener tu información. Por favor, intenta cerrar sesión e iniciar de nuevo."
                            }
                        }
                        return@MainButton
                    }

                    // Crear perfil de usuario en la BD
                    scope.launch {
                        showError = false
                        isCreatingUser = true
                        try {
                            Log.d("GoogleRegister", "🔄 Creando usuario en BD...")
                            Log.d("GoogleRegister", "  CognitoId: $cognitoSub")
                            Log.d("GoogleRegister", "  Email: ${email.trim()}")
                            Log.d("GoogleRegister", "  Nombre: ${nombre.trim()} ${apPaterno.trim()} ${apMaterno.trim()}")

                            val userProfile = UserProfile(
                                cognitoId = cognitoSub!!,
                                name = nombre.trim(),
                                lastNamePaternal = apPaterno.trim(),
                                lastNameMaternal = apMaterno.trim().takeIf { it.isNotBlank() } ?: "",
                                birthDate = fechaNacDb,
                                phoneNumber = phone.trim(),
                                email = email.trim(),
                                accountState = AccountState.activo
                            )

                            // Crear usuario en BD - MISMO FLUJO QUE Register.kt
                            userViewModel.createUser(userProfile)

                            // ESPERAR a que createUser() complete (máximo 10 segundos)
                            Log.d("GoogleRegister", "⏳ Esperando a que createUser complete...")
                            var attempts = 0
                            while (userViewModel.isLoading.value && attempts < 100) {
                                delay(100)
                                attempts++
                            }

                            // Verificar si hubo error en la creación
                            val creationError = userViewModel.error.value
                            if (!creationError.isNullOrBlank()) {
                                Log.e("GoogleRegister", "❌ Error en createUser: $creationError")
                                throw Exception(creationError)
                            }

                            // Verificar que el usuario se creó correctamente
                            val createdUser = userViewModel.userState.value
                            if (createdUser.cognitoId.isNullOrBlank()) {
                                Log.e("GoogleRegister", "❌ Usuario no se creó correctamente")
                                throw Exception("No se pudo crear el usuario en el servidor")
                            }

                            authViewModel.clearPendingGoogleUserData()
                            Log.d("GoogleRegister", "✅ Usuario creado exitosamente en servidor")
                            Log.d("GoogleRegister", "  → UserId: ${createdUser.cognitoId}")

                            // Actualizar currentUserId sin cambiar sessionKey
                            authViewModel.getCurrentUser()
                            delay(300)

                            Log.d("GoogleRegister", "✅ CurrentUserId actualizado: ${authViewModel.currentUserId.value}")

                            // Navegar directo a onboarding (igual que Register.kt)
                            nav.navigate(Screens.Onboarding.route) {
                                popUpTo(Screens.LoginRegister.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Log.e("GoogleRegister", "❌ Error creando usuario: ${e.message}", e)
                            showError = true
                            errorMessage = e.message ?: "No se pudo crear el perfil en la BD."
                        } finally {
                            isCreatingUser = false
                        }
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
                    TextButton(onClick = {
                        authViewModel.signOut()
                        nav.navigate(Screens.Login.route) {
                            popUpTo(Screens.LoginRegister.route) { inclusive = true }
                        }
                    }) {
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
                    "Completa tu Perfil",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )
            }

            item {
                Text(
                    "Iniciaste sesión con Google. Por favor, completa tu información personal.",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161)),
                    modifier = Modifier.padding(bottom = 8.dp)
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

            // Correo (autocompletado de Google pero editable)
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

            // Error global
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

// Usar las mismas utilidades que Register.kt para evitar duplicación
// Las clases y funciones están definidas en Register.kt
