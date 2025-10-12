package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

/**
 * Pantalla inicial del flujo de bienvenida de Beneficio Joven.
 * Presenta el logotipo, una breve descripción y un botón para continuar con la selección de categorías.
 * @param nav Controlador de navegación para gestionar transiciones de pantallas.
 * @param modifier Modificador opcional para personalizar el diseño externo.
 * @param imageRes Recurso drawable mostrado como ilustración principal.
 * @param onStart Acción ejecutada al presionar el botón “¡Empecemos!”, por defecto navega a [Screens.OnboardingCategories].
 * @return Unit
 */
@Composable
fun Onboarding(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    imageRes: Int = R.drawable.onboarding_one,
    onStart: () -> Unit = { nav.navigate(Screens.OnboardingCategories.route) }
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            val isCompact = maxWidth < 360.dp || maxHeight < 640.dp

            val sidePad = if (isCompact) 16.dp else 24.dp
            val title1Size = if (isCompact) 22.sp else 26.sp
            val title2Size = if (isCompact) 26.sp else 32.sp
            val subtitleSize = if (isCompact) 13.sp else 14.sp
            val imageHeight = if (isCompact) 220.dp else 280.dp
            val buttonTop = if (isCompact) 16.dp else 24.dp

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = sidePad, vertical = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(if (isCompact) 8.dp else 16.dp))

                // Título
                Text(
                    "Bienvenid@ a",
                    style = TextStyle(
                        fontSize = title1Size,
                        fontWeight = FontWeight.Black,
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96)))
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    "BENEFICIO JOVEN",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = title2Size,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Encuentra cupones hechos para ti",
                    style = TextStyle(
                        fontSize = subtitleSize,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Ilustración
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxHeight()
                    )
                }

                Spacer(Modifier.height(if (isCompact) 8.dp else 12.dp))

                // Botón
                MainButton(
                    text = "¡Empecemos!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = buttonTop)
                ) {
                    onStart()
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Previsualiza la pantalla de bienvenida en modo claro y con sistema UI visible.
 * @return Unit
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPreview() {
    BeneficioJuventudTheme {
        Onboarding(nav = rememberNavController())
    }
}
