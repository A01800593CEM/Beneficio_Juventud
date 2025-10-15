package mx.itesm.beneficiojuventud.view

import androidx.navigation.NavType
import androidx.navigation.navArgument

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
    data object Home : Screens("home")
    data object Profile : Screens("profile")
    data object EditProfile : Screens("edit_profile")
    data object History : Screens("history")
    data object Settings : Screens("settings")
    data object Help : Screens("help")
    data object Favorites : Screens("favorites")
    data object Coupons : Screens("coupons")
    data object Business : Screens("business")
    data object PromoQR: Screens("promoQR/{promotionId}") {
        fun createRoute(promotionId: Int) = "promoQR/$promotionId"
        val arguments = listOf(navArgument("promotionId") { type = NavType.IntType })
    }
    data object GenerarPromocion : Screens("generar_promocion")
    data object GenerarPromocionIA : Screens("generar_promocion_ia")
    data object EditPromotion : Screens("edit_promotion")

}