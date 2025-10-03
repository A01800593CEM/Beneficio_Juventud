package mx.itesm.beneficiojuventud.view

sealed class Screens(val route: String) {
    data object Login : Screens("login")
    data object Register : Screens("register")
    data object LoginRegister : Screens("login_register")
    data object ForgotPassword : Screens("forgot_password")
    data object RecoveryCode : Screens("recovery_code")
    data object NewPassword : Screens("new_password")
    data object Onboarding : Screens("onboarding")

    // Rutas con parámetros
    companion object {
        fun recoveryCodeWithEmail(email: String) = "recovery_code/$email"
        fun newPasswordWithEmailAndCode(email: String, code: String) = "new_password/$email/$code"
    }
    data object OnboardingCategories : Screens("onboarding_categories")
    data object MainMenu : Screens("main_menu")
}