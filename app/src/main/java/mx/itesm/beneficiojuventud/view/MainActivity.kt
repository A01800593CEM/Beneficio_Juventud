package mx.itesm.beneficiojuventud.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.gson.Gson
import mx.itesm.beneficiojuventud.model.webhook.PromotionData
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel

/**
 * **MainActivity**
 * Actividad principal que inicializa el tema y muestra el flujo Compose de la app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        solicitarPermiso()
        obtenerToken()
        setContent {
            BeneficioJuventudTheme { AppContent() }
        }
    }
    private fun solicitarPermiso() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
// Habilitar funciones
        } else {
// Avisar que no habrá notificaciones
        }
    }
    private fun obtenerToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("Error al obtener el token: ${task.exception}")
                return@OnCompleteListener
            }
            val token = task.result
            println("FCM TOKEN: $token")
// Para suscribirse a cierto tema
            Firebase.messaging.subscribeToTopic("TarjetaJoven")
                .addOnCompleteListener { task ->
                    var msg = "Subscribed"
                    if (!task.isSuccessful) {
                        msg = "Subscribe failed"
                    }
                    println(msg)
                }
        })
    }
}

/**
 * Muestra el contenido principal según el estado de autenticación.
 */
@Composable
private fun AppContent(
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val nav = rememberNavController()
    AppNav(
        nav = nav,
        authViewModel = authViewModel,
        userViewModel = userViewModel
    )
}

/**
 * Controla la navegación principal de la aplicación SIN “flash”:
 * - No navegamos programáticamente tras el gate.
 * - Creamos el NavHost con el startDestination correcto y lo re-keyeamos cuando cambie.
 */
@Composable
private fun AppNav(
    nav: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val appState by authViewModel.appState.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val sessionKey by authViewModel.sessionKey.collectAsState()

    // Estado del perfil
    val userState by userViewModel.userState.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    // 1) En cuanto haya sesión (o cambie), asegúrate de tener el usuario actual de Cognito
    LaunchedEffect(appState.isAuthenticated, sessionKey) {
        if (appState.isAuthenticated) authViewModel.getCurrentUser()
    }

    // 2) Limpia el perfil apenas cambie la sesión (evita “fantasmas” de la cuenta anterior)
    LaunchedEffect(sessionKey) { userViewModel.clearUser() }

    // 3) Cuando ya tengamos el sub/cognitoId, ahora sí haz el fetch del perfil
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) userViewModel.getUserById(currentUserId!!)
    }

    // 4) Gate: perfil cargado SOLO si coincide con la sesión actual
    val isUserLoaded = remember(userState, isLoading, currentUserId) {
        !isLoading &&
                !currentUserId.isNullOrBlank() &&
                (userState.cognitoId == currentUserId)
    }

    // 5) Determinar startDestination sin navegar
    val start = when {
        !appState.hasCheckedAuth -> "splash"
        !appState.isAuthenticated -> Screens.LoginRegister.route
        isUserLoaded -> Screens.Home.route
        else -> "splash"
    }

    // 6) Re-crear NavHost cuando cambie la sesión o el destino inicial
    key(sessionKey, start) {
        NavHost(navController = nav, startDestination = start) {
            // Splash/loader interno (sin navegación programática)
            composable("splash") {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            }

            // --- Autenticación ---
            composable(Screens.LoginRegister.route) { LoginRegister(nav) }
            composable(Screens.Login.route) { Login(nav, authViewModel = authViewModel) }
            composable(Screens.Register.route) { Register(nav, authViewModel = authViewModel) }
            composable(Screens.ForgotPassword.route) { ForgotPassword(nav) }
            composable(Screens.RecoveryCode.route) { RecoveryCode(nav) }
            composable("recovery_code/{email}") { backStackEntry ->
                val email = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("email") ?: "",
                    "UTF-8"
                )
                RecoveryCode(nav, emailArg = email)
            }
            composable(Screens.ConfirmSignUp.route) { ConfirmSignUp(nav, "", authViewModel = authViewModel) }
            composable("confirm_signup/{email}") { backStackEntry ->
                val email = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("email") ?: "",
                    "UTF-8"
                )
                ConfirmSignUp(nav, email, authViewModel = authViewModel)
            }
            composable(Screens.NewPassword.route) { NewPassword(nav) }
            composable("new_password/{email}/{code}") { backStackEntry ->
                val email = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("email") ?: "",
                    "UTF-8"
                )
                val code = backStackEntry.arguments?.getString("code") ?: ""
                NewPassword(nav, emailArg = email, codeArg = code)
            }

            // --- Onboarding ---
            composable(Screens.Onboarding.route) { Onboarding(nav) }
            composable(Screens.OnboardingCategories.route) { OnboardingCategories(nav) }

            // --- App principal ---
            composable(Screens.Home.route) { Home(nav, userViewModel = userViewModel) }
            composable(Screens.Profile.route) { Profile(nav, authViewModel, userViewModel) }
            composable(Screens.EditProfile.route) { EditProfile(nav) }
            composable(Screens.History.route) { History(nav) }
            composable(Screens.Settings.route) { Settings(nav) }
            composable(Screens.Help.route) { Help(nav) }
            composable(Screens.Favorites.route) { Favorites(nav) }
            composable(Screens.Coupons.route) { Coupons(nav) }
            composable(Screens.Business.route) { Business(nav) }
            composable(Screens.GenerarPromocion.route) { GenerarPromocion(nav) }
            composable(Screens.GenerarPromocionIA.route) { GenerarPromocionIA(nav) }

            // --- Edición de promoción ---
            composable(Screens.EditPromotion.route) {
                val json = nav.previousBackStackEntry?.savedStateHandle?.get<String>("promotion_data")
                val promo = json?.let { parsePromotionDataFromJson(it) }
                EditPromotion(nav, promo)
            }

            composable(
                route = Screens.PromoQR.route,
                arguments = Screens.PromoQR.arguments
            ) { backStackEntry ->

                // Memoiza el argumento para que no cambie en recomposiciones
                val promotionId = remember(backStackEntry) {
                    backStackEntry.arguments?.getInt("promotionId")
                } ?: return@composable

                // Toma el cognitoId del AuthViewModel (StateFlow<String?>)
                val cognitoId by authViewModel.currentUserId.collectAsState()

                // Si aún no cargó, muestra loader breve (evita entrar con "")
                if (cognitoId.isNullOrBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    PromoQR(
                        nav = nav,
                        promotionId = promotionId,
                        cognitoId = cognitoId!!
                    )
                }
            }
        }
    }
}

/** Convierte una cadena JSON a un objeto [PromotionData]. */
private fun parsePromotionDataFromJson(json: String): PromotionData =
    Gson().fromJson(json, PromotionData::class.java)

/** Vista previa de [AppContent] dentro del tema BeneficioJuventud. */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    BeneficioJuventudTheme { AppContent() }
}
