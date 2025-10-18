package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.components.PasswordTextField
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.collaborators.CollaboratorsState
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.view.Screens
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import java.time.Instant

@Composable
fun RegisterCollab(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    // Campos del formulario
    var businessName by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var representativeName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    var didCreateDirect by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(authState.needsConfirmation) {
        if (authState.needsConfirmation) {
            nav.navigate(Screens.confirmSignUpWithEmail(email)) {
                popUpTo(Screens.LoginRegister.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(authState.isSuccess, authState.needsConfirmation) {
        if (didCreateDirect) return@LaunchedEffect
        if (authState.isSuccess && !authState.needsConfirmation) {
            val pending = authViewModel.consumePendingCollabProfile()
            val sub = authState.cognitoSub

            if (pending == null || sub.isNullOrBlank()) {
                return@LaunchedEffect
            }

            didCreateDirect = true

            authViewModel.signIn(email.trim(), password)

            // Crea el Colaborador en la BD
            scope.launch {
                try {
                    collabViewModel.createCollaborator(
                        pending.copy(
                            cognitoId = sub, // Vincula el cognitoId
                            email = email.trim(),
                            state = CollaboratorsState.activo, // Estado inicial
                            registrationDate = Instant.now().toString()
                        )
                    )

                    authViewModel.clearPendingCredentials()

                    // Navega a la Home del Colaborador
                    nav.navigate(Screens.HomeCollab.route) {
                        popUpTo(Screens.LoginRegister.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    didCreateDirect = false
                    showError = true
                    errorMessage = e.message ?: "No se pudo crear el perfil del colaborador en la BD."
                    authViewModel.signOut() // Deshace el auto-login si falla la creación en BD
                }
            }
        }
    }

    // Manejo de errores de registro (ej. email ya existe)
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            errorMessage = error
            showError = true
        }
    }

    // Validación del formulario
    val isFormValid = businessName.isNotBlank() &&
            rfc.isNotBlank() &&
            representativeName.isNotBlank() &&
            phone.length == 10 &&
            email.isNotBlank() &&
            address.isNotBlank() &&
            postalCode.isNotBlank() &&
            description.isNotBlank() &&
            password.isNotBlank() &&
            acceptTerms

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
                    text = if (authState.isLoading) "Registrando..." else "Continuar",
                    enabled = !authState.isLoading && isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    showError = false
                    val collabProfile = Collaborator(
                        businessName = businessName.trim(),
                        rfc = rfc.trim(),
                        representativeName = representativeName.trim(),
                        phone = phone,
                        email = email.trim(),
                        address = address.trim(),
                        postalCode = postalCode.trim(),
                        description = description.trim(),
                    )

                    authViewModel.savePendingCollabProfile(collabProfile)
                    authViewModel.setPendingCredentials(email.trim(), password)
                    authViewModel.signUp(email.trim(), password)
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
                    "Registro de Colaborador",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )
            }

            // Nombre del Negocio
            item { Label("Nombre del Negocio", top = 4.dp) }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        singleLine = true,
                        modifier = it.fillMaxWidth().heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) },
                        placeholder = { Text("Ej. La Bella Italia", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // RFC
            item { Label("RFC") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = rfc,
                        onValueChange = { rfc = it.uppercase().take(13) },
                        singleLine = true,
                        modifier = it.fillMaxWidth().heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        placeholder = { Text("RFC (12 o 13 caracteres)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Nombre del Representante
            item { Label("Nombre del Representante") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = representativeName,
                        onValueChange = { representativeName = it },
                        singleLine = true,
                        modifier = it.fillMaxWidth().heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        placeholder = { Text("Nombre Completo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Teléfono (Negocio)
            item { Label("Teléfono del Negocio") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }.take(10)
                            phone = digits
                        },
                        singleLine = true,
                        modifier = it.fillMaxWidth().heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                        placeholder = { Text("55 5555 5555", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors(),
                        visualTransformation = MxPhoneVisualTransformation()
                    )
                }
            }

            // Dirección
            item { Label("Dirección del Negocio") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        singleLine = true,
                        modifier = it.fillMaxWidth().heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                        placeholder = { Text("Calle, Número, Colonia, Ciudad", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Código Postal
            item { Label("Código Postal") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it.filter { it.isDigit() }.take(5) },
                        singleLine = true,
                        modifier = it.fillMaxWidth().heightIn(min = TextFieldDefaults.MinHeight),
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.Numbers, contentDescription = null) },
                        placeholder = { Text("Ej. 01234", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors()
                    )
                }
            }

            // Descripción
            item { Label("Descripción del Negocio") }
            item {
                FocusBringIntoView {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = it.fillMaxWidth().height(120.dp), // Campo más alto
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                        placeholder = { Text("Breve descripción de tu negocio...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors()
                    )
                }
            }

            // Correo (para la cuenta)
            item { Label("Correo Electrónico (para tu cuenta)") }
            item {
                FocusBringIntoView {
                    EmailTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = it.fillMaxWidth()
                    )
                }
            }

            // Contraseña (para la cuenta)
            item { Label("Contraseña (para tu cuenta)") }
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


// Esto es de Register

private class MxPhoneVisualTransformation : VisualTransformation {
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
                if (offset > 2) o += 1
                if (offset > 6) o += 1
                return o
            }
            override fun transformedToOriginal(offset: Int): Int {
                var o = offset
                if (offset > 2) o -= 1
                if (offset > 7) o -= 1
                return o.coerceIn(0, 10)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
private fun FocusBringIntoView(
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