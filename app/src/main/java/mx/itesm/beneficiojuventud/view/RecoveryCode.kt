package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
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

/**
 * Pantalla para introducir y validar el código de recuperación de contraseña (OTP de 6 dígitos).
 * Muestra un temporizador para reenviar el código y navega al flujo de creación de nueva contraseña al verificar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryCode(
    /**
     * Controlador de navegación para retroceder y continuar al siguiente paso del flujo.
     */
    nav: NavHostController,
    /**
     * Modificador externo para composición y pruebas.
     */
    modifier: Modifier = Modifier,
    /**
     * Correo electrónico al cual se envió el código de verificación.
     */
    emailArg: String = "beneficio_user@juventud.com",
    /**
     * ViewModel de autenticación que gestiona el estado y acciones de recuperación.
     */
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf(emailArg) }
    val authState by viewModel.authState.collectAsState()

    // ----- OTP state (6 casillas) -----
    val length = 6
    var code by remember { mutableStateOf("") }

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

    // Dispara el reenvío del código y reinicia el temporizador.
    fun handleResend() {
        if (canResend) {
            viewModel.resetPassword(email)
            canResend = false
            seconds = 60
            code = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(
                        nav = nav,
                        modifier = Modifier.padding(10.dp, 16.dp, 0.dp, 0.dp)
                    )
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .dismissKeyboardOnTap()
                .padding(top = 85.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.recovery_code),
                    contentDescription = "",
                    modifier = Modifier.fillMaxHeight()
                )
            }

            Column(
                modifier = modifier.fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Ingresa el Código",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Un código de 6 dígitos ha sido enviado a",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    email,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = modifier.padding(top = 6.dp, bottom = 4.dp)
                )
            }

            // ----- Fila de OTP -----
            Row(modifier = modifier.fillMaxWidth(0.9f)) {
                CodeTextField(
                    value = code,
                    onValueChange = { code = it },
                    length = 6,
                    isError = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    onFilled = { filled ->
                        // Opcional: navegación automática al completar el código.
                        // nav.navigate(Screens.newPasswordWithEmailAndCode(email, filled))
                    }
                )
            }

            // ----- Botón Verificar -----
            Column(
                modifier = modifier
                    .fillMaxWidth(0.95f)
                    .padding(top = 18.dp)
            ) {
                MainButton(
                    text = "Verificar",
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(0.95f)
                        .align(Alignment.CenterHorizontally),
                    enabled = code.length == length && !authState.isLoading
                ) {
                    nav.navigate(Screens.newPasswordWithEmailAndCode(email, code))
                }
            }

            // ----- Reenviar -----
            Text(
                text = if (canResend) "Reenviar código" else "Reenviar código  (00:${seconds.toString().padStart(2, '0')})",
                color = if (canResend) Color(0xFF008D96) else Color(0xFF7D7A7A),
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .padding(bottom = 8.dp)
                    .noRippleClickable { handleResend() }
            )
        }
    }
}

/**
 * Extensión de Modifier para clicks sin efecto de ripple, útil en textos de acción secundarios.
 * @param onClick Acción a ejecutar al detectar un toque.
 * @return El mismo Modifier con soporte de gesto tap sin ripple.
 */
@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = { onClick() })
    })

/**
 * Vista previa de la pantalla RecoveryCode con tema y sistema UI visibles.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecoveryCodePreview() {
    BeneficioJuventudTheme {
        RecoveryCode(nav = NavHostController(LocalContext.current))
    }
}
