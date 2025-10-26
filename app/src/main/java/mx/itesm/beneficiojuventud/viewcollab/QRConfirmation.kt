package mx.itesm.beneficiojuventud.viewcollab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.view.Screens
import mx.itesm.beneficiojuventud.view.StatusType
import mx.itesm.beneficiojuventud.viewcollab.QRScannerViewModel

/**
 * Data class para pasar información a la pantalla de confirmación de QR
 */
data class QRConfirmationData(
    val userName: String,
    val promotionTitle: String,
    val collaboratorName: String,
    val promotionId: Int,
    val userId: String,
    val branchId: Int,
    val nonce: String,
    val qrTimestamp: Long
)

/**
 * Pantalla de confirmación para QR escaneado
 * Muestra la información del cupón antes de registrar el uso
 */
@Composable
fun QRConfirmationScreen(
    nav: NavHostController,
    qrConfirmationData: QRConfirmationData,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    viewModel: QRScannerViewModel = viewModel()
) {
    // Observar cambios en el resultado del scan
    val scanResult = viewModel.scanResult.collectAsState()
    val error = viewModel.error.collectAsState()

    // Manejar éxito de redención
    LaunchedEffect(scanResult.value) {
        scanResult.value?.let {
            // Cupón canjeado exitosamente
            nav.navigate(
                Screens.Status.createRoute(
                    StatusType.QR_SCAN_SUCCESS,
                    Screens.HomeScreenCollab.route
                )
            ) {
                popUpTo(Screens.QrScanner.route) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.clearResult()
        }
    }

    // Manejar errores
    LaunchedEffect(error.value) {
        error.value?.let {
            // Error al canjear
            nav.navigate(
                Screens.Status.createRoute(
                    StatusType.QR_SCAN_ERROR,
                    Screens.HomeScreenCollab.route
                )
            ) {
                popUpTo(Screens.QrScanner.route) { inclusive = true }
                launchSingleTop = true
            }
            viewModel.clearError()
        }
    }

    // Bloquea el botón de retroceso
    BackHandler(enabled = true) {
        onCancel()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícono informativo
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Confirmación",
            tint = Color(0xFF1976D2),
            modifier = Modifier.size(100.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Título
        Text(
            text = "Confirmar cupón",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF616161)
        )

        Spacer(Modifier.height(32.dp))

        // Información del cupón
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Nombre del Usuario
            Text(
                text = "Usuario",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Text(
                text = qrConfirmationData.userName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF616161),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Título de la Promoción
            Text(
                text = "Promoción",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Text(
                text = qrConfirmationData.promotionTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF616161),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Nombre del Colaborador
            Text(
                text = "Colaborador",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Text(
                text = qrConfirmationData.collaboratorName,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
        }

        Spacer(Modifier.height(48.dp))

        // Botones de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón Cancelar (Rojo)
            MainButton(
                text = "Cancelar",
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                backgroundGradient = Brush.linearGradient(
                    listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))
                )
            )

            // Botón Continuar (Verde por defecto)
            MainButton(
                text = "Continuar",
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun QRConfirmationScreenPreview() {
    BeneficioJuventudTheme {
        QRConfirmationScreen(
            nav = rememberNavController(),
            qrConfirmationData = QRConfirmationData(
                userName = "Juan Pérez",
                promotionTitle = "Descuento 50% en café",
                collaboratorName = "Cafetería El Buen Café",
                promotionId = 123,
                userId = "user123",
                branchId = 1,
                nonce = "abc12345",
                qrTimestamp = System.currentTimeMillis()
            ),
            onConfirm = { },
            onCancel = { }
        )
    }
}
