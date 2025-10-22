package mx.itesm.beneficiojuventud.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.amplifyframework.core.Amplify
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.gson.Gson
import kotlinx.coroutines.delay
import mx.itesm.beneficiojuventud.model.RoomDB.LocalDatabase
import mx.itesm.beneficiojuventud.model.webhook.PromotionData
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewcollab.QRScannerScreen
import mx.itesm.beneficiojuventud.viewcollab.EditProfileCollab
import mx.itesm.beneficiojuventud.viewcollab.GeneratePromotionScreen
import mx.itesm.beneficiojuventud.viewcollab.BranchManagementScreen
import mx.itesm.beneficiojuventud.viewcollab.StatusCollabSignup
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.BookingViewModel
import mx.itesm.beneficiojuventud.viewmodel.CategoryViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewcollab.HomeScreenCollab
import mx.itesm.beneficiojuventud.viewcollab.ProfileCollab
import mx.itesm.beneficiojuventud.viewcollab.RegisterCollab
import mx.itesm.beneficiojuventud.viewcollab.StatsScreen
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import mx.itesm.beneficiojuventud.viewcollab.PromotionsScreenCollab
import mx.itesm.beneficiojuventud.viewmodel.StatsViewModel

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private var hasProcessedOAuthIntent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = LocalDatabase.getDatabase(this)
        val promotionDao = db.promotionDao()
        val categoryDao = db.categoryDao()

        enableEdgeToEdge()
        // Los permisos y token ahora se manejan en PostLoginPermissions
        setContent {
            BeneficioJuventudTheme {
                AppContent(
                    promotionDao = promotionDao,
                    categoryDao = categoryDao
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "🔵 onNewIntent llamado")
        // CRÍTICO: Actualizar el intent de la activity
        setIntent(intent)

        // Resetear el flag cuando llega un nuevo intent
        hasProcessedOAuthIntent = false
        handleOAuthRedirect(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔵 onResume llamado")

        // Solo procesar si no se ha procesado ya
        if (!hasProcessedOAuthIntent) {
            handleOAuthRedirect(intent)
        }
    }

    private fun handleOAuthRedirect(intent: Intent?) {
        if (intent?.data != null && intent.data?.scheme == "beneficiojoven") {
            // Evitar procesar el mismo intent múltiples veces
            if (hasProcessedOAuthIntent) {
                Log.d(TAG, "⚠️ Intent OAuth ya procesado, saltando...")
                return
            }

            Log.d(TAG, "🔵 Intent de OAuth detectado: ${intent.data}")
            Log.d(TAG, "Scheme: ${intent.data?.scheme}")
            Log.d(TAG, "Host: ${intent.data?.host}")
            Log.d(TAG, "Full URI: ${intent.data}")

            // Extraer parámetros para logging
            val code = intent.data?.getQueryParameter("code")
            val state = intent.data?.getQueryParameter("state")
            val error = intent.data?.getQueryParameter("error")

            if (error != null) {
                Log.e(TAG, "❌ OAuth error recibido: $error")
                Log.e(TAG, "Error description: ${intent.data?.getQueryParameter("error_description")}")
            } else {
                Log.d(TAG, "Code presente: ${!code.isNullOrBlank()}")
                Log.d(TAG, "State presente: ${!state.isNullOrBlank()}")
            }

            // Marcar como procesado ANTES de llamar a Amplify
            hasProcessedOAuthIntent = true

            // Pasar el intent a Amplify para que complete el flujo OAuth
            try {
                Log.d(TAG, "⏳ Procesando OAuth con Amplify...")
                Amplify.Auth.handleWebUISignInResponse(intent)
                Log.d(TAG, "✅ Intent pasado a Amplify.Auth.handleWebUISignInResponse")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al pasar intent a Amplify: ${e.message}", e)
                Log.e(TAG, "Exception class: ${e.javaClass.simpleName}")
                Log.e(TAG, "Stack trace:", e)
                // Resetear el flag en caso de error para permitir retry
                hasProcessedOAuthIntent = false
            }
        } else {
            if (intent?.data != null) {
                Log.d(TAG, "Intent con data pero scheme diferente: ${intent.data?.scheme}")
            } else {
                Log.d(TAG, "Intent sin data (normal app launch)")
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
    promotionDao: mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionDao,
    categoryDao: mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryDao
) {
    val context = LocalContext.current
    val db = LocalDatabase.getDatabase(context)

    // Create repository with the DAOs
    val repository = remember(promotionDao) {
        mx.itesm.beneficiojuventud.model.SavedCouponRepository(
            promotionDao = promotionDao,
            bookingDao = db.bookingDao()
        )
    }

    // Create ViewModels
    val authViewModel = remember { AuthViewModel(context) }
    val userViewModel = remember(repository) {
        UserViewModel(repository = repository)
    }
    val collabViewModel: CollabViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val promoViewModel: PromoViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    val statsViewModel: StatsViewModel = viewModel()

    val nav = rememberNavController()
    AppNav(
        nav = nav,
        authViewModel = authViewModel,
        userViewModel = userViewModel,
        collabViewModel = collabViewModel,
        categoryViewModel = categoryViewModel,
        promoViewModel = promoViewModel,
        bookingViewModel = bookingViewModel,
        statsViewModel = statsViewModel
    )
}

@Composable
private fun AppNav(
    nav: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    collabViewModel: CollabViewModel,
    categoryViewModel: CategoryViewModel,
    promoViewModel: PromoViewModel,
    bookingViewModel: BookingViewModel,
    statsViewModel: StatsViewModel
) {
    val appState by authViewModel.appState.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val sessionKey by authViewModel.sessionKey.collectAsState()

    LaunchedEffect(appState.isAuthenticated, sessionKey) {
        if (appState.isAuthenticated) authViewModel.getCurrentUser()
    }
    LaunchedEffect(sessionKey) {
        if (appState.isAuthenticated) {
            val id = authViewModel.currentUserId.value
            val currentUser = userViewModel.userState.value

            // Solo limpiar y recargar si el usuario cambió o no está cargado
            if (currentUser == null || (id != null && currentUser.cognitoId != id)) {
                Log.d("MainActivity", "🔄 SessionKey cambió - Recargando usuario")
                userViewModel.clearUser()
                collabViewModel.clearCollaborator()

                if (!id.isNullOrBlank()) {
                    userViewModel.getUserById(id)
                    try { collabViewModel.getCollaboratorById(id) } catch (_: Exception) {}
                }
            } else {
                Log.d("MainActivity", "✅ SessionKey cambió pero usuario ya está correcto - No recargar")
            }
        } else {
            Log.d("MainActivity", "🚪 Usuario no autenticado - Limpiando datos")
            userViewModel.clearUser()
            collabViewModel.clearCollaborator()
        }
    }
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            // Solo cargar si el usuario NO está ya cargado
            val currentUser = userViewModel.userState.value
            Log.d("MainActivity", "🔍 LaunchedEffect(currentUserId) - currentUserId: $currentUserId")
            Log.d("MainActivity", "🔍 currentUser?.cognitoId: ${currentUser?.cognitoId}")
            Log.d("MainActivity", "🔍 currentUser == null: ${currentUser == null}")
            Log.d("MainActivity", "🔍 cognitoId mismatch: ${currentUser != null && currentUser.cognitoId != currentUserId}")

            if (currentUser == null || currentUser.cognitoId != currentUserId) {
                Log.d("MainActivity", "📥 Cargando usuario por currentUserId: $currentUserId")
                userViewModel.getUserById(currentUserId!!)
            } else {
                Log.d("MainActivity", "✅ Usuario ya está cargado, saltando getUserById")
            }
        }
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
        composable(Screens.LoginRegister.route) { LoginRegister(nav, authViewModel = authViewModel) }
        composable(Screens.Login.route) { Login(nav, authViewModel = authViewModel) }
        composable(Screens.Register.route) { Register(nav, authViewModel = authViewModel) }
        composable(Screens.GoogleRegister.route) { GoogleRegister(nav, authViewModel = authViewModel) }
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

        // --- Post Login Permissions ---
        composable(Screens.PostLoginPermissions.route) {
            PostLoginPermissionsWithDestination(
                nav = nav,
                authViewModel = authViewModel,
                userViewModel = userViewModel,
                collabViewModel = collabViewModel
            )
        }

        // --- Onboarding ---
        composable(Screens.Onboarding.route) { Onboarding(nav) }
        composable(Screens.OnboardingCategories.route) {
            OnboardingCategories(
                nav,
                categoryViewModel = categoryViewModel,
                userViewModel = userViewModel,
                authViewModel = authViewModel
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
        composable(Screens.History.route) {
            val cognitoId by authViewModel.currentUserId.collectAsState()
            if (cognitoId.isNullOrBlank()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                History(
                    nav = nav,
                    userId = cognitoId!!,
                    userViewModel = userViewModel,
                    bookingVm = bookingViewModel
                )
            }
        }
        composable(Screens.Settings.route) { Settings(nav) }
        composable(Screens.Help.route) { Help(nav) }
        composable(Screens.Credits.route) { Credits(nav) }
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
                PromoQR(
                    nav = nav,
                    promotionId = promotionId,
                    cognitoId = cognitoId!!,
                    bookingViewModel = bookingViewModel,
                    viewModel = promoViewModel,
                    userViewModel = userViewModel
                )
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
        composable(Screens.ProfileCollab.route) { ProfileCollab(nav, authViewModel, collabViewModel) }
        composable(
            route = Screens.QrScanner.route,
            arguments = Screens.QrScanner.arguments
        ) { backStackEntry ->
            val branchId = backStackEntry.arguments?.getInt("branchId") ?: 1
            QRScannerScreen(
                nav = nav,
                branchId = branchId
            )
        }
        composable(Screens.StatsScreen.route) {
            val collabId by authViewModel.currentUserId.collectAsState()
            if (!collabId.isNullOrBlank()) {
                StatsScreen(nav, collabId!!, statsViewModel)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
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
        composable(Screens.BranchManagement.route) {
            BranchManagementScreen(nav = nav)
        }
        composable(Screens.StatusCollabSignup.route) {
            StatusCollabSignup(nav = nav)
        }

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

        composable(Screens.GeneratePromotionScreen.route) { GeneratePromotionScreen(nav) }
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

/**
 * Wrapper que determina el destino correcto basado en el tipo de usuario
 * (normal o colaborador) después de solicitar permisos post-login.
 */
@Composable
private fun PostLoginPermissionsWithDestination(
    nav: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    collabViewModel: CollabViewModel
) {
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val userState by userViewModel.userState.collectAsState()
    val collabState by collabViewModel.collabState.collectAsState()
    val isLoadingUser by userViewModel.isLoading.collectAsState()

    var hasLoadedProfiles by remember { mutableStateOf(false) }

    // Log cada vez que se recompone
    android.util.Log.d("MainActivity", "🔄 PostLoginPermissionsWithDestination recompose")
    android.util.Log.d("MainActivity", "  → currentUserId: $currentUserId ${if (currentUserId.isNullOrBlank()) "❌ NULL" else "✅"}")
    android.util.Log.d("MainActivity", "  → userState.cognitoId: ${userState.cognitoId}")
    android.util.Log.d("MainActivity", "  → hasLoadedProfiles: $hasLoadedProfiles")
    android.util.Log.d("MainActivity", "  → isLoadingUser: $isLoadingUser")

    // Cargar perfiles de usuario y colaborador cuando tenemos el ID
    LaunchedEffect(currentUserId) {
        val id = currentUserId
        if (!id.isNullOrBlank() && !hasLoadedProfiles) {
            Log.d("MainActivity", "🔄 PostLoginPermissions - LaunchedEffect ejecutándose para: $id")

            // Solo limpiar y recargar si el usuario NO está ya cargado con el ID correcto
            val currentUser = userViewModel.userState.value
            val currentCollab = collabViewModel.collabState.value

            if (currentUser.cognitoId.isNullOrBlank() || currentUser.cognitoId != id) {
                Log.d("MainActivity", "🔄 PostLoginPermissions - Cargando usuario: $id")
                userViewModel.clearUser()

                // Reintentar hasta 3 veces con delay entre intentos
                var attempts = 0
                var loaded = false
                while (attempts < 3 && !loaded) {
                    Log.d("MainActivity", "📥 Intento ${attempts + 1}/3 de cargar usuario")
                    userViewModel.getUserById(id)

                    // Esperar hasta 5 segundos por respuesta
                    var waitTime = 0
                    while (userViewModel.isLoading.value && waitTime < 5000) {
                        kotlinx.coroutines.delay(100)
                        waitTime += 100
                    }

                    // Log del timeout si aplica
                    if (waitTime >= 5000) {
                        Log.w("MainActivity", "⚠️ Timeout esperando getUserById (${waitTime}ms)")
                    }

                    val error = userViewModel.error.value
                    val user = userViewModel.userState.value

                    Log.d("MainActivity", "📋 Resultado: error=$error, userCognitoId=${user.cognitoId}")

                    if (error != null && error.contains("404")) {
                        attempts++
                        Log.w("MainActivity", "⚠️ Usuario no encontrado (intento $attempts/3), reintentando en 2 segundos...")
                        kotlinx.coroutines.delay(2000)
                    } else if (!user.cognitoId.isNullOrBlank()) {
                        loaded = true
                        Log.d("MainActivity", "✅ Usuario cargado exitosamente")
                    } else {
                        Log.d("MainActivity", "⚠️ Usuario sin cognitoId, reintentando...")
                        attempts++
                        if (attempts < 3) {
                            kotlinx.coroutines.delay(2000)
                        }
                    }
                }
            } else {
                Log.d("MainActivity", "✅ PostLoginPermissions - Usuario ya cargado: $id")
            }

            if (currentCollab.cognitoId.isNullOrBlank() || currentCollab.cognitoId != id) {
                Log.d("MainActivity", "🔄 PostLoginPermissions - Intentando cargar colaborador: $id")
                collabViewModel.clearCollaborator()
                try { collabViewModel.getCollaboratorById(id) } catch (_: Exception) {
                    Log.d("MainActivity", "ℹ️ No es colaborador")
                }
            } else {
                Log.d("MainActivity", "✅ PostLoginPermissions - Colaborador ya cargado: $id")
            }

            // CRÍTICO: Marcar como cargado SIEMPRE, independiente de si ya estaba o se cargó ahora
            hasLoadedProfiles = true
            Log.d("MainActivity", "✅ PostLoginPermissions - hasLoadedProfiles = true")
        }
    }

    // Determinar el destino basado en el tipo de usuario
    val destinationRoute = remember(userState.cognitoId, collabState.cognitoId) {
        when {
            !collabState.cognitoId.isNullOrBlank() -> Screens.HomeScreenCollab.route
            !userState.cognitoId.isNullOrBlank() -> Screens.Home.route
            else -> Screens.Home.route // Fallback
        }
    }

    // Mostrar splash mientras se carga
    if (!hasLoadedProfiles || isLoadingUser) {
        Startup(Modifier.fillMaxSize())
    } else {
        PostLoginPermissions(
            nav = nav,
            destinationRoute = destinationRoute
        )
    }
}
