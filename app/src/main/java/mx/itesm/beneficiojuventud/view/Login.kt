package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.components.AltLoginButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.PasswordTextField

@Composable
fun Login(nav: NavHostController, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(top = 85.dp)
        ) {
            // Logo arriba a la izquierda
            Box(modifier = modifier.padding(horizontal = 30.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_beneficio_joven),
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
            }

            // Título
            Text(
                "Inicia Sesión",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                    ),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = modifier.padding(24.dp, 18.dp, 20.dp, 14.dp)
            )

            // Subtítulo
            Text(
                "Por favor, inicie sesión en su cuenta",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161)
                ),
                textAlign = TextAlign.Start,
                modifier = modifier.padding(horizontal = 24.dp)
            )

            // Label correo
            Text(
                "Correo Electrónico",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7D7A7A)
                ),
                modifier = modifier.padding(24.dp, 24.dp, 24.dp, 8.dp)
            )

            // Campo correo (tu componente)
            EmailTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            )

            // Label contraseña
            Text(
                "Contraseña",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7D7A7A)
                ),
                modifier = modifier.padding(24.dp, 20.dp, 24.dp, 8.dp)
            )


            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // Recuérdame / ¿Olvidaste tu contraseña?
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(
                        "Recuérdame",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7D7A7A)
                        )
                    )
                }
                TextButton(onClick = { nav.navigate(Screens.ForgotPassword.route) }) {
                    Text(
                        "¿Olvidaste tu contraseña?",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7D7A7A)
                        )
                    )
                }
            }

            // Botón primario (gradiente) — usa tu MainButton
            MainButton(
                text = "Inicia Sesión",
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // TODO: login con email/password
            }

            GradientDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Social buttons
            AltLoginButton(
                text = "Continuar con Google",
                icon = painterResource(id = R.drawable.logo_google),
                contentDescription = "Continuar con Google",
                onClick = { /* TODO: login Google */ },
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            AltLoginButton(
                text = "Continuar con Facebook",
                icon = painterResource(id = R.drawable.logo_facebook),
                contentDescription = "Continuar con Facebook",
                onClick = { /* TODO: login Facebook */ },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            )

            // ¿No tienes cuenta? Regístrate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿No tienes cuenta?  ",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    )
                )
                TextButton(onClick = { nav.navigate(Screens.Register.route) }) {
                    Text(
                        "Regístrate",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF008D96)
                        )
                    )
                }
            }
        }
    }
}


