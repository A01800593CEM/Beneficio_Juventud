package mx.itesm.beneficiojuventud.view

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.tooling.preview.Preview
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModelFactory

/**
 * Pantalla de inicio de sesión con email y contraseña.
 * @param nav Controlador de navegación.
 * @param modifier Modificador de diseño opcional.
 * @param authViewModel ViewModel que gestiona el proceso de autenticación.
 */
@Composable
fun Login(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isCheckingGoogleUser by remember { mutableStateOf(false) }
    var canRetry by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    // Función para verificar y navegar según usuario de Google
    fun checkAndNavigateGoogleUser(googleData: mx.itesm.beneficiojuventud.viewmodel.GoogleUserData) {
        scope.launch {
            Log.d("Login", "🔍 checkAndNavigateGoogleUser - AuthViewModel instance: ${authViewModel.hashCode()}")
            Log.d("Login", "📦 Google data recibida: ${googleData.email}")
            isCheckingGoogleUser = true
            canRetry = false
            try {
                val exists = userViewModel.emailExists(googleData.email)

                if (exists) {
                    Log.d("Login", "✅ Usuario ya existe, navegando a PostLoginPermissions")
                    authViewModel.clearPendingGoogleUserData()

                    // Asegurar que currentUserId está actualizado antes de navegar
                    authViewModel.getCurrentUser()
                    delay(500) // Esperar a que se actualice

                    Log.d("Login", "✅ CurrentUserId antes de navegar: ${authViewModel.currentUserId.value}")

                    authViewModel.clearState()
                    nav.navigate(Screens.PostLoginPermissions.route) {
                        popUpTo(Screens.LoginRegister.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    Log.d("Login", "📝 Primer login con Google, navegando a GoogleRegister")
                    Log.d("Login", "📦 Antes de navegar - pendingGoogleUserData: ${authViewModel.pendingGoogleUserData?.email}")
                    authViewModel.clearError()
                    Log.d("Login", "📦 Después de clearError - pendingGoogleUserData: ${authViewModel.pendingGoogleUserData?.email}")
                    nav.navigate(Screens.GoogleRegister.route) {
                        popUpTo(Screens.Login.route) { inclusive = false }
                        launchSingleTop = true
                    }
                    Log.d("Login", "📦 Después de navegar - pendingGoogleUserData: ${authViewModel.pendingGoogleUserData?.email}")
                }
            } catch (e: Exception) {
                Log.e("Login", "❌ Error verificando usuario: ${e.message}", e)

                val isTimeout = e is java.net.SocketTimeoutException ||
                               e.message?.contains("timeout", ignoreCase = true) == true

                if (isTimeout) {
                    errorMessage = "El servidor tardó mucho en responder. Por favor, intenta de nuevo."
                    showError = true
                    canRetry = true
                    Log.w("Login", "⚠️ Timeout - permitiendo reintento")
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
            // Verificar si hay datos de Google (indica que fue login con Google)
            val googleData = authViewModel.pendingGoogleUserData

            if (googleData != null) {
                Log.d("Login", "📧 Google Sign-In exitoso: ${googleData.email}")
                isCheckingGoogleUser = true
                checkAndNavigateGoogleUser(googleData)
            } else {
                // Login tradicional (email/contraseña)
                nav.navigate(Screens.PostLoginPermissions.route) {
                    popUpTo(Screens.LoginRegister.route) { inclusive = true }
                    launchSingleTop = true
                }
                authViewModel.clearState()
            }
        }
    }

    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            Log.e("Login", "❌ Error en autenticación: $error")
            errorMessage = error
            showError = true
            isCheckingGoogleUser = false
        }
    }

    /** Envuelve un campo y lo desplaza al recibir foco para evitar ocultarlo con el teclado. */
    @Composable
    fun FocusBringIntoView(
        delayMs: Long = 140,
        content: @Composable (Modifier) -> Unit
    ) {
        val requester = remember { BringIntoViewRequester() }
        val scope = rememberCoroutineScope()
        val mod = Modifier
            .bringIntoViewRequester(requester)
            .onFocusEvent { st ->
                if (st.isFocused) {
                    scope.launch {
                        awaitFrame()
                        delay(delayMs)
                        requester.bringIntoView()
                    }
                }
            }
        content(mod)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.ime)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿No tienes cuenta?  ",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A))
                )
                TextButton(onClick = { nav.navigate(Screens.Register.route) }) {
                    Text(
                        "Regístrate",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF008D96))
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .dismissKeyboardOnTap()
        ) {
            LazyColumn(
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_beneficio_joven),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
                item {
                    Text(
                        "Inicia Sesión",
                        style = TextStyle(
                            brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        ),
                        modifier = Modifier.padding(top = 18.dp, start = 6.dp, end = 6.dp, bottom = 14.dp)
                    )
                }
                item {
                    Text(
                        "Por favor, inicie sesión en su cuenta",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161)),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
                item {
                    Text(
                        "Correo Electrónico",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A)),
                        modifier = Modifier.padding(start = 6.dp, top = 32.dp, end = 6.dp, bottom = 8.dp)
                    )
                }
                item {
                    FocusBringIntoView {
                        EmailTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = it.fillMaxWidth().padding(horizontal = 6.dp)
                        )
                    }
                }
                item {
                    Text(
                        "Contraseña",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A)),
                        modifier = Modifier.padding(start = 6.dp, top = 20.dp, end = 6.dp, bottom = 8.dp)
                    )
                }
                item {
                    FocusBringIntoView {
                        PasswordTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = it.fillMaxWidth().padding(horizontal = 6.dp)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                            Text(
                                "Recuérdame",
                                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A))
                            )
                        }
                        TextButton(onClick = { nav.navigate(Screens.ForgotPassword.route) }) {
                            Text(
                                "¿Olvidaste tu contraseña?",
                                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7D7A7A))
                            )
                        }
                    }
                }

                // BOTÓN PRINCIPAL EN EL CONTENIDO
                item {
                    MainButton(
                        text = if (authState.isLoading && !isCheckingGoogleUser) "Iniciando sesión..." else "Inicia Sesión",
                        enabled = !authState.isLoading && !isCheckingGoogleUser && email.isNotEmpty() && password.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 12.dp)
                    ) {
                        showError = false
                        authViewModel.signIn(email, password, rememberMe)
                    }
                }

                // Divisor "O"
                item {
                    GradientDivider_OR(modifier = Modifier.padding(vertical = 24.dp))
                }

                // Botón de Google Sign-In
                item {
                    AltLoginButton(
                        text = if (authState.isLoading || isCheckingGoogleUser) "Verificando..." else "Continuar con Google",
                        icon = painterResource(id = R.drawable.logo_google),
                        contentDescription = "Continuar con Google",
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                Log.d("Login", "🔵 Iniciando Google Sign-In desde Login")
                                authViewModel.signInWithGoogle(activity)
                            } else {
                                errorMessage = "Error: contexto de actividad no disponible"
                                showError = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                // Mensaje de error
                if (showError && errorMessage.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
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
                }

                // Espaciador final
                item {
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

/** Vista previa de [Login] en el editor de Android Studio. */
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLogin() {
    BeneficioJuventudTheme {
        val navController = rememberNavController()
        Login(nav = navController)
    }
}
