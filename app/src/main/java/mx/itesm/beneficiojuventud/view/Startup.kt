package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

/**
 * Pantalla de inicio con un fondo de degradado y el logo de la aplicación en el centro.
 */
@Composable
fun Startup(modifier: Modifier = Modifier) {
    val backgroundGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4B4C7E),
            Color(0xFF008D96)
        )
    )

    Box(
        modifier = modifier
            .background(backgroundGradient)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_white),
            contentDescription = "Logo de Beneficio Joven",
            modifier = Modifier.size(250.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Vista previa para la pantalla de inicio [Startup].
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StartupPreview() {
    BeneficioJuventudTheme {
        Startup()
    }
}
