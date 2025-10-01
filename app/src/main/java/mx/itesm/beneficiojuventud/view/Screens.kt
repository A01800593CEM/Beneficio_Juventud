package mx.itesm.beneficiojuventud.view

sealed class Screens(val route: String) {
    data object Login : Screens("login")
    data object Register : Screens("register")
    data object MainMenu : Screens("main_menu")
    data object ForgotPassword : Screens("forgot_password")
}