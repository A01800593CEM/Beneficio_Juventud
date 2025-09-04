package mx.itesm.beneficiojuventud.view

import androidx.compose.material3.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

// De León working on this

val inter = FontFamily(
    Font(R.font.inter_28pt_black, FontWeight.Black),
    Font(R.font.inter_28pt_semibold, FontWeight.SemiBold),
    Font(R.font.inter_28pt_extrabold, FontWeight.ExtraBold)
)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeneficioJuventudTheme {
                Login()
            }
        }
    }
}

@Composable
fun Login(modifier: Modifier = Modifier) {
    Scaffold(modifier = Modifier.fillMaxSize(),
    ){ innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(top = 85.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_beneficio_joven),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
            )
            Column(modifier = modifier.fillMaxWidth(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Empieza Ahora",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4B4C7E),
                                Color(0xFF008D96)
                            )
                        ),
                        fontSize = 30.sp,
                        fontFamily = inter,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = modifier.padding(20.dp)
                )
                Text(
                    "Crea una cuenta o inicia sesión para explorar nuestra app",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = inter,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = modifier.padding(horizontal = 24.dp)
                )
            }
            Column (modifier = modifier.fillMaxWidth(0.94f)){
                MainButton("Inicia Sesión", modifier = Modifier.padding(top = 50.dp))
                MainButton("Regístrate", modifier = Modifier.padding(top = 20.dp))
                GradientDivider(modifier = modifier.padding(vertical = 50.dp))
                AltLoginButton(
                    "Continuar con Google",
                    painterResource(id = R.drawable.logo_google),
                    "Continuar con Google",
                    { /* TODO: login con Google */ }
                )
                AltLoginButton(
                    "Continuar con Facebook",
                    painterResource(id = R.drawable.logo_facebook),
                    "Continuar con Facebook",
                    { /* TODO: login con Facebook */ },
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ){
                Text(
                    "¿Quieres ser colaborador?",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = inter,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A))
                )
                TextButton(onClick = {}) {
                    Text("Ver más",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = inter,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF008D96))
                    )
                }
            }
        }
    }
}

@Composable
fun MainButton(
    inputText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {
        /* TODO */
    }
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(
                elevation = 8.dp,
                shape = shape,
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF4B4C7E),
                        Color(0xFF008D96)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            inputText,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = inter,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        )
    }
}


@Composable
fun GradientDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Línea izquierda
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF4B4C7E),
                            Color(0xFF008D96)
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
        Box(){
            Text("  O  ",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = inter,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD9D9D9)
                ))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF008D96),
                            Color(0xFF4B4C7E)
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

@Composable
fun AltLoginButton(
    text: String,
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp), // bordes redondeados
        border = BorderStroke(1.dp, Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Ícono
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                tint = Color.Unspecified, // 👈 para que conserve sus colores originales
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Texto
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF616161),
                    fontFamily = inter,
                    fontWeight = FontWeight.Bold

                )
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    BeneficioJuventudTheme {
        Login()
    }
}