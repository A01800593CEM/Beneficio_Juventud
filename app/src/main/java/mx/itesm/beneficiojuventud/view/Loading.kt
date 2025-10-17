package mx.itesm.beneficiojuventud.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFFFFFF)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_beneficio_joven),
                    contentDescription = "Logo de Beneficio Joven",
                    modifier = Modifier.size(250.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Colores para el degradado. La lista extendida ayuda a que el degradado sea más visible
                val gradientColors = listOf(Color(0xFF4B4C7E), Color(0xFF008D96), Color(0xFF4B4C7E))

                val transition = rememberInfiniteTransition(label = "text-gradient-animation")
                val translateAnimation by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1000f,
                    animationSpec = infiniteRepeatable(
                        // La clave está en usar LinearEasing para una velocidad constante
                        animation = tween(durationMillis = 1700, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse // Ahora la reversa será instantánea
                    ),
                    label = "text-gradient-animation-translate"
                )

                // El pincel que se mueve horizontalmente
                val brush = Brush.horizontalGradient(
                    colors = gradientColors,
                    startX = translateAnimation - 500f, // Ajustamos para que el degradado pase completamente
                    endX = translateAnimation
                )

                Text(
                    text = "CARGANDO ...",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        brush = brush
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoadingScreen() {
    BeneficioJuventudTheme {
        Loading()
    }
}
