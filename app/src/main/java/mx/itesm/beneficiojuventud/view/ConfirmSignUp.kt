package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.components.CodeTextField
import mx.itesm.beneficiojuventud.model.collaborators.CollaboratorsState
import mx.itesm.beneficiojuventud.model.users.AccountState
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import java.time.Instant

/**
 * Confirmación de registro: valida código OTP, detecta si es usuario o colaborador,
 * crea el perfil correspondiente en BD usando el perfil pendiente y sub de Cognito.
 * Maneja también el caso de usuarios ya confirmados (status CONFIRMED) haciendo sign-in directo.
 * Navega a Onboarding (usuarios) o HomeScreenCollab (colaboradores) según el tipo de perfil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmSignUp(
    nav: NavHostController,
    email: String,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    var code by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val authState by authViewModel.authState.collectAsState()
    val currentSub by authViewModel.currentUserId.collectAsState()

    // Parche 1: one-shot latch para evitar doble creación
    var didCreate by rememberSaveable { mutableStateOf(false) }

    // ----- Temporizador Reenviar -----
    var seconds by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    LaunchedEffect(seconds) {
        if (!canResend && seconds > 0) {
            delay(1000)
            seconds--
        } else if (seconds == 0) {
            canResend = true
        }
    }

    fun handleResend() {
        if (!canResend) return
        authViewModel.resendSignUpCode(email)
        canResend = false
        seconds = 60
        code = ""
        showError = false
        errorMessage = ""
    }

    // Cuando confirmación sea exitosa O sign-in exitoso (caso usuario ya confirmado),
    // crea en BD con el perfil pendiente y navega a Home
    LaunchedEffect(authState.isSuccess, authState.error, authState.isLoading, authState.cognitoSub, currentSub) {
        if (didCreate) return@LaunchedEffect

        // No procesar errores aquí; el otro LaunchedEffect se encarga
        if (authState.error != null) {
            return@LaunchedEffect
        }

        if (!authState.isLoading && authState.isSuccess) {
            // Detectar si es usuario o colaborador
            val pendingUser = authViewModel.getPendingUserProfile()
            val pendingCollab = authViewModel.getPendingCollabProfile()
            val sub = currentSub ?: authState.cognitoSub

            if (pendingUser == null && pendingCollab == null) {
                // Si esto se dispara por segunda vez, evitamos ruido
                return@LaunchedEffect
            }
            if (sub.isNullOrBlank()) {
                showError = true
                errorMessage = "No se pudo obtener el ID de Cognito."
                return@LaunchedEffect
            }

            didCreate = true

            scope.launch {
                try {
                    // Crear usuario o colaborador según lo que esté pendiente
                    if (pendingUser != null) {
                        userViewModel.createUser(
                            pendingUser.copy(
                                cognitoId = sub,
                                email = email.trim(),
                                accountState = AccountState.activo
                            )
                        )
                        authViewModel.consumePendingUserProfile()
                        authViewModel.clearPendingCredentials()

                        // Navega a Onboarding para usuarios
                        nav.navigate(Screens.Onboarding.route) {
                            popUpTo(Screens.LoginRegister.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else if (pendingCollab != null) {
                        collabViewModel.createCollaborator(
                            pendingCollab.copy(
                                cognitoId = sub,
                                email = email.trim(),
                                state = CollaboratorsState.activo,
                                registrationDate = Instant.now().toString()
                            )
                        )
                        authViewModel.consumePendingCollabProfile()
                        authViewModel.clearPendingCredentials()

                        // Navega a HomeScreenCollab para colaboradores
                        nav.navigate(Screens.HomeScreenCollab.route) {
                            popUpTo(Screens.LoginRegister.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    authViewModel.clearState()

                } catch (e: Exception) {
                    didCreate = false // permitir reintento si quieres
                    showError = true
                    errorMessage = e.message ?: "No se pudo crear el perfil en la BD."
                }
            }
        }
    }

    // Mostrar error del estado (si llega un error externo)
    // CASO ESPECIAL: Si el usuario ya está confirmado, hacer sign-in directo
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            val lower = error.lowercase()

            // Detectar si el usuario ya está confirmado
            if ("current status is confirmed" in lower ||
                ("user cannot be confirmed" in lower && "confirmed" in lower)) {

                // El usuario ya está confirmado en Cognito, hacer sign-in directo
                val (savedEmail, savedPassword) = authViewModel.getPendingCredentials()
                if (savedEmail != null && savedPassword != null) {
                    authViewModel.signIn(savedEmail, savedPassword)
                    // El LaunchedEffect de éxito se encargará de crear el usuario en BD
                    return@LaunchedEffect
                }
            }

            errorMessage = error
            showError = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(nav = nav)
                }
            )

        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 85.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_beneficio_joven),
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
            }

            // Título
            Text(
                "Confirma tu cuenta",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                    ),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Descripción
            Text(
                "Ingresa el código de 6 dígitos que enviamos a:",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7D7A7A)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Email
            Text(
                email,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF008D96)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo de código
            CodeTextField(
                value = code,
                onValueChange = {
                    code = it
                    showError = false
                },
                length = 6,
                isError = showError,
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                onFilled = { filled ->
                    if (!authState.isLoading) {
                        showError = false
                        authViewModel.confirmSignUp(email, filled)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // Error
            if (showError && errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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

            // Botón confirmar (manual)
            MainButton(
                text = if (authState.isLoading) "Confirmando..." else "Confirmar",
                enabled = !authState.isLoading && code.length == 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                showError = false
                authViewModel.confirmSignUp(email, code)
            }

            Spacer(Modifier.height(12.dp))

            // ----- Reenviar -----
            Text(
                text = if (canResend) "Reenviar código" else "Reenviar código  (00:${
                    seconds.toString().padStart(2, '0')
                })",
                color = if (canResend) Color(0xFF008D96) else Color(0xFF7D7A7A),
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 8.dp)
                    .noRippleClickable { handleResend() }
            )
        }
    }
}

/** Captura taps sin ripple, útil para "Reenviar código". */
// ConfirmSignUp.kt
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = { onClick() })
    })

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfirmSignUpPreview() {
    BeneficioJuventudTheme {
        ConfirmSignUp(
            nav = NavHostController(LocalContext.current),
            email = "usuario@correo.com"
        )
    }
}
