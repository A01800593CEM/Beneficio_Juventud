# Guía Rápida: Integrar Mapas con Recomendaciones por Ubicación

Esta guía te ayudará a integrar rápidamente los mapas de Google en tu app para mostrar promociones y colaboradores cercanos.

## ✅ Ya Configurado

- ✅ Dependencias de Google Maps SDK agregadas
- ✅ API Key configurada en AndroidManifest
- ✅ Permisos de ubicación agregados
- ✅ LocationManager implementado
- ✅ Componentes de mapa listos para usar
- ✅ Endpoints de backend funcionando

## 🚀 Paso 1: Agregar Permisos en Runtime

En tu Activity principal o donde uses ubicación:

```kotlin
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun MainScreen() {
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Tu contenido...
}
```

## 🗺️ Paso 2: Usar el Componente de Mapa

### Opción A: Mapa de Promociones Cercanas

```kotlin
import mx.itesm.beneficiojuventud.components.NearbyPromotionsMap
import mx.itesm.beneficiojuventud.utils.LocationManager

@Composable
fun PromocionesScreen(context: Context) {
    val locationManager = remember { LocationManager(context) }
    var userLocation by remember { mutableStateOf<UserLocation?>(null) }
    var nearbyPromotions by remember { mutableStateOf<List<NearbyPromotion>>(emptyList()) }

    // Obtener ubicación
    LaunchedEffect(Unit) {
        userLocation = locationManager.getCurrentLocation()

        // Llamar al API
        userLocation?.let { location ->
            val response = RetrofitInstance.promoApi.getNearbyPromotions(
                latitude = location.latitude,
                longitude = location.longitude,
                radius = 3.0
            )
            if (response.isSuccessful) {
                nearbyPromotions = response.body() ?: emptyList()
            }
        }
    }

    // Mostrar mapa
    NearbyPromotionsMap(
        userLocation = userLocation,
        nearbyPromotions = nearbyPromotions,
        onPromotionMarkerClick = { promo ->
            // Navegar a detalles de la promoción
            println("Clicked: ${promo.title}")
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

### Opción B: Vista Lista + Mapa con Toggle

```kotlin
@Composable
fun PromocionesConMapaScreen(context: Context) {
    val locationManager = remember { LocationManager(context) }
    var userLocation by remember { mutableStateOf<UserLocation?>(null) }
    var nearbyPromotions by remember { mutableStateOf<List<NearbyPromotion>>(emptyList()) }
    var showMap by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userLocation = locationManager.getCurrentLocation()
        userLocation?.let { location ->
            val response = RetrofitInstance.promoApi.getNearbyPromotions(
                location.latitude, location.longitude
            )
            if (response.isSuccessful) {
                nearbyPromotions = response.body() ?: emptyList()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Botones de toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = { showMap = false },
                enabled = showMap
            ) {
                Icon(Icons.Default.List, null)
                Spacer(Modifier.width(4.dp))
                Text("Lista")
            }
            FilledTonalButton(
                onClick = { showMap = true },
                enabled = !showMap
            ) {
                Icon(Icons.Default.Map, null)
                Spacer(Modifier.width(4.dp))
                Text("Mapa")
            }
        }

        // Contenido
        if (showMap) {
            NearbyPromotionsMap(
                userLocation = userLocation,
                nearbyPromotions = nearbyPromotions,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn {
                items(nearbyPromotions) { promo ->
                    PromoCard(promotion = promo)
                }
            }
        }
    }
}

@Composable
fun PromoCard(promotion: NearbyPromotion) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Logo del negocio
            AsyncImage(
                model = promotion.logoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = promotion.title ?: "",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = promotion.businessName ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = promotion.getFormattedDistance(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = promotion.closestBranch?.address ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### Opción C: Mapa Combinado (Promociones + Colaboradores)

```kotlin
import mx.itesm.beneficiojuventud.components.CombinedNearbyMap

@Composable
fun ExplorarCercaScreen(context: Context) {
    val locationManager = remember { LocationManager(context) }
    var userLocation by remember { mutableStateOf<UserLocation?>(null) }
    var nearbyPromotions by remember { mutableStateOf<List<NearbyPromotion>>(emptyList()) }
    var nearbyCollaborators by remember { mutableStateOf<List<NearbyCollaborator>>(emptyList()) }

    LaunchedEffect(Unit) {
        userLocation = locationManager.getCurrentLocation()

        userLocation?.let { location ->
            // Cargar promociones
            val promosResponse = RetrofitInstance.promoApi.getNearbyPromotions(
                location.latitude, location.longitude
            )
            if (promosResponse.isSuccessful) {
                nearbyPromotions = promosResponse.body() ?: emptyList()
            }

            // Cargar colaboradores
            val collabResponse = RetrofitInstance.collabApi.getNearbyCollaborators(
                location.latitude, location.longitude
            )
            if (collabResponse.isSuccessful) {
                nearbyCollaborators = collabResponse.body() ?: emptyList()
            }
        }
    }

    CombinedNearbyMap(
        userLocation = userLocation,
        nearbyPromotions = nearbyPromotions,
        nearbyCollaborators = nearbyCollaborators,
        onPromotionMarkerClick = { promo ->
            // Navegar a detalles
        },
        onCollaboratorMarkerClick = { collab ->
            // Navegar a perfil del negocio
        },
        modifier = Modifier.fillMaxSize()
    )
}
```

## 📱 Paso 3: Agregar a tu Navegación

En tu NavHost o sistema de navegación:

```kotlin
NavHost(navController = navController, startDestination = "home") {
    // ... otras rutas

    composable("nearby_promotions") {
        PromocionesConMapaScreen(context = LocalContext.current)
    }

    composable("explore_nearby") {
        ExplorarCercaScreen(context = LocalContext.current)
    }
}
```

## 🎨 Paso 4: Agregar Tab/Botón de Acceso

En tu BottomNavigationBar o menú principal:

```kotlin
NavigationBar {
    NavigationBarItem(
        icon = { Icon(Icons.Default.Map, null) },
        label = { Text("Cerca de ti") },
        selected = currentRoute == "nearby_promotions",
        onClick = { navController.navigate("nearby_promotions") }
    )
}
```

## 🔧 Configuración Avanzada (Opcional)

### Actualizar ubicación en tiempo real

```kotlin
LaunchedEffect(Unit) {
    locationManager.observeLocation(intervalMillis = 30000L) // Cada 30 segundos
        .collect { newLocation ->
            userLocation = newLocation
            // Recargar promociones cercanas
        }
}
```

### Ajustar radio de búsqueda

```kotlin
var radius by remember { mutableStateOf(3.0) } // 3 km por defecto

Slider(
    value = radius.toFloat(),
    onValueChange = { radius = it.toDouble() },
    valueRange = 1f..10f,
    steps = 8
)

Text("Radio: ${radius.toInt()} km")

// Usar en la llamada al API
getNearbyPromotions(latitude, longitude, radius = radius)
```

### Filtrar por categoría + ubicación

```kotlin
var selectedCategory by remember { mutableStateOf<String?>(null) }

// Filtrar localmente después de obtener resultados
val filteredPromotions = if (selectedCategory != null) {
    nearbyPromotions.filter { promo ->
        promo.categories.any { it.name == selectedCategory }
    }
} else {
    nearbyPromotions
}
```

## 🐛 Solución de Problemas

### Mapa no se muestra

1. Verifica que la API Key esté en AndroidManifest.xml
2. Asegúrate de que Maps SDK for Android esté habilitado en Google Cloud Console
3. Revisa Logcat para errores de autenticación

### No se obtiene ubicación

1. Verifica permisos en runtime
2. Activa GPS en el dispositivo
3. Prueba con `getLastKnownLocation()` primero

### Marcadores no aparecen

1. Verifica que las sucursales tengan el campo `location` poblado en la BD
2. Revisa el formato de ubicación: `"(longitude, latitude)"`
3. Verifica que la respuesta del API incluya `closestBranch`

## 📚 Recursos

- Documentación completa: `RECOMENDACIONES_POR_UBICACION.md`
- Google Maps Compose: https://developers.google.com/maps/documentation/android-sdk/maps-compose
- API Reference: https://developers.google.com/maps/documentation/android-sdk/reference

## ✨ Mejoras Futuras

- [ ] Clustering de marcadores para muchos resultados
- [ ] Rutas con Directions API
- [ ] Notificaciones de proximidad
- [ ] Modo oscuro personalizado
- [ ] Heatmap de promociones por zona
- [ ] Guardado de ubicaciones favoritas
