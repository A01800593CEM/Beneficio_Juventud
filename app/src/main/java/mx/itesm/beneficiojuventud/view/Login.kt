package mx.itesm.beneficiojuventud.view

import android.annotation.SuppressLint
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

@Composable
fun Login(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            nav.navigate(Screens.Onboarding.route) {
                popUpTo(Screens.LoginRegister.route) { inclusive = true }
            }
            authViewModel.clearState()
        }
    }
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            errorMessage = error
            showError = true
        }
    }

    // Desplaza el campo enfocado para que no lo tape el teclado
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // control total de insets
        topBar = { LoginHeader() } // Header fijo (logo + títulos)
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dismissKeyboardOnTap()
                // Ajusta el contenido cuando aparece el teclado y respeta barras del sistema
                .windowInsetsPadding(WindowInsets.ime)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            LazyColumn(
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // -------- Formulario ----------
                item {
                    Text(
                        "Correo Electrónico",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7D7A7A)
                        ),
                        modifier = Modifier.padding(start = 6.dp, top = 8.dp, end = 6.dp, bottom = 8.dp)
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
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7D7A7A)
                        ),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                            Text(
                                "Recuérdame",
                                style = TextStyle(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7D7A7A)
                                )
                            )
                        }
                        TextButton(onClick = { nav.navigate(Screens.ForgotPassword.route) }) {
                            Text(
                                "¿Olvidaste tu contraseña?",
                                style = TextStyle(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7D7A7A)
                                )
                            )
                        }
                    }
                }

                // --------- Botón principal en la posición del diseño (no en bottomBar) ----------
                item {
                    MainButton(
                        text = if (authState.isLoading) "Iniciando sesión..." else "Inicia Sesión",
                        enabled = !authState.isLoading && email.isNotEmpty() && password.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp)
                    ) {
                        showError = false
                        authViewModel.signIn(email, password)
                    }
                }

                if (showError && errorMessage.isNotEmpty()) {
                    item {
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
                                IconButton(onClick = {
                                    showError = false
                                    errorMessage = ""
                                    authViewModel.clearState()
                                }) { Text("✕", color = Color(0xFFD32F2F)) }
                            }
                        }
                    }
                }

                item { GradientDivider_OR(modifier = Modifier.padding(vertical = 16.dp)) }

                // --------- Social logins ----------
                item {
                    AltLoginButton(
                        text = "Continuar con Google",
                        icon = painterResource(id = R.drawable.logo_google),
                        contentDescription = "Continuar con Google",
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
                item {
                    AltLoginButton(
                        text = "Continuar con Facebook",
                        icon = painterResource(id = R.drawable.logo_facebook),
                        contentDescription = "Continuar con Facebook",
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --------- Registro (al final del scroll) ----------
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp),
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
                }
            }
        }
    }
}

@Composable
private fun LoginHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_beneficio_joven),
            contentDescription = null,
            modifier = Modifier.size(50.dp).padding(horizontal = 6.dp)
        )
        Text(
            "Inicia Sesión",
            style = TextStyle(
                brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            ),
            modifier = Modifier.padding(top = 18.dp, start = 6.dp, end = 6.dp, bottom = 14.dp)
        )
        Text(
            "Por favor, inicie sesión en su cuenta",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161)),
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLogin() {
    BeneficioJuventudTheme {
        val navController = rememberNavController()
        val fakeAppViewModel = AuthViewModel()
        Login(nav = navController, authViewModel = fakeAppViewModel)
    }
}
