package mx.itesm.beneficiojuventud.viewcollab

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.view.Screens

/**
 * Pantalla de estatus para colaboradores después del registro y confirmación.
 * Muestra que la cuenta fue creada correctamente pero que aún falta completar el perfil.
 * Requiere que el usuario presione "Continuar" para ir a editar el perfil.
 * Bloquea la navegación hacia atrás.
 */
@Composable
fun StatusCollabSignup(
    nav: NavHostController,
    modifier: Modifier = Modifier
) {
    // Bloquea el botón de retroceso del sistema
    BackHandler(enabled = true) {
        // No hace nada - bloquea la navegación hacia atrás
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono de éxito
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Cuenta creada exitosamente",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título principal
        Text(
            text = "¡Cuenta creada exitosamente!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF4B4C7E)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtítulo 1 - Confirmación
        Text(
            text = "Tu cuenta de colaborador ha sido registrada correctamente.",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color(0xFF616161)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtítulo 2 - Necesidad de completar perfil
        Text(
            text = "Aún falta completar tu perfil para poder acceder a todas las funcionalidades.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = Color(0xFF7D7A7A)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón Continuar
        MainButton(
            text = "Continuar",
            modifier = Modifier.fillMaxWidth()
        ) {
            nav.navigate(Screens.EditProfileCollab.route) {
                launchSingleTop = true
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StatusCollabSignupPreview() {
    BeneficioJuventudTheme {
        StatusCollabSignup(
            nav = rememberNavController()
        )
    }
}
