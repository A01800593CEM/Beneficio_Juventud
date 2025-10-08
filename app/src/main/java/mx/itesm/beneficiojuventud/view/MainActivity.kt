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
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AppViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


// De León working on this

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
private fun AppContent(appViewModel: AppViewModel = viewModel()) {
    val appState by appViewModel.appState.collectAsState()

    when {
        appState.isLoading -> {
            // Mostrar loading mientras verificamos la autenticación
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        appState.hasCheckedAuth -> {
            // Determinar la pantalla inicial basada en el estado de autenticación
            val startDestination = if (appState.isAuthenticated) {
                Screens.Onboarding.route
            } else {
                Screens.LoginRegister.route
            }

            AppNav(startDestination = startDestination, appViewModel = appViewModel)
        }
    }
}

@Composable
private fun AppNav(startDestination: String, appViewModel: AppViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Screens.Home.route) {
        composable(Screens.LoginRegister.route) { LoginRegister(nav) }
        composable(Screens.Login.route) { Login(nav, appViewModel) }
        composable(Screens.Register.route) { Register(nav, appViewModel = appViewModel) }
        composable(Screens.ForgotPassword.route) { ForgotPassword(nav)}
        composable(Screens.RecoveryCode.route) { RecoveryCode(nav) }
        composable("recovery_code/{email}") { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = java.net.URLDecoder.decode(encodedEmail, "UTF-8")
            RecoveryCode(nav, emailArg = email)
        }
        composable(Screens.ConfirmSignUp.route) { ConfirmSignUp(nav, "") }
        composable("confirm_signup/{email}") { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = java.net.URLDecoder.decode(encodedEmail, "UTF-8")
            ConfirmSignUp(nav, email, appViewModel = appViewModel)
        }
        composable(Screens.NewPassword.route){ NewPassword(nav) }
        composable("new_password/{email}/{code}") { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = java.net.URLDecoder.decode(encodedEmail, "UTF-8")
            val code = backStackEntry.arguments?.getString("code") ?: ""
            NewPassword(nav, emailArg = email, codeArg = code)
        }
        composable(Screens.Onboarding.route){ Onboarding(nav, appViewModel) }
        composable(Screens.OnboardingCategories.route){ OnboardingCategories(nav) }
        composable(Screens.Home.route){ Home(nav) }
        composable(Screens.Profile.route){ Profile(nav) }
        composable(Screens.EditProfile.route){ EditProfile(nav) }
        composable(Screens.History.route){ History(nav) }
        composable(Screens.Settings.route){ Settings(nav) }
        composable(Screens.Help.route){ Help(nav) }

    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    BeneficioJuventudTheme {
        AppContent()
    }
}