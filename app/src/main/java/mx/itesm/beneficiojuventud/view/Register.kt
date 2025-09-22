package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.AltLoginButton
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.components.MainButton

@Composable
fun Register(modifier: Modifier = Modifier) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(top = 85.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_beneficio_joven),
                contentDescription = "",
                modifier = Modifier.size(80.dp)
            )
            Column(
                modifier = modifier.fillMaxWidth(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Empieza Ahora",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                        ),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = modifier.padding(20.dp)
                )
                Text(
                    "Crea una cuenta o inicia sesión para explorar nuestra app",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = modifier.padding(horizontal = 24.dp)
                )
            }
            Column(modifier = modifier.fillMaxWidth(0.94f)) {
                MainButton("Inicia Sesión", modifier = Modifier.padding(top = 50.dp)) { /* TODO implementar */ }
                MainButton("Regístrate", modifier = Modifier.padding(top = 20.dp)) { /* TODO implementar */ }
                GradientDivider(modifier = modifier.padding(vertical = 50.dp))
                AltLoginButton(
                    "Continuar con Google",
                    painterResource(id = R.drawable.logo_google),
                    "Continuar con Google",
                    onClick = { /* TODO: login con Google */ }
                )
                AltLoginButton(
                    "Continuar con Facebook",
                    painterResource(id = R.drawable.logo_facebook),
                    "Continuar con Facebook",
                    onClick = { /* TODO: login con Facebook */ },
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(
                    "¿Quieres ser colaborador?",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    )
                )
                TextButton(onClick = { /* TODO implementar */ }) {
                    Text(
                        "Ver más",
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