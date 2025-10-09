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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
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

    // ---------- Auto-scroll al enfocar ----------
    val scope = rememberCoroutineScope()
    val bringEmail = remember { BringIntoViewRequester() }
    val bringPass  = remember { BringIntoViewRequester() }

    val topPadding = rememberResponsiveTopPadding()

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

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .dismissKeyboardOnTap()
                .imePadding(),
            contentPadding = PaddingValues(
                start = 24.dp, end = 24.dp,
                top = topPadding,
                bottom = 32.dp
            ),
        ) {
            item {
                Box(modifier = modifier.padding(horizontal = 30.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_beneficio_joven),
                        contentDescription = "",
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            item {
                Text(
                    "Inicia Sesión",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF4B4C7E), Color(0xFF008D96))
                        ),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    modifier = modifier.padding(24.dp, 18.dp, 20.dp, 14.dp)
                )
            }

            item {
                Text(
                    "Por favor, inicie sesión en su cuenta",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    ),
                    textAlign = TextAlign.Start,
                    modifier = modifier.padding(horizontal = 24.dp)
                )
            }

            // Label correo
            item {
                Text(
                    "Correo Electrónico",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    modifier = modifier.padding(24.dp, 32.dp, 24.dp, 8.dp)
                )
            }

            // Campo correo con auto-scroll al enfocar
            item {
                EmailTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .bringIntoViewRequester(bringEmail)
                        .onFocusEvent {
                            if (it.isFocused) {
                                scope.launch { bringEmail.bringIntoView() }
                            }
                        }
                )
            }

            // Label contraseña
            item {
                Text(
                    "Contraseña",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7D7A7A)
                    ),
                    modifier = modifier.padding(24.dp, 20.dp, 24.dp, 8.dp)
                )
            }

            // Campo contraseña con auto-scroll al enfocar
            item {
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .bringIntoViewRequester(bringPass)
                        .onFocusEvent {
                            if (it.isFocused) {
                                scope.launch { bringPass.bringIntoView() }
                            }
                        }
                )
            }

            // Recuérdame / ¿Olvidaste tu contraseña?
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
            }

            // Mostrar error si existe
            if (showError && errorMessage.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
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
            }

            // Botón primario
            item {
                MainButton(
                    text = if (authState.isLoading) "Iniciando sesión..." else "Inicia Sesión",
                    enabled = !authState.isLoading && email.isNotEmpty() && password.isNotEmpty(),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    showError = false
                    viewModel.signIn(email, password)
                }
            }

            item {
                GradientDivider_OR(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Social buttons
            item {
                AltLoginButton(
                    text = "Continuar con Google",
                    icon = painterResource(id = R.drawable.logo_google),
                    contentDescription = "Continuar con Google",
                    onClick = { /* TODO: login Google */ },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }

            item {
                AltLoginButton(
                    text = "Continuar con Facebook",
                    icon = painterResource(id = R.drawable.logo_facebook),
                    contentDescription = "Continuar con Facebook",
                    onClick = { /* TODO: login Facebook */ },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // ¿No tienes cuenta? Regístrate
            item {
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
            }

            // Espaciador inferior para no chocar con la barra de gestos
            item {
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}


@Composable
fun rememberResponsiveTopPadding(): Dp {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val proportionalPadding = (screenHeight * 0.8f).dp
    return proportionalPadding.coerceIn(24.dp, 75.dp)
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
