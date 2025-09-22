package mx.itesm.beneficiojuventud.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import mx.itesm.beneficiojuventud.R

// Set of Material typography styles to start with

val Inter = FontFamily(
    Font(R.font.inter_28pt_thin, FontWeight.Thin),
    Font(R.font.inter_28pt_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.inter_28pt_extralight, FontWeight.ExtraLight),
    Font(R.font.inter_28pt_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.inter_28pt_light, FontWeight.Light),
    Font(R.font.inter_28pt_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.inter_28pt_regular, FontWeight.Normal),
    Font(R.font.inter_28pt_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.inter_28pt_medium, FontWeight.Medium),
    Font(R.font.inter_28pt_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.inter_28pt_semibold, FontWeight.SemiBold),
    Font(R.font.inter_28pt_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.inter_28pt_bold, FontWeight.Bold),
    Font(R.font.inter_28pt_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.inter_28pt_extrabold, FontWeight.ExtraBold),
    Font(R.font.inter_28pt_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.inter_28pt_black, FontWeight.Black),
    Font(R.font.inter_28pt_blackitalic, FontWeight.Black, FontStyle.Italic)
)


val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Inter),
        displayMedium = displayMedium.copy(fontFamily = Inter),
        displaySmall = displaySmall.copy(fontFamily = Inter),
        headlineLarge = headlineLarge.copy(fontFamily = Inter),
        headlineMedium = headlineMedium.copy(fontFamily = Inter),
        headlineSmall = headlineSmall.copy(fontFamily = Inter),
        titleLarge = titleLarge.copy(fontFamily = Inter),
        titleMedium = titleMedium.copy(fontFamily = Inter),
        titleSmall = titleSmall.copy(fontFamily = Inter),
        bodyLarge = bodyLarge.copy(fontFamily = Inter),
        bodyMedium = bodyMedium.copy(fontFamily = Inter),
        bodySmall = bodySmall.copy(fontFamily = Inter),
        labelLarge = labelLarge.copy(fontFamily = Inter),
        labelMedium = labelMedium.copy(fontFamily = Inter),
        labelSmall = labelSmall.copy(fontFamily = Inter)
    )
}
