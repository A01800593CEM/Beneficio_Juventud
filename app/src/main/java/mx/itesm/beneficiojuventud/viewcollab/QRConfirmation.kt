package mx.itesm.beneficiojuventud.viewcollab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.view.Screens
import mx.itesm.beneficiojuventud.view.StatusType
import mx.itesm.beneficiojuventud.viewcollab.QRScannerViewModel
import mx.itesm.beneficiojuventud.viewmodel.QRConfirmationViewModel

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
    viewModel: QRScannerViewModel
) {
    // Observar cambios en el resultado del scan
    val scanResult = viewModel.scanResult.collectAsState()
    val error = viewModel.error.collectAsState()
    val isProcessing = viewModel.isProcessing.collectAsState()

    // Manejar éxito de redención
    LaunchedEffect(scanResult.value) {
        if (scanResult.value != null && !isProcessing.value) {
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
        if (error.value != null && !isProcessing.value) {
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

    Scaffold(
        topBar = {
            BJTopHeader(title = "Confirmar cupón", nav = nav)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espaciador superior
            Spacer(Modifier.height(32.dp))

            // Ícono de confirmación
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Confirmar",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(100.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Título
            Text(
                text = "Verifica los datos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF616161),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Por favor confirma la información del cupón",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFAEAEAE),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Tarjeta con información del cupón
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Sección: Usuario
                    InfoRow(
                        label = "Usuario",
                        value = qrConfirmationData.userName
                    )

                    // Divisor
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEEEEEE))
                    )

                    // Sección: Promoción
                    InfoRow(
                        label = "Promoción",
                        value = qrConfirmationData.promotionTitle,
                        isHighlight = true
                    )

                    // Divisor
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEEEEEE))
                    )

                    // Sección: Colaborador
                    InfoRow(
                        label = "Colaborador",
                        value = qrConfirmationData.collaboratorName,
                        isSmall = true
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Botones de acción
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Continuar (Principal)
                MainButton(
                    text = "Confirmar y registrar",
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth()
                )

                // Botón Cancelar (Secundario - Simple)
                MainButton(
                    text = "Cancelar",
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundGradient = Brush.linearGradient(
                        listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
                    )
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Composable auxiliar para mostrar información estructurada
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isSmall: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = if (isSmall) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFAEAEAE)
        )
        Text(
            text = value,
            style = if (isSmall) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) Color(0xFF4CAF50) else Color(0xFF616161)
        )
    }
}
