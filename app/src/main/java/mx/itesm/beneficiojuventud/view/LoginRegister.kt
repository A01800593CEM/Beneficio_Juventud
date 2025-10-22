package mx.itesm.beneficiojuventud.view

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.AltLoginButton
import mx.itesm.beneficiojuventud.components.GradientDivider_OR
import mx.itesm.beneficiojuventud.components.MainButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import mx.itesm.beneficiojuventud.model.TestRemote
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModelFactory

/**
 * Pantalla inicial con opciones para iniciar sesión o registrarse.
 * @param nav Controlador de navegación usado para cambiar de pantalla.
 * @param modifier Modificador opcional para personalizar el diseño.
 */
@Composable
fun LoginRegister(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authState by authViewModel.authState.collectAsState()

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isCheckingGoogleUser by remember { mutableStateOf(false) }
    var canRetry by remember { mutableStateOf(false) }

    // Función para verificar y navegar según usuario de Google
    fun checkAndNavigateGoogleUser(googleData: mx.itesm.beneficiojuventud.viewmodel.GoogleUserData) {
        scope.launch {
            Log.d("LoginRegister", "🔍 checkAndNavigateGoogleUser - AuthViewModel instance: ${authViewModel.hashCode()}")
            Log.d("LoginRegister", "📦 Google data recibida: ${googleData.email}")
            isCheckingGoogleUser = true
            canRetry = false
            try {
                val exists = userViewModel.emailExists(googleData.email)

                if (exists) {
                    Log.d("LoginRegister", "✅ Usuario ya existe, navegando a PostLoginPermissions")
                    authViewModel.clearPendingGoogleUserData()

                    // Asegurar que currentUserId está actualizado antes de navegar
                    authViewModel.getCurrentUser()
                    delay(500)

                    Log.d("LoginRegister", "✅ CurrentUserId antes de navegar: ${authViewModel.currentUserId.value}")

                    // ⭐️ IMPORTANTE: Solo limpiar error, NO clearState()
                    // clearState() borra currentUserId que se necesita en PostLoginPermissions → Startup
                    authViewModel.clearError()

                    nav.navigate(Screens.PostLoginPermissions.route) {
                        popUpTo(Screens.LoginRegister.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    Log.d("LoginRegister", "📝 Primer login con Google, navegando a GoogleRegister")
                    Log.d("LoginRegister", "📦 Antes de navegar - pendingGoogleUserData: ${authViewModel.pendingGoogleUserData?.email}")
                    authViewModel.clearError()
                    Log.d("LoginRegister", "📦 Después de clearError - pendingGoogleUserData: ${authViewModel.pendingGoogleUserData?.email}")
                    nav.navigate(Screens.GoogleRegister.route) {
                        popUpTo(Screens.LoginRegister.route) { inclusive = false }
                        launchSingleTop = true
                    }
                    Log.d("LoginRegister", "📦 Después de navegar - pendingGoogleUserData: ${authViewModel.pendingGoogleUserData?.email}")
                }
            } catch (e: Exception) {
                Log.e("LoginRegister", "❌ Error verificando usuario: ${e.message}", e)

                val isTimeout = e is java.net.SocketTimeoutException ||
                               e.message?.contains("timeout", ignoreCase = true) == true

                if (isTimeout) {
                    errorMessage = "El servidor tardó mucho en responder. Por favor, intenta de nuevo."
                    showError = true
                    canRetry = true
                    Log.w("LoginRegister", "⚠️ Timeout - permitiendo reintento")
                } else {
                    errorMessage = "Error verificando usuario: ${e.message}"
                    showError = true
                    authViewModel.signOut()
                }
            } finally {
                isCheckingGoogleUser = false
            }
        }
    }

    // Manejar resultado de Google Sign-In
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess && !isCheckingGoogleUser) {
            isCheckingGoogleUser = true

            // Verificar si hay datos de Google (indica que fue login con Google)
            val googleData = authViewModel.pendingGoogleUserData

            if (googleData != null) {
                Log.d("LoginRegister", "📧 Google Sign-In exitoso: ${googleData.email}")
                checkAndNavigateGoogleUser(googleData)
            } else {
                isCheckingGoogleUser = false
                authViewModel.clearState() // Solo limpiar si NO es Google
            }
        }
    }

    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Log.e("LoginRegister", "❌ Error en autenticación: $error")
            errorMessage = error
            showError = true
            isCheckingGoogleUser = false
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 15.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_beneficio_joven),
                contentDescription = "",
                modifier = Modifier.size(80.dp)
            )

            // Títulos
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Empieza Ahora",
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Crea una cuenta o inicia sesión para explorar nuestra app",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // Botones principales
            Column(modifier = Modifier.fillMaxWidth(0.94f)) {
                MainButton("Inicia Sesión", modifier = Modifier.padding(top = 26.dp)) {
                    //TestRemote.probarLlamada()
                    //TestRemote.probarPromoService()
                    nav.navigate(Screens.Login.route)
                }
                MainButton("Regístrate", modifier = Modifier.padding(top = 16.dp)) {
                    nav.navigate(Screens.Register.route)
                }

                GradientDivider_OR(modifier = Modifier.padding(vertical = 32.dp))

                AltLoginButton(
                    text = if (authState.isLoading || isCheckingGoogleUser) "Verificando..." else "Regístrate con Google",
                    icon = painterResource(id = R.drawable.logo_google),
                    contentDescription = "Regístrate con Google",
                    onClick = {
                        val activity = context as? Activity
                        if (activity != null) {
                            Log.d("LoginRegister", "🔵 Iniciando Google Sign-In desde LoginRegister")
                            authViewModel.signInWithGoogle(activity)
                        } else {
                            errorMessage = "Error: contexto de actividad no disponible"
                            showError = true
                        }
                    }
                )

                // Error global
                if (showError && errorMessage.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(0.94f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                        canRetry = false
                                        authViewModel.clearError()
                                    }
                                ) { Text("✕", color = Color(0xFFD32F2F)) }
                            }

                            // Botón de reintentar si hubo timeout
                            if (canRetry) {
                                Spacer(Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        showError = false
                                        val googleData = authViewModel.pendingGoogleUserData
                                        if (googleData != null) {
                                            checkAndNavigateGoogleUser(googleData)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        "Reintentar",
                                        color = Color(0xFF008D96),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
//                AltLoginButton(
//                    "Continuar con Facebook",
//                    painterResource(id = R.drawable.logo_facebook),
//                    "Continuar con Facebook",
//                    onClick = { /* TODO login Facebook */ },
//                    modifier = Modifier.padding(top = 16.dp)
//                )
            }

            Spacer(Modifier.height(32.dp))

            // Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp)
            ) {
                Text(
                    "¿Quieres ser colaborador?",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7D7A7A)
                    )
                )
                // Panel de Colaboradres Registro
                TextButton(onClick = { nav.navigate(Screens.RegisterCollab.route) }) {
                    Text(
                        "Registrate",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF008D96)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Vista previa de [LoginRegister] en el editor de Android Studio.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginRegisterPreview() {
    BeneficioJuventudTheme {
        val nav = rememberNavController()
        LoginRegister(nav = nav)
    }
}