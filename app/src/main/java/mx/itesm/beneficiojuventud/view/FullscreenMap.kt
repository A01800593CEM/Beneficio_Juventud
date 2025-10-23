package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.components.CombinedNearbyMap
import mx.itesm.beneficiojuventud.model.collaborators.NearbyCollaborator
import mx.itesm.beneficiojuventud.model.promos.NearbyPromotion
import mx.itesm.beneficiojuventud.utils.LocationManager
import mx.itesm.beneficiojuventud.utils.UserLocation
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import mx.itesm.beneficiojuventud.viewmodel.PromoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Pantalla de mapa en pantalla completa para explorar promociones y colaboradores cercanos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenMap(
    nav: NavHostController,
    promoViewModel: PromoViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationManager = remember { LocationManager(context) }
    var userLocation by remember { mutableStateOf<UserLocation?>(null) }
    var nearbyPromotions by remember { mutableStateOf<List<NearbyPromotion>>(emptyList()) }
    var nearbyCollaborators by remember { mutableStateOf<List<NearbyCollaborator>>(emptyList()) }
    var nearbyLoading by remember { mutableStateOf(false) }
    var nearbyError by remember { mutableStateOf<String?>(null) }

    // Obtener ubicación del usuario
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                if (locationManager.hasLocationPermission()) {
                    userLocation = locationManager.getLastKnownLocation()
                }
            } catch (e: Exception) {
                android.util.Log.e("FullscreenMap", "Error getting location", e)
            }
        }
    }

    // Cargar datos cercanos
    LaunchedEffect(userLocation) {
        val location = userLocation ?: return@LaunchedEffect

        nearbyLoading = true
        nearbyError = null

        scope.launch {
            try {
                val promosDeferred = async {
                    try {
                        promoViewModel.getNearbyPromotions(
                            location.latitude,
                            location.longitude,
                            radius = 3.0
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("FullscreenMap", "Error loading nearby promos", e)
                        emptyList()
                    }
                }
                val collabsDeferred = async {
                    try {
                        collabViewModel.getNearbyCollaborators(
                            location.latitude,
                            location.longitude,
                            radius = 3.0
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("FullscreenMap", "Error loading nearby collabs", e)
                        emptyList()
                    }
                }

                nearbyPromotions = promosDeferred.await()
                nearbyCollaborators = collabsDeferred.await()
            } catch (e: Exception) {
                android.util.Log.e("FullscreenMap", "Error in nearby loading", e)
                nearbyError = e.message ?: "Error desconocido"
            } finally {
                nearbyLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cerca de ti") },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver al inicio"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !locationManager.hasLocationPermission() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "📍 Se requieren permisos de ubicación",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Para mostrarte el mapa de promociones y negocios cercanos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                userLocation == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Obteniendo tu ubicación...")
                    }
                }
                nearbyLoading -> {
                    CombinedNearbyMap(
                        userLocation = userLocation,
                        nearbyPromotions = emptyList(),
                        nearbyCollaborators = emptyList(),
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text("Buscando cerca de ti...")
                            }
                        }
                    }
                }
                nearbyError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "❌ Error al cargar el mapa",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            nearbyError ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    CombinedNearbyMap(
                        userLocation = userLocation,
                        nearbyPromotions = nearbyPromotions,
                        nearbyCollaborators = nearbyCollaborators,
                        onPromotionMarkerClick = { promo ->
                            promo.promotionId?.let { id ->
                                nav.navigate(Screens.PromoQR.createRoute(id))
                            }
                        },
                        onCollaboratorMarkerClick = { collab ->
                            collab.cognitoId?.let { id ->
                                nav.navigate(Screens.Business.createRoute(id))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
