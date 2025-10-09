package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.AltLoginButton
import mx.itesm.beneficiojuventud.components.GradientDivider_OR
import mx.itesm.beneficiojuventud.components.MainButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

@Composable
fun LoginRegister(nav: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars // respeta status/nav bars
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(top = 85.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_beneficio_joven),
                contentDescription = "",
                modifier = Modifier.size(80.dp)
            )

            // Títulos
            Column(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Empieza Ahora",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(20.dp)
                )
                Text(
                    "Crea una cuenta o inicia sesión para explorar nuestra app",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Deja que el bloque superior use espacio, pero si no cabe, el scroll funciona:
            Spacer(Modifier.height(24.dp))

            // Botones principales
            Column(modifier = Modifier.fillMaxWidth(0.94f)) {
                MainButton("Inicia Sesión", modifier = Modifier.padding(top = 26.dp)) {
                    nav.navigate(Screens.Login.route)
                }
                MainButton("Regístrate", modifier = Modifier.padding(top = 16.dp)) {
                    nav.navigate(Screens.Register.route)
                }

                GradientDivider_OR(modifier = Modifier.padding(vertical = 32.dp))

                // Botones de redes (se mantienen dentro del scroll)
                AltLoginButton(
                    "Continuar con Google",
                    painterResource(id = R.drawable.logo_google),
                    "Continuar con Google",
                    onClick = { /* TODO login Google */ }
                )
                AltLoginButton(
                    "Continuar con Facebook",
                    painterResource(id = R.drawable.logo_facebook),
                    "Continuar con Facebook",
                    onClick = { /* TODO login Facebook */ },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Empuja el bloque inferior si hay espacio; si no, el usuario puede hacer scroll
            Spacer(Modifier.weight(1f))

            // Footer
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp) // margen final extra
            ) {
                Text(
                    "¿Quieres ser colaborador?",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A))
                )
                TextButton(onClick = { /* nav.navigate("colabora") */ }) {
                    Text(
                        "Ver más",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF008D96))
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginRegisterPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        LoginRegister(nav = nav)
    }
}