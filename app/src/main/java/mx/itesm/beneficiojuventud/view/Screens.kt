package mx.itesm.beneficiojuventud.view

sealed class Screens(val route: String) {
    data object Login : Screens("login")
    data object Register : Screens("register")
    data object LoginRegister : Screens("login_register")
    data object ForgotPassword : Screens("forgot_password")
    data object RecoveryCode : Screens("recovery_code")
    data object ConfirmSignUp : Screens("confirm_signup")
    data object NewPassword : Screens("new_password")
    data object Onboarding : Screens("onboarding")

    // Rutas con parámetros
    companion object {
        fun recoveryCodeWithEmail(email: String) = "recovery_code/${java.net.URLEncoder.encode(email, "UTF-8")}"
        fun confirmSignUpWithEmail(email: String) = "confirm_signup/${java.net.URLEncoder.encode(email, "UTF-8")}"
        fun newPasswordWithEmailAndCode(email: String, code: String) = "new_password/${java.net.URLEncoder.encode(email, "UTF-8")}/$code"
    }
    data object OnboardingCategories : Screens("onboarding_categories")
    data object MainMenu : Screens("main_menu")
    data object Profile : Screens("profile")
}