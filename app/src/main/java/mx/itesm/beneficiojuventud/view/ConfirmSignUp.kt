package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.MainButton
import mx.itesm.beneficiojuventud.components.CodeTextField
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.AppViewModel

@Composable
fun ConfirmSignUp(
    nav: NavHostController,
    email: String,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    appViewModel: AppViewModel? = null
) {
    var code by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()

    // Manejar confirmación exitosa
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            // Registro completado exitosamente
            authViewModel.clearState()
            // Navegar al onboarding
            nav.navigate(Screens.Onboarding.route) {
                popUpTo(Screens.LoginRegister.route) { inclusive = true }
            }
        }
    }

    // Manejar errores
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            errorMessage = error
            showError = true
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 85.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logo_beneficio_joven),
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
            }

            // Título
            Text(
                "Confirma tu cuenta",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                    ),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Descripción
            Text(
                "Ingresa el código de 6 dígitos que enviamos a:",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7D7A7A)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Email
            Text(
                email,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF008D96)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo de código
            CodeTextField(
                value = code,
                onValueChange = {
                    if (it.length <= 6) {
                        code = it
                        showError = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Mostrar error si existe
            if (showError && errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                showError = false
                                errorMessage = ""
                                authViewModel.clearState()
                            }
                        ) {
                            Text("✕", color = Color(0xFFD32F2F))
                        }
                    }
                }
            }

            // Botón confirmar
            MainButton(
                text = if (authState.isLoading) "Confirmando..." else "Confirmar",
                enabled = !authState.isLoading && code.length == 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                showError = false
                authViewModel.confirmSignUp(email, code)
            }

            Spacer(Modifier.height(16.dp))

            // Reenviar código
            TextButton(
                onClick = {
                    // TODO: Implementar reenvío de código
                }
            ) {
                Text(
                    "¿No recibiste el código? Reenviar",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF008D96)
                    )
                )
            }

            Spacer(Modifier.weight(1f))

            // Volver
            TextButton(
                onClick = {
                    appViewModel?.clearPendingUserProfile()
                    nav.navigate(Screens.Register.route)
                }
            ) {
                Text(
                    "Volver al registro",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7D7A7A)
                    )
                )
            }
        }
    }
}