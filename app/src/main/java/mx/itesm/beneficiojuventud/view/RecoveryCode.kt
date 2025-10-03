package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryCode(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    emailArg: String = "beneficio_user@juventud.com",
    onVerify: (code: String) -> Unit = { nav.navigate(Screens.NewPassword.route) },
    onResend: () -> Unit = {}
) {
    var email by remember { mutableStateOf(emailArg) }

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

    fun handleResend() {
        if (canResend) {
            onResend()
            canResend = false
            seconds = 60
            code = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = { BackButton(nav = nav) })
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(top = 85.dp),
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
            OtpCodeInput(
                code = code,
                length = length,
                onCodeChange = { code = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

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
                    enabled = code.length == length
                ) { onVerify(code) }
            }

            // ----- Reenviar -----
            Text(
                text = if (canResend) "Reenviar código" else "Reenviar código  (00:${
                    seconds.toString().padStart(2, '0')
                })",
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

@Composable
private fun OtpCodeInput(
    code: String,
    length: Int,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tf by remember { mutableStateOf(TextFieldValue(code)) }

    // Mantiene cursor siempre al final
    LaunchedEffect(code) {
        if (tf.text != code) {
            tf = tf.copy(text = code, selection = TextRange(code.length))
        }
    }

    BasicTextField(
        value = tf,
        onValueChange = { new ->
            val filtered = new.text.filter { it.isDigit() }.take(length)
            tf = new.copy(text = filtered, selection = TextRange(filtered.length))
            onCodeChange(filtered)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        modifier = modifier,
        decorationBox = { inner ->
            // Oculta el campo original pero mantiene el foco
            Box(Modifier.size(0.dp)) { inner() }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(length) { i ->
                    val char = code.getOrNull(i)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .border(
                                width = 2.dp,
                                color = if (char.isEmpty()) Color(0xFFE3E3E3) else Color(0xFF008D96),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = { onClick() })
    })

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RecoveryCodePreview() {
    BeneficioJuventudTheme {
        RecoveryCode(nav = NavHostController(LocalContext.current))
    }
}
