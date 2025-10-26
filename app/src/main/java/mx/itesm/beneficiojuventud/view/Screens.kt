package mx.itesm.beneficiojuventud.view

import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder

sealed class Screens(val route: String) {
    data object Login : Screens("login")
    data object Register : Screens("register")
    data object GoogleRegister : Screens("google_register")
    data object LoginRegister : Screens("login_register")
    data object ForgotPassword : Screens("forgot_password")
    data object RecoveryCode : Screens("recovery_code")
    data object ConfirmSignUp : Screens("confirm_signup")
    data object NewPassword : Screens("new_password")
    data object PostLoginPermissions : Screens("post_login_permissions")
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
    data object Credits : Screens("credits")
    data object Favorites : Screens("favorites")
    data object Coupons : Screens("coupons")
    data object FullscreenMap : Screens("fullscreen_map")
    data object Business : Screens("business/{collabId}") {
        fun createRoute(collabId: String) =
            "business/${java.net.URLEncoder.encode(collabId, "UTF-8")}"
        val arguments = listOf(navArgument("collabId") { type = NavType.StringType })
    }

    data object PromoQR: Screens("promoQR/{promotionId}") {
        fun createRoute(promotionId: Int) = "promoQR/$promotionId"
        val arguments = listOf(navArgument("promotionId") { type = NavType.IntType })
    }
    data object GenerarPromocion : Screens("generar_promocion")
    data object GenerarPromocionIA : Screens("generar_promocion_ia")
    data object EditPromotion : Screens("edit_promotion")

    // Panel de Colaboradres
    data object RegisterCollab : Screens("register_collab")
    data object HomeScreenCollab : Screens("home_screen_collab")
    data object QrScanner : Screens("qr_scanner/{branchId}") {
        fun createRoute(branchId: Int) = "qr_scanner/$branchId"
        val arguments = listOf(navArgument("branchId") { type = NavType.IntType })
    }
    data object ProfileCollab : Screens("profile_collab")
    data object StatsScreen : Screens("stats_screen")
    data object PromotionsScreen : Screens("promotions_screen/{collabId}") {
        fun createRoute(collabId: String) =
            "promotions_screen/${java.net.URLEncoder.encode(collabId, "UTF-8")}"
        val arg = "collabId"
    }
    data object Terms : Screens("terms")
    data object Status : Screens("status/{type}/{destination}") {
        fun createRoute(type: StatusType, destination: String): String =
            "status/${type.name}/${java.net.URLEncoder.encode(destination, "UTF-8")}"
        val arguments = listOf(
            navArgument("type") { type = NavType.StringType },
            navArgument("destination") { type = NavType.StringType }
        )
    }

    data object GeneratePromotionScreen : Screens("generate_promotion_screen")

    data object EditProfileCollab : Screens("edit_profile_collab")
    data object BranchManagement : Screens("branch_management")
    data object StatusCollabSignup : Screens("status_collab_signup")
    data object SettingsCollab : Screens("settings_collab")
    data object QRConfirmation : Screens("qr_confirmation/{userName}/{promotionTitle}/{collaboratorName}/{promotionId}/{userId}/{branchId}/{nonce}/{qrTimestamp}") {
        fun createRoute(
            userName: String,
            promotionTitle: String,
            collaboratorName: String,
            promotionId: Int,
            userId: String,
            branchId: Int,
            nonce: String,
            qrTimestamp: Long
        ): String {
            return "qr_confirmation/${java.net.URLEncoder.encode(userName, "UTF-8")}/${java.net.URLEncoder.encode(promotionTitle, "UTF-8")}/${java.net.URLEncoder.encode(collaboratorName, "UTF-8")}/$promotionId/${java.net.URLEncoder.encode(userId, "UTF-8")}/$branchId/${java.net.URLEncoder.encode(nonce, "UTF-8")}/$qrTimestamp"
        }
        val arguments = listOf(
            navArgument("userName") { type = NavType.StringType },
            navArgument("promotionTitle") { type = NavType.StringType },
            navArgument("collaboratorName") { type = NavType.StringType },
            navArgument("promotionId") { type = NavType.IntType },
            navArgument("userId") { type = NavType.StringType },
            navArgument("branchId") { type = NavType.IntType },
            navArgument("nonce") { type = NavType.StringType },
            navArgument("qrTimestamp") { type = NavType.LongType }
        )
    }

}