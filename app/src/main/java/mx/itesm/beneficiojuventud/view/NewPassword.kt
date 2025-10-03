package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.BackButton
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.components.PasswordTextField
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPassword(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    imageRes: Int = R.drawable.new_password,
    onConfirm: (newPassword: String) -> Unit = { nav.navigate(Screens.Login.route) },
    minLength: Int = 8,
    requireLetter: Boolean = true,
    requireDigit: Boolean = true
) {
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val passValid by remember(pass) {
        mutableStateOf(isPasswordValid(pass, minLength, requireLetter, requireDigit))
    }
    val matches by remember(pass, confirm) { mutableStateOf(pass.isNotEmpty() && pass == confirm) }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = { BackButton(nav = nav) })
        }
    ) { inner ->
        Column(
            modifier = modifier
                .padding(inner)
                .fillMaxWidth()
                .padding(top = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxHeight()
                )
            }

            // Títulos
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Ingresa tu nueva contraseña",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    "¡Guárdala mejor esta vez!",
                    color = Color(0xFF7D7A7A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Formulario
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(top = 24.dp)
            ) {
                Text(
                    "Nueva Contraseña",
                    color = Color(0xFF7D7A7A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                )
                PasswordTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                if (pass.isNotEmpty() && !passValid) {
                    Text(
                        "Mínimo $minLength caracteres, al menos ${if (requireLetter) "1 letra" else ""}${if (requireLetter && requireDigit) " y " else ""}${if (requireDigit) "1 número" else ""}.",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 24.dp, top = 6.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Confirmar Contraseña",
                    color = Color(0xFF7D7A7A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                )
                PasswordTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                if (confirm.isNotEmpty() && !matches) {
                    Text(
                        "Las contraseñas no coinciden.",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 24.dp, top = 6.dp)
                    )
                }

                // Botón Confirmar
                MainButton(
                    text = "Confirmar",
                    enabled = passValid && matches,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(0.95f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    onConfirm(pass)
                }
            }
        }
    }
}

private fun isPasswordValid(
    pass: String,
    minLen: Int,
    requireLetter: Boolean,
    requireDigit: Boolean
): Boolean {
    if (pass.length < minLen) return false
    if (requireLetter && pass.none { it.isLetter() }) return false
    if (requireDigit && pass.none { it.isDigit() }) return false
    return true
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewPasswordPreview() {
    BeneficioJuventudTheme {
        NewPassword(nav = NavHostController(LocalContext.current))
    }
}