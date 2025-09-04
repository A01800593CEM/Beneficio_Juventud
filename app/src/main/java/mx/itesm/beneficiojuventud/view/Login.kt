package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.EmailTextField
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

@Composable
fun Login(nav: NavHostController, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(top = 85.dp),
        ) {
            Box(modifier = modifier.padding(horizontal = 30.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_beneficio_joven),
                    contentDescription = "",
                    modifier = Modifier
                        .size(50.dp)
                )
            }
            Text(
                "Inicia Sesión",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF4B4C7E),
                            Color(0xFF008D96)
                        )
                    ),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = modifier.padding(24.dp, 18.dp, 20.dp, 14.dp)
            )
            Text(
                "Por favor, inicie sesión en su cuenta",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161)
                ),
                textAlign = TextAlign.Center,
                modifier = modifier.padding(horizontal = 24.dp)
            )
            Text(
                "Correo Electrónico",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7D7A7A)
                ),
                textAlign = TextAlign.Center,
                modifier = modifier.padding(24.dp)
            )
            EmailTextField(
                value = email,
                onValueChange = { email = it }
            )
        }
    }
}




@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    BeneficioJuventudTheme {
        val navController = rememberNavController()
        Login(nav = navController)
    }
}