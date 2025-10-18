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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.gson.Gson
import kotlinx.coroutines.delay
import mx.itesm.beneficiojuventud.model.webhook.PromotionData
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewcollab.HomeScreenCollab
import mx.itesm.beneficiojuventud.viewcollab.RegisterCollab


/**
 * MainActivity
 * Siempre inicia en "splash" y rutea limpio a LoginRegister o Home.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        solicitarPermiso()
        obtenerToken()
        setContent {
            BeneficioJuventudTheme {
                AppContent()
            }
        }
    }

    private fun solicitarPermiso() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* no-op */ }

    private fun obtenerToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("Error al obtener el token: ${task.exception}")
                return@OnCompleteListener
            }
            val token = task.result
            println("FCM TOKEN: $token")

            Firebase.messaging.subscribeToTopic("TarjetaJoven")
                .addOnCompleteListener { t ->
                    val msg = if (t.isSuccessful) "Subscribed" else "Subscribe failed"
                    println(msg)
                }
        })
    }
}

/** Arranque del árbol Compose con viewmodels compartidos */
@Composable
private fun AppContent(
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    val nav = rememberNavController()
    AppNav(
        nav = nav,
        authViewModel = authViewModel,
        userViewModel = userViewModel,
        collabViewModel = collabViewModel
    )
}

/**
 * Nav principal: siempre inicia en "splash".
 * No calcules startDestination dinámico; la lógica vive en StartupScreen().
 */
@Composable
private fun AppNav(
    nav: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    collabViewModel: CollabViewModel
) {
    val appState by authViewModel.appState.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val sessionKey by authViewModel.sessionKey.collectAsState()

    val userState by userViewModel.userState.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    LaunchedEffect(appState.isAuthenticated, sessionKey) {
        if (appState.isAuthenticated) authViewModel.getCurrentUser()
    }

    LaunchedEffect(sessionKey) {
        if (appState.isAuthenticated) {
            userViewModel.clearUser()
            val id = authViewModel.currentUserId.value
            if (!id.isNullOrBlank()) userViewModel.getUserById(id)
        } else {
            userViewModel.clearUser()
        }
    }

    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) userViewModel.getUserById(currentUserId!!)
    }

    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            StartupScreen(
                nav = nav,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
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
        composable(Screens.ConfirmSignUp.route) {
            ConfirmSignUp(nav, "", authViewModel = authViewModel)
        }
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
        composable(Screens.Home.route) {
            Home(nav, userViewModel = userViewModel, collabViewModel = collabViewModel)
        }
        composable(Screens.Profile.route) { Profile(nav, authViewModel, userViewModel) }
        composable(Screens.EditProfile.route) {
            EditProfile(nav = nav, authViewModel = authViewModel, userViewModel = userViewModel)
        }
        composable(Screens.History.route) { History(nav) }
        composable(Screens.Settings.route) { Settings(nav) }
        composable(Screens.Help.route) { Help(nav) }
        composable(Screens.Favorites.route) { Favorites(nav, userViewModel = userViewModel) }
        composable(Screens.Coupons.route) { Coupons(nav) }

        composable(Screens.GenerarPromocion.route) { GenerarPromocion(nav) }
        composable(Screens.GenerarPromocionIA.route) { GenerarPromocionIA(nav) }

        composable(Screens.EditPromotion.route) {
            val json = nav.previousBackStackEntry?.savedStateHandle?.get<String>("promotion_data")
            val promo = json?.let { parsePromotionDataFromJson(it) }
            EditPromotion(nav, promo)
        }

        composable(
            route = Screens.Business.route,
            arguments = Screens.Business.arguments
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("collabId") ?: return@composable
            val collabId = remember(encoded) { java.net.URLDecoder.decode(encoded, "UTF-8") }
            val cognitoId by authViewModel.currentUserId.collectAsState()
            if (cognitoId.isNullOrBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Business(
                    nav = nav,
                    collabId = collabId,
                    userCognitoId = cognitoId!!
                )
            }
        }

        composable(
            route = Screens.PromoQR.route, // "promoQR/{promotionId}"
            arguments = listOf(
                navArgument("promotionId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val promotionId = backStackEntry.arguments?.getInt("promotionId") ?: return@composable

            val cognitoId by authViewModel.currentUserId.collectAsState()
            if (!cognitoId.isNullOrBlank()) {
                PromoQR(nav, promotionId, cognitoId!!)
            } else {
                Startup()
            }
        }

        // App Colaborador
        composable(Screens.RegisterCollab.route) {
            RegisterCollab(
                nav = nav,
                authViewModel = authViewModel,
                collabViewModel = collabViewModel
            )
        }
        composable(Screens.HomeCollab.route) {
            HomeScreenCollab(
                nav = nav,
                authViewModel = authViewModel,
                collabViewModel = collabViewModel
            )
        }
    }
}

/**
 * Usa la pantalla Startup existente como splash durante la verificación de sesión.
 */
@Composable
private fun StartupScreen(
    nav: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val appState by authViewModel.appState.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val userState by userViewModel.userState.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    // refrescos
    LaunchedEffect(appState.isAuthenticated) {
        if (appState.isAuthenticated) authViewModel.getCurrentUser()
    }
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) userViewModel.getUserById(currentUserId!!)
    }

    val isUserLoaded = remember(userState, isLoading, currentUserId) {
        !isLoading && !currentUserId.isNullOrBlank() && userState.cognitoId == currentUserId
    }

    // Muestra tu pantalla de inicio (ya con logo y degradado)
    Startup(Modifier.fillMaxSize())

    // Lógica de espera/navegación
    LaunchedEffect(appState.hasCheckedAuth, appState.isAuthenticated, isUserLoaded) {
        val minSplash = 900L
        val maxWaitIfAuthed = 2500L
        val absoluteTimeout = 3500L
        val start = System.currentTimeMillis()

        while (!appState.hasCheckedAuth && System.currentTimeMillis() - start < absoluteTimeout) {
            delay(80)
        }

        val target = if (!appState.isAuthenticated) {
            Screens.LoginRegister.route
        } else {
            val deadline = start + maxWaitIfAuthed
            while (System.currentTimeMillis() < deadline && !isUserLoaded) {
                delay(120)
            }
            // Aquí deberiamos determinar si es un Colaborador o Usuario para navegar a Screens.Home.route o Screens.HomeCollab.route.
            // Por ahora, la lógica de `RegisterCollab` navega directo a `HomeCollab`, pero el login normal necesita esta lógica.
            Screens.Home.route
        }

        val elapsed = System.currentTimeMillis() - start
        if (elapsed < minSplash) delay(minSplash - elapsed)

        nav.navigate(target) {
            popUpTo("splash") { inclusive = true }
            launchSingleTop = true
        }
    }
}

/** Utilidad: JSON -> PromotionData */
private fun parsePromotionDataFromJson(json: String): PromotionData =
    Gson().fromJson(json, PromotionData::class.java)