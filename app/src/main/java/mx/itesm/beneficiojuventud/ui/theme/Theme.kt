package mx.itesm.beneficiojuventud.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Teal,
    secondary = DarkBlue,
    tertiary = Teal,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    secondary = DarkBlue,
    tertiary = Teal,

    // Background and Surface - Pure white
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color.White,

    // Surface containers - All white for clean look
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,

    // Text on backgrounds
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextGrey,
    onSurface = TextGrey,
    onSurfaceVariant = TextGrey,

    // Borders and outlines
    outline = BorderGrey,
    outlineVariant = LightGrey
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
