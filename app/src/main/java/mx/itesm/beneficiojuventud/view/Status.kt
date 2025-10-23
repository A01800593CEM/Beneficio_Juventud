package mx.itesm.beneficiojuventud.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

/**
 * Enum para definir los diferentes tipos de estados que la pantalla puede mostrar.
 * Cada tipo encapsula si es un éxito o un error, el título y el subtítulo.
 */
enum class StatusType(
    val isSuccess: Boolean,
    val title: String,
    val subtitle: String
) {
    REGISTRATION_SUCCESS(
        isSuccess = true,
        title = "¡Te has registrado correctamente!",
        subtitle = "Redirigiéndote a la pantalla de Login"
    ),
    COUPON_SAVE_SUCCESS(
        isSuccess = true,
        title = "¡Cupón guardado!",
        subtitle = "Lo encontrarás en tu sección de cupones"
    ),
    COUPON_RESERVATION_SUCCESS(
        isSuccess = true,
        title = "¡Cupón reservado exitosamente!",
        subtitle = "Lo encontrarás en tu sección de favoritos"
    ),
    COUPON_USE_SUCCESS(
        isSuccess = true,
        title = "¡Has usado tu cupón!",
        subtitle = "Disfruta de tu beneficio"
    ),
    QR_SCAN_SUCCESS(
        isSuccess = true,
        title = "¡Cupón canjeado exitosamente!",
        subtitle = "El cupón ha sido registrado correctamente"
    ),
    VERIFICATION_SUCCESS(
        isSuccess = true,
        title = "¡Verificado correctamente!",
        subtitle = "Ya puedes continuar"
    ),
    COUPON_SAVE_ERROR(
        isSuccess = false,
        title = "Error al guardar",
        subtitle = "No se pudo guardar el cupón, intenta de nuevo"
    ),
    COUPON_RESERVATION_ERROR(
        isSuccess = false,
        title = "Error al reservar",
        subtitle = "No se pudo reservar el cupón, intenta de nuevo"
    ),
    COUPON_USE_ERROR(
        isSuccess = false,
        title = "No se pudo canjear",
        subtitle = "Ocurrió un error al usar el cupón"
    ),
    QR_SCAN_ERROR(
        isSuccess = false,
        title = "Error al escanear",
        subtitle = "No se pudo canjear el cupón, verifica los datos"
    ),
    VERIFICATION_ERROR(
        isSuccess = false,
        title = "Verificación fallida",
        subtitle = "Los datos no son correctos"
    ),
    USER_INFO_UPDATED(
        isSuccess = true,
        title = "¡Información actualizada!",
        subtitle = "Tus datos se han guardado correctamente"
    ),
    USER_INFO_UPDATE_ERROR(
        isSuccess = false,
        title = "Error al actualizar",
        subtitle = "No se pudo guardar tu información, intenta de nuevo"
    ),
    PROMOTION_CREATION_SUCCESS(
        isSuccess = true,
        title = "¡Promoción creada exitosamente!",
        subtitle = "Tu nueva promoción ya está disponible"
    ),
    PROMOTION_UPDATE_SUCCESS(
        isSuccess = true,
        title = "¡Promoción actualizada exitosamente!",
        subtitle = "Los cambios se han guardado correctamente"
    ),
    PROMOTION_CREATION_ERROR(
        isSuccess = false,
        title = "Error al crear promoción",
        subtitle = "No se pudo crear la promoción, intenta de nuevo"
    ),
    PROMOTION_UPDATE_ERROR(
        isSuccess = false,
        title = "Error al actualizar promoción",
        subtitle = "No se pudo actualizar la promoción, intenta de nuevo"
    )
}

/**
 * Pantalla genérica para mostrar un estado de éxito o error y luego redirigir.
 *
 * @param nav El controlador de navegación para la redirección.
 * @param statusType El tipo de estado a mostrar (debe ser uno de los definidos en el enum).
 * @param destinationRoute La ruta a la que se navegará después del delay.
 */
@Composable
fun StatusScreen(
    nav: NavHostController,
    statusType: StatusType,
    destinationRoute: String
) {

    // Determina el ícono y el color basado en si el estado es de éxito o error
    val icon = if (statusType.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error
    val iconColor = if (statusType.isSuccess) Color(0xFF4CAF50) else Color(0xFFD32F2F)

    // Deshabilita el botón de retroceso mientras estamos en StatusScreen
    BackHandler(enabled = true) {
        // No hace nada - bloquea el botón de retroceso
    }

    // Efecto que se lanza una sola vez para manejar la redirección automática

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = statusType.title,
            tint = iconColor,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = statusType.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF616161)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = statusType.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        MainButton(
            text = "Continuar",
            modifier = Modifier.fillMaxWidth()
        ) {
            // Primero, elimina el StatusScreen de la pila
            nav.popBackStack()
            // Luego navega al destino
            nav.navigate(destinationRoute) {
                launchSingleTop = true
                restoreState = false
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Éxito")
@Composable
private fun StatusScreenSuccessPreview() {
    BeneficioJuventudTheme {
        StatusScreen(
            nav = rememberNavController(),
            statusType = StatusType.COUPON_USE_SUCCESS,
            destinationRoute = "login_screen" // Ruta de ejemplo como String
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Error")
@Composable
private fun StatusScreenErrorPreview() {
    BeneficioJuventudTheme {
        StatusScreen(
            nav = rememberNavController(),
            statusType = StatusType.VERIFICATION_ERROR,
            destinationRoute = "home_screen" // Ruta de ejemplo como String
        )
    }
}

