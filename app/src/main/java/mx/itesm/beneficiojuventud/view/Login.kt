package mx.itesm.beneficiojuventud.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.layout.onSizeChanged
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.viewmodel.AppViewModel
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel

@Composable
fun Login(
    nav: NavHostController,
    appViewModel: AppViewModel? = null,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            nav.navigate(Screens.Onboarding.route) {
                popUpTo(Screens.LoginRegister.route) { inclusive = true }
            }
            viewModel.clearState()
        }
    }
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            errorMessage = error
            showError = true
        }
    }

    // ✅ Deja que el Scaffold maneje status/nav bars sin duplicar paddings
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

        // --- Medimos contenedor y contenido para decidir si hay overflow ---
        var containerHeight by remember { mutableStateOf(0) }
        var contentHeight by remember { mutableStateOf(0) }
        val needsScroll by derivedStateOf { contentHeight > containerHeight }

        val scrollState = rememberScrollState()

        // Helper: solo trae al campo a la vista si hay scroll; si no, es no-op.
        @Composable
        fun FocusBringIntoView(
            delayMs: Long = 140,
            content: @Composable (Modifier) -> Unit
        ) {
            if (!needsScroll) {
                content(Modifier) // sin scroll, no hace falta requester
                return
            }
            val requester = remember { BringIntoViewRequester() }
            val scope = rememberCoroutineScope()
            val mod = Modifier
                .bringIntoViewRequester(requester)
                .onFocusEvent { state ->
                    if (state.isFocused) {
                        scope.launch {
                            awaitFrame()     // espera medición con IME
                            delay(delayMs)   // amortigua "saltos"
                            requester.bringIntoView()
                        }
                    }
                }
            content(mod)
        }

        Box(
            modifier = modifier
                .padding(innerPadding)          // ✅ usa los insets del Scaffold
                .fillMaxSize()
                .dismissKeyboardOnTap()
                // ❌ evita imePadding aquí (duplica con bringIntoView / bottom bar)
                .onSizeChanged { containerHeight = it.height } // alto visible
        ) {
            // Contenido principal; medimos su altura real
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // ✅ Solo activamos scroll si de verdad se desborda
                    .then(if (needsScroll) Modifier.verticalScroll(scrollState) else Modifier)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .onSizeChanged { contentHeight = it.height }
                    // ✅ imePadding SOLO cuando NO hay scroll (sin bringIntoView)
                    .then(if (!needsScroll) Modifier.imePadding() else Modifier),
                horizontalAlignment = Alignment.Start
            ) {
                // Logo
                Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_beneficio_joven),
                        contentDescription = "",
                        modifier = Modifier.size(50.dp)
                    )
                }

                // Título
                Text(
                    "Inicia Sesión",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                        ),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = Modifier.padding(top = 18.dp, start = 6.dp, end = 6.dp, bottom = 14.dp)
                )

                // Subtítulo
                Text(
                    "Por favor, inicie sesión en su cuenta",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                // Label correo
                Text(
                    "Correo Electrónico",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    modifier = Modifier.padding(start = 6.dp, top = 32.dp, end = 6.dp, bottom = 8.dp)
                )

                // Campo correo (bringIntoView solo si hay scroll)
                FocusBringIntoView {
                    EmailTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = it
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                    )
                }

                // Label contraseña
                Text(
                    "Contraseña",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    modifier = Modifier.padding(start = 6.dp, top = 20.dp, end = 6.dp, bottom = 8.dp)
                )

                // Campo contraseña (bringIntoView solo si hay scroll)
                FocusBringIntoView {
                    PasswordTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = it
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                    )
                }

                // Recuérdame / ¿Olvidaste tu contraseña?
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text(
                            "Recuérdame",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7D7A7A)
                            )
                        )
                    }
                    TextButton(onClick = { nav.navigate(Screens.ForgotPassword.route) }) {
                        Text(
                            "¿Olvidaste tu contraseña?",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7D7A7A)
                            )
                        )
                    }
                }

                // Error
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
                                    viewModel.clearState()
                                }
                            ) {
                                Text("✕", color = Color(0xFFD32F2F))
                            }
                        }
                    }
                }

                // Botón primario
                MainButton(
                    text = if (authState.isLoading) "Iniciando sesión..." else "Inicia Sesión",
                    enabled = !authState.isLoading && email.isNotEmpty() && password.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    showError = false
                    viewModel.signIn(email, password)
                }

                GradientDivider_OR(
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Social buttons
                AltLoginButton(
                    text = "Continuar con Google",
                    icon = painterResource(id = R.drawable.logo_google),
                    contentDescription = "Continuar con Google",
                    onClick = { /* TODO: login Google */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                AltLoginButton(
                    text = "Continuar con Facebook",
                    icon = painterResource(id = R.drawable.logo_facebook),
                    contentDescription = "Continuar con Facebook",
                    onClick = { /* TODO: login Facebook */ },
                    modifier = Modifier.fillMaxWidth()
                )

                // ¿No tienes cuenta? Regístrate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "¿No tienes cuenta?  ",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7D7A7A)
                        )
                    )
                    TextButton(onClick = { nav.navigate(Screens.Register.route) }) {
                        Text(
                            "Regístrate",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF008D96)
                            )
                        )
                    }
                }

                // ❌ Ya no añadimos Spacer(navigationBarsPadding())
                // El Scaffold + safeDrawing y la bottom bar cubrirán ese espacio.
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLogin() {
    BeneficioJuventudTheme {
        val navController = rememberNavController()
        val fakeAppViewModel = AppViewModel()
        Login(nav = navController, appViewModel = fakeAppViewModel)
    }
}
