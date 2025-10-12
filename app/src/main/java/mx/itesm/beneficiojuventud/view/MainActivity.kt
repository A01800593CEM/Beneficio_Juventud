package mx.itesm.beneficiojuventud.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import mx.itesm.beneficiojuventud.model.webhook.PromotionData
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel

/**
 * **MainActivity**
 * Actividad principal que inicializa el tema y muestra el flujo Compose de la app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeneficioJuventudTheme { AppContent() }
        }
    }
}

/**
 * Muestra el contenido principal según el estado de autenticación.
 * @param authViewModel ViewModel que gestiona el estado de sesión.
 */
@Composable
private fun AppContent(authViewModel: AuthViewModel = viewModel()) {
    val nav = rememberNavController() // <-- NavController estable
    AppNav(nav = nav, authViewModel = authViewModel)
}



/**
 * Controla la navegación principal de la aplicación usando una ruta neutral "launcher"
 * que decide imperativamente a qué pantalla ir y limpia el back stack.
 */
@Composable
private fun AppNav(nav: NavHostController, authViewModel: AuthViewModel) {
    val appState by authViewModel.appState.collectAsState()

    NavHost(
        navController = nav,
        startDestination = "launcher"
    ) {
        composable("launcher") {
            // ➊ Evita decidir más de una vez por arranque
            var decided by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(appState.hasCheckedAuth, appState.isAuthenticated) {
                if (decided || !appState.hasCheckedAuth) return@LaunchedEffect
                decided = true

                if (appState.isAuthenticated) {
                    nav.navigate(Screens.Home.route) {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    nav.navigate(Screens.LoginRegister.route) {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            // ➋ Loader mientras aún no sabemos el estado:
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
                backStackEntry.arguments?.getString("email") ?: "", "UTF-8"
            )
            RecoveryCode(nav, emailArg = email)
        }

        composable(Screens.ConfirmSignUp.route) { ConfirmSignUp(nav, "", authViewModel = authViewModel) }
        composable("confirm_signup/{email}") { backStackEntry ->
            val email = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("email") ?: "", "UTF-8"
            )
            ConfirmSignUp(nav, email, authViewModel = authViewModel)
        }

        composable(Screens.NewPassword.route) { NewPassword(nav) }
        composable("new_password/{email}/{code}") { backStackEntry ->
            val email = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("email") ?: "", "UTF-8"
            )
            val code = backStackEntry.arguments?.getString("code") ?: ""
            NewPassword(nav, emailArg = email, codeArg = code)
        }

        // --- Onboarding ---
        composable(Screens.Onboarding.route) { Onboarding(nav) }
        composable(Screens.OnboardingCategories.route) { OnboardingCategories(nav) }

        // --- Principales ---
        composable(Screens.Home.route) { Home(nav) }
        composable(Screens.Profile.route) { Profile(nav) }
        composable(Screens.EditProfile.route) { EditProfile(nav) }
        composable(Screens.History.route) { History(nav) }
        composable(Screens.Settings.route) { Settings(nav) }
        composable(Screens.Help.route) { Help(nav) }
        composable(Screens.Favorites.route) { Favorites(nav) }
        composable(Screens.Coupons.route) { Coupons(nav) }
        composable(Screens.Business.route) { Business(nav) }
        composable(Screens.PromoQR.route) { PromoQR(nav) }
        composable(Screens.GenerarPromocion.route) { GenerarPromocion(nav) }
        composable(Screens.GenerarPromocionIA.route) { GenerarPromocionIA(nav) }

        // --- Edición de promoción ---
        composable(Screens.EditPromotion.route) {
            val json = nav.previousBackStackEntry?.savedStateHandle?.get<String>("promotion_data")
            val promo = json?.let { parsePromotionDataFromJson(it) }
            EditPromotion(nav, promo)
        }
    }
}

/**
 * Convierte una cadena JSON a un objeto [PromotionData].
 */
private fun parsePromotionDataFromJson(json: String): PromotionData =
    Gson().fromJson(json, PromotionData::class.java)

/**
 * Convierte un objeto [PromotionData] a una cadena JSON.
 */
private fun promotionDataToJson(promotionData: PromotionData): String =
    Gson().toJson(promotionData)

/**
 * Vista previa de [AppContent] dentro del tema BeneficioJuventud.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    BeneficioJuventudTheme { AppContent() }
}
