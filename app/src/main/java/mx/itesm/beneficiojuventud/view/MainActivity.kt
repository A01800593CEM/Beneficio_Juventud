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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.gson.Gson
import kotlinx.coroutines.delay
import mx.itesm.beneficiojuventud.model.RoomDB.LocalDatabase
import mx.itesm.beneficiojuventud.model.webhook.PromotionData
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewcollab.EditProfileCollab
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CategoryViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewcollab.HomeScreenCollab
import mx.itesm.beneficiojuventud.viewcollab.ProfileCollab
import mx.itesm.beneficiojuventud.viewcollab.RegisterCollab
import mx.itesm.beneficiojuventud.viewcollab.StatsScreen
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import mx.itesm.beneficiojuventud.viewcollab.PromotionsScreenCollab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val localDatabase = Room.databaseBuilder(
            applicationContext,
            LocalDatabase::class.java,
            "beneficio-joven-db"
        ).build()
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

@Composable
private fun AppContent(
    userViewModel: UserViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    promoViewModel: PromoViewModel = viewModel()
) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel(context) }
    val nav = rememberNavController()
    AppNav(
        nav = nav,
        authViewModel = authViewModel,
        userViewModel = userViewModel,
        collabViewModel = collabViewModel,
        categoryViewModel = categoryViewModel,
        promoViewModel = promoViewModel
    )
}

@Composable
private fun AppNav(
    nav: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    collabViewModel: CollabViewModel,
    categoryViewModel: CategoryViewModel,
    promoViewModel: PromoViewModel
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
            collabViewModel.clearCollaborator()
            val id = authViewModel.currentUserId.value
            if (!id.isNullOrBlank()) {
                userViewModel.getUserById(id)
                try { collabViewModel.getCollaboratorById(id) } catch (_: Exception) {}
            }
        } else {
            userViewModel.clearUser()
            collabViewModel.clearCollaborator()
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
                userViewModel = userViewModel,
                collabViewModel = collabViewModel
            )
        }

        // --- Auth ---
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
        composable(Screens.OnboardingCategories.route) {
            OnboardingCategories(
                nav,
                categoryViewModel = categoryViewModel,
                userViewModel = userViewModel
            )
        }

        // --- App Usuario ---
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
                    userCognitoId = cognitoId!!,
                    promoViewModel = promoViewModel,
                    collabViewModel = collabViewModel,
                    userViewModel = userViewModel
                )
            }
        }

        composable(
            route = Screens.PromoQR.route,
            arguments = listOf(navArgument("promotionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val promotionId = backStackEntry.arguments?.getInt("promotionId") ?: return@composable
            val cognitoId by authViewModel.currentUserId.collectAsState()
            if (!cognitoId.isNullOrBlank()) {
                PromoQR(nav, promotionId, cognitoId!!)
            } else {
                Startup()
            }
        }

        // --- App Colaborador ---
        composable(Screens.RegisterCollab.route) {
            RegisterCollab(
                nav = nav,
                authViewModel = authViewModel,
                collabViewModel = collabViewModel
            )
        }
        composable(Screens.HomeScreenCollab.route) {
            HomeScreenCollab(
                nav = nav,
                authViewModel = authViewModel,
                collabViewModel = collabViewModel
            )
        }
        composable(Screens.ProfileCollab.route) { ProfileCollab(nav = nav, collabViewModel = collabViewModel) }
        composable(Screens.QrScanner.route) {
            QrScannerScreen(
                onClose = { nav.popBackStack() },
                onResult = { text ->
                    nav.previousBackStackEntry?.savedStateHandle?.set("qr_result", text)
                    nav.popBackStack()
                }
            )
        }
        composable(Screens.StatsScreen.route) { StatsScreen(nav) }
        composable(Screens.PromotionsScreen.route) {
            val collabId by authViewModel.currentUserId.collectAsState()
            PromotionsScreenCollab(
                nav = nav,
                collabId = collabId.orEmpty(),
                promoViewModel = promoViewModel,
                onEditPromotion = { id -> nav.navigate(Screens.EditPromotion.route) },
                onCreatePromotion = { nav.navigate(Screens.GenerarPromocion.route) }
            )
        }
        composable(Screens.EditProfileCollab.route) { EditProfileCollab(nav) }

        // Status Screen
        composable(
            route = Screens.Status.route,
            arguments = Screens.Status.arguments
        ) { backStackEntry ->
            val typeString = backStackEntry.arguments?.getString("type") ?: return@composable
            val destination = backStackEntry.arguments?.getString("destination") ?: return@composable

            val statusType = try {
                StatusType.valueOf(typeString)
            } catch (e: IllegalArgumentException) {
                StatusType.VERIFICATION_ERROR
            }

            StatusScreen(
                nav = nav,
                statusType = statusType,
                destinationRoute = java.net.URLDecoder.decode(destination, "UTF-8")
            )
        }
    }
}

@Composable
private fun StartupScreen(
    nav: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    collabViewModel: CollabViewModel
) {
    val appState by authViewModel.appState.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()

    val userState by userViewModel.userState.collectAsState()
    val isLoadingUser by userViewModel.isLoading.collectAsState()

    val collabState by collabViewModel.collabState.collectAsState()

    // Intentar auto-login si hay credenciales guardadas
    var autoLoginAttempted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!autoLoginAttempted) {
            autoLoginAttempted = true
            authViewModel.tryAutoLogin()
        }
    }

    // refrescos
    LaunchedEffect(appState.isAuthenticated) {
        if (appState.isAuthenticated) authViewModel.getCurrentUser()
    }
    LaunchedEffect(currentUserId) {
        val id = currentUserId
        if (!id.isNullOrBlank()) {
            // Limpiar ambos estados primero
            userViewModel.clearUser()
            collabViewModel.clearCollaborator()
            // Cargar ambos perfiles en paralelo
            userViewModel.getUserById(id)
            try { collabViewModel.getCollaboratorById(id) } catch (_: Exception) {}
        } else {
            userViewModel.clearUser()
            collabViewModel.clearCollaborator()
        }
    }

    val isUser = !userState.cognitoId.isNullOrBlank()
    val isCollab = !collabState.cognitoId.isNullOrBlank()

    Startup(Modifier.fillMaxSize())

    LaunchedEffect(appState.hasCheckedAuth, appState.isAuthenticated, isUser, isCollab, isLoadingUser) {
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
            while (System.currentTimeMillis() < deadline && !(isUser || isCollab || !isLoadingUser)) {
                delay(120)
            }
            when {
                isCollab -> Screens.HomeScreenCollab.route
                isUser   -> Screens.Home.route
                else     -> Screens.Home.route
            }
        }

        val elapsed = System.currentTimeMillis() - start
        if (elapsed < minSplash) delay(minSplash - elapsed)

        nav.navigate(target) {
            popUpTo("splash") { inclusive = true }
            launchSingleTop = true
        }
    }
}

private fun parsePromotionDataFromJson(json: String): mx.itesm.beneficiojuventud.model.webhook.PromotionData =
    Gson().fromJson(json, PromotionData::class.java)
