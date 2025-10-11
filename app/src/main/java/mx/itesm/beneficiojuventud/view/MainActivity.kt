package mx.itesm.beneficiojuventud.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeneficioJuventudTheme {
                AppContent()
            }
        }
    }
}

@Composable
private fun AppContent(authViewModel: AuthViewModel = viewModel()) {
    val appState by authViewModel.appState.collectAsState()

    when {
        appState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        appState.hasCheckedAuth -> {
            val startDestination = if (appState.isAuthenticated) {
                Screens.Home.route
            } else {
                Screens.LoginRegister.route
            }
            AppNav(startDestination = startDestination, authViewModel = authViewModel)
        }
    }
}

@Composable
private fun AppNav(startDestination: String, authViewModel: AuthViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = startDestination) {
        composable(Screens.LoginRegister.route) { LoginRegister(nav) }
        composable(Screens.Login.route) { Login(nav = nav, authViewModel = authViewModel) }
        composable(Screens.Register.route) { Register(nav, authViewModel = authViewModel) }
        composable(Screens.ForgotPassword.route) { ForgotPassword(nav) }
        composable(Screens.RecoveryCode.route) { RecoveryCode(nav) }
        composable("recovery_code/{email}") { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = java.net.URLDecoder.decode(encodedEmail, "UTF-8")
            RecoveryCode(nav, emailArg = email)
        }
        composable(Screens.ConfirmSignUp.route) { ConfirmSignUp(nav, "", authViewModel = authViewModel) }
        composable("confirm_signup/{email}") { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = java.net.URLDecoder.decode(encodedEmail, "UTF-8")
            ConfirmSignUp(nav, email, authViewModel = authViewModel)
        }
        composable(Screens.NewPassword.route) { NewPassword(nav) }
        composable("new_password/{email}/{code}") { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = java.net.URLDecoder.decode(encodedEmail, "UTF-8")
            val code = backStackEntry.arguments?.getString("code") ?: ""
            NewPassword(nav, emailArg = email, codeArg = code)
        }
        composable(Screens.Onboarding.route) { Onboarding(nav) }
        composable(Screens.OnboardingCategories.route) { OnboardingCategories(nav) }
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    BeneficioJuventudTheme {
        AppContent()
    }
}
