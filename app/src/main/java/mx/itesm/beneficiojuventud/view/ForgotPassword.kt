package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPassword(nav: NavHostController, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    val emailValid = remember(email) {
        Regex("^[A-Za-z0-9][A-Za-z0-9+_.-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$").matches(email)
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

        BackButton(nav)

        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .dismissKeyboardOnTap()
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
                    painter = painterResource(id = R.drawable.forgot_password),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxHeight()
                )
            }
            Column(
                modifier = modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "¿Olvidaste tu Contraseña?",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
                Text(
                    "No te preocupes. Suele pasar. \nPor favor, introduzca la dirección de correo electrónico asociada a su cuenta.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = modifier.padding(horizontal = 24.dp)
                )
            }
            Column(modifier = modifier
                .fillMaxWidth(0.95f)
                .padding(top = 10.dp)) {
                Text(
                    "Correo Electrónico",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    modifier = modifier.padding(24.dp, 30.dp, 24.dp, 8.dp)
                )

                // Campo correo (tu componente)
                EmailTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                )
                MainButton("Obtener Código",
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth(0.95f)
                        .align(Alignment.CenterHorizontally),
                    enabled = emailValid
                ) {
                    nav.navigate(Screens.RecoveryCode.route)
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordPreview() {
    BeneficioJuventudTheme {
        ForgotPassword(nav = NavHostController(LocalContext.current))
    }
}