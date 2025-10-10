package mx.itesm.beneficiojuventud.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    // 🔹 Blanco puro para toda la app
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),

    // 🔹 Texto oscuro sobre fondo blanco
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000)
)

@Composable
fun BeneficioJuventudTheme(
    darkTheme: Boolean = false,  // Puedes cambiar a isSystemInDarkTheme() si quieres detectar sistema
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
