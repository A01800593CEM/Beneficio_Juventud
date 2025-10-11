package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.components.CodeTextField
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel

/**
 * Pantalla para confirmar el registro ingresando un código de 6 dígitos.
 * Maneja temporizador para reenviar código, errores del estado y navegación tras éxito.
 * @param nav Controlador de navegación para volver o continuar al flujo de login.
 * @param email Correo al que se envió el código de confirmación.
 * @param modifier Modificador opcional del contenedor.
 * @param authViewModel ViewModel de autenticación utilizado para confirmar y reenviar código.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmSignUp(
    nav: NavHostController,
    email: String,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
) {
    var code by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

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

    // Navegar al onboarding cuando se confirme con éxito
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            authViewModel.clearState()
            nav.navigate(Screens.Login.route) {
                popUpTo(Screens.LoginRegister.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Mostrar error del estado
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            errorMessage = error
            showError = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = { BackButton(nav = nav) },
                modifier = Modifier.padding(10.dp, 16.dp ,0.dp, 0.dp) )
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

            // Campo de código (CodeTextField con casillas)
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

            // Mostrar error si existe
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

            // ----- Reenviar (mismo estilo que RecoveryCode) -----
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

/**
 * Extensión de Modifier para capturar taps sin efecto ripple.
 * Útil para textos de acción como "Reenviar código".
 * @param onClick Acción a ejecutar al tocar el elemento.
 * @return Modifier con detector de tap aplicado.
 */
@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = { onClick() })
    })

/**
 * Vista previa de la pantalla de confirmación de registro.
 */
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
