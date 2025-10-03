package mx.itesm.beneficiojuventud.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
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
                AppNav()
            }
        }
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Screens.MainMenu.route) {
        composable(Screens.MainMenu.route) { MainMenu(nav) }
        composable(Screens.Login.route) { Login(nav) }
        composable(Screens.Register.route) { Register(nav) }
        composable(Screens.ForgotPassword.route) { ForgotPassword(nav)}
        composable(Screens.RecoveryCode.route) { RecoveryCode(nav) }
        composable (Screens.NewPassword.route){ NewPassword(nav) }

    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    BeneficioJuventudTheme {
        AppNav()
    }
}