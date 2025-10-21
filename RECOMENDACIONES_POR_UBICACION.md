# Sistema de Recomendaciones por Ubicación - Beneficio Juventud

## Descripción General

Se ha implementado un sistema completo de recomendaciones basado en la ubicación del usuario, que permite encontrar cupones y colaboradores cercanos en un radio de aproximadamente 3 km (configurable).

## Componentes Implementados

### Backend (NestJS)

#### 1. Utilidades de Ubicación
**Archivo:** `server-bj/src/common/location.utils.ts`

Funciones principales:
- `calculateDistance(point1, point2)`: Calcula distancia entre dos coordenadas usando fórmula Haversine
- `parseLocationString(locationString)`: Convierte string de PostgreSQL Point a coordenadas
- `filterByProximity(items, userLocation, getItemLocation, maxDistanceKm)`: Filtra y ordena elementos por proximidad

#### 2. Endpoints de Promociones
**Archivos:**
- `server-bj/src/promotions/promotions.service.ts`
- `server-bj/src/promotions/promotions.controller.ts`

**Endpoint:** `GET /promotions/nearby/search`

**Parámetros:**
- `latitude` (required): Latitud del usuario (ej: 25.6866)
- `longitude` (required): Longitud del usuario (ej: -100.3161)
- `radius` (optional): Radio de búsqueda en km (default: 3, max: 50)

**Ejemplo de uso:**
```bash
GET https://localhost:3000/promotions/nearby/search?latitude=25.6866&longitude=-100.3161&radius=3
```

**Respuesta:**
```json
[
  {
    "promotionId": 1,
    "title": "2x1 en Hamburguesas",
    "description": "...",
    "businessName": "Burger King",
    "logoUrl": "...",
    "distance": 0.8,
    "closestBranch": {
      "branchId": 5,
      "name": "Sucursal Centro",
      "address": "Av. Hidalgo 123",
      "phone": "8181234567",
      "location": "(-100.3161,25.6866)"
    },
    "categories": [...]
  }
]
```

#### 3. Endpoints de Colaboradores
**Archivos:**
- `server-bj/src/collaborators/collaborators.service.ts`
- `server-bj/src/collaborators/collaborators.controller.ts`

**Endpoint:** `GET /collaborators/nearby/search`

**Parámetros:** Mismos que promociones

**Ejemplo de uso:**
```bash
GET https://localhost:3000/collaborators/nearby/search?latitude=25.6866&longitude=-100.3161&radius=5
```

**Respuesta:**
```json
[
  {
    "cognitoId": "abc123",
    "businessName": "Restaurante La Plaza",
    "logoUrl": "...",
    "description": "...",
    "phone": "8181234567",
    "distance": 1.2,
    "closestBranch": {
      "branchId": 3,
      "name": "Sucursal Norte",
      "address": "..."
    },
    "totalBranches": 4,
    "categories": [...]
  }
]
```

### App Móvil (Android/Kotlin)

#### 1. Permisos de Ubicación
**Archivo:** `app/src/main/AndroidManifest.xml`

Permisos agregados:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

#### 2. Dependencias
**Archivo:** `app/build.gradle.kts`

```kotlin
// Google Play Services Location
implementation("com.google.android.gms:play-services-location:21.3.0")

// Google Maps SDK for Android
implementation("com.google.android.gms:play-services-maps:19.0.0")
implementation("com.google.maps.android:maps-compose:4.4.1")
implementation("com.google.maps.android:maps-ktx:5.1.1")
implementation("com.google.maps.android:maps-utils-ktx:5.1.1")
```

**API Key configurada en AndroidManifest.xml:**
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyCOe4P-4mbkN1eO0vjbV0BAV2Pe03gczXU" />
```

#### 3. LocationManager
**Archivo:** `app/src/main/java/mx/itesm/beneficiojuventud/utils/LocationManager.kt`

**Clase principal:** `LocationManager(context: Context)`

**Métodos:**
- `hasLocationPermission()`: Verifica permisos
- `getLastKnownLocation()`: Obtiene última ubicación conocida (rápido)
- `getCurrentLocation()`: Obtiene ubicación actual (preciso)
- `observeLocation(intervalMillis)`: Flow para actualizaciones en tiempo real
- `calculateDistance(from, to)`: Calcula distancia entre dos puntos

**Ejemplo de uso:**
```kotlin
val locationManager = LocationManager(context)

// Verificar permisos
if (!locationManager.hasLocationPermission()) {
    // Solicitar permisos
}

// Obtener ubicación actual
lifecycleScope.launch {
    val location = locationManager.getCurrentLocation()
    location?.let {
        println("Lat: ${it.latitude}, Lon: ${it.longitude}")
    }
}

// Observar cambios de ubicación
lifecycleScope.launch {
    locationManager.observeLocation(intervalMillis = 10000L)
        .collect { location ->
            println("Nueva ubicación: ${location.latitude}, ${location.longitude}")
        }
}
```

#### 4. Modelos de Datos

**NearbyPromotion** (`model/promos/NearbyPromotion.kt`):
```kotlin
data class NearbyPromotion(
    // Campos de Promotion...
    val distance: Double? = null,
    val closestBranch: Branch? = null
) {
    fun getFormattedDistance(): String // "0.5 km" o "500 m"
    fun toPromotion(): Promotions
}
```

**NearbyCollaborator** (`model/collaborators/NearbyCollaborator.kt`):
```kotlin
data class NearbyCollaborator(
    // Campos de Collaborator...
    val distance: Double? = null,
    val closestBranch: Branch? = null,
    val totalBranches: Int? = null
) {
    fun getFormattedDistance(): String
    fun toCollaborator(): Collaborator
}
```

#### 5. Componentes de Mapa con Google Maps

**NearbyPromotionsMap** (`components/NearbyPromotionsMap.kt`):

Mapa interactivo que muestra promociones cercanas con marcadores.

```kotlin
@Composable
fun NearbyPromotionsMap(
    userLocation: UserLocation?,
    nearbyPromotions: List<NearbyPromotion>,
    onPromotionMarkerClick: (NearbyPromotion) -> Unit = {},
    modifier: Modifier = Modifier
)
```

**Características:**
- Muestra la ubicación del usuario
- Marcadores para cada promoción cercana
- Círculo de 3 km de radio de búsqueda
- Círculos pequeños alrededor de cada promoción
- Contador de promociones encontradas
- Click en marcadores para ver detalles

**NearbyCollaboratorsMap** (`components/NearbyCollaboratorsMap.kt`):

Mapa para mostrar colaboradores (negocios) cercanos.

```kotlin
@Composable
fun NearbyCollaboratorsMap(
    userLocation: UserLocation?,
    nearbyCollaborators: List<NearbyCollaborator>,
    onCollaboratorMarkerClick: (NearbyCollaborator) -> Unit = {},
    modifier: Modifier = Modifier
)
```

**Características:**
- Marcadores naranjas para colaboradores
- Muestra nombre del negocio y sucursal más cercana
- Contador de negocios y sucursales totales

**CombinedNearbyMap** (`components/NearbyCollaboratorsMap.kt`):

Mapa que muestra tanto promociones como colaboradores en un solo mapa.

```kotlin
@Composable
fun CombinedNearbyMap(
    userLocation: UserLocation?,
    nearbyPromotions: List<NearbyPromotion>,
    nearbyCollaborators: List<NearbyCollaborator>,
    onPromotionMarkerClick: (NearbyPromotion) -> Unit = {},
    onCollaboratorMarkerClick: (NearbyCollaborator) -> Unit = {},
    modifier: Modifier = Modifier
)
```

**Características:**
- Marcadores azules para promociones 🎟️
- Marcadores naranjas para colaboradores 🏢
- Contador combinado
- Callbacks separados para cada tipo

**SimpleLocationMap** (`components/NearbyPromotionsMap.kt`):

Mapa simple que solo muestra la ubicación del usuario (útil para perfiles o configuración).

```kotlin
@Composable
fun SimpleLocationMap(
    userLocation: UserLocation?,
    modifier: Modifier = Modifier
)
```

#### 6. Servicios de API

**PromoApiService** (`model/promos/PromoApiService.kt`):
```kotlin
@GET("promotions/nearby/search")
suspend fun getNearbyPromotions(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("radius") radius: Double? = 3.0
): Response<List<NearbyPromotion>>
```

**CollabApiService** (`model/collaborators/CollabApiService.kt`):
```kotlin
@GET("collaborators/nearby/search")
suspend fun getNearbyCollaborators(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("radius") radius: Double? = 3.0
): Response<List<NearbyCollaborator>>
```

## Cómo Integrar en ViewModels

### Ejemplo: ViewModel de Promociones Cercanas

```kotlin
class PromosViewModel(
    private val promoApiService: PromoApiService,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _nearbyPromotions = MutableStateFlow<List<NearbyPromotion>>(emptyList())
    val nearbyPromotions: StateFlow<List<NearbyPromotion>> = _nearbyPromotions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadNearbyPromotions(radiusKm: Double = 3.0) {
        viewModelScope.launch {
            _isLoading.value = true

            // Obtener ubicación actual
            val location = locationManager.getCurrentLocation()

            if (location != null) {
                // Llamar al API
                val response = promoApiService.getNearbyPromotions(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = radiusKm
                )

                if (response.isSuccessful) {
                    _nearbyPromotions.value = response.body() ?: emptyList()
                }
            }

            _isLoading.value = false
        }
    }
}
```

### Ejemplo: UI con Compose

```kotlin
@Composable
fun NearbyPromotionsScreen(viewModel: PromosViewModel) {
    val nearbyPromotions by viewModel.nearbyPromotions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Solicitar permisos de ubicación
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        } else {
            viewModel.loadNearbyPromotions()
        }
    }

    LazyColumn {
        items(nearbyPromotions) { promo ->
            PromoCard(
                promotion = promo,
                distance = promo.getFormattedDistance()
            )
        }
    }
}

@Composable
fun PromoCard(promotion: NearbyPromotion, distance: String) {
    Card {
        Column {
            Text(promotion.title ?: "")
            Text(promotion.businessName ?: "")
            Text("📍 $distance") // Mostrar distancia
            Text(promotion.closestBranch?.address ?: "")
        }
    }
}
```

## Cómo Usar en la UI

### 1. Solicitar Permisos de Ubicación

```kotlin
// En MainActivity o en el Composable
val locationPermissions = rememberMultiplePermissionsState(
    permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
)

Button(onClick = { locationPermissions.launchMultiplePermissionRequest() }) {
    Text("Permitir acceso a ubicación")
}
```

### 2. Mostrar Promociones Cercanas

```kotlin
// En el ViewModel
fun loadNearbyPromotions() {
    viewModelScope.launch {
        val location = locationManager.getCurrentLocation()
        if (location != null) {
            val response = promoApiService.getNearbyPromotions(
                location.latitude,
                location.longitude
            )
            // Procesar respuesta...
        }
    }
}
```

### 3. Actualizar UI con Distancia

```kotlin
LazyColumn {
    items(nearbyPromotions) { promo ->
        Row {
            // Imagen de la promoción
            AsyncImage(model = promo.imageUrl, ...)

            Column {
                Text(promo.title ?: "")
                Text(promo.businessName ?: "")

                // Mostrar distancia con icono
                Row {
                    Icon(Icons.Default.Place, ...)
                    Text(promo.getFormattedDistance()) // "1.2 km"
                }

                // Mostrar dirección de sucursal más cercana
                Text(promo.closestBranch?.address ?: "")
            }
        }
    }
}
```

### 4. Usar Componentes de Mapa

#### Mapa de Promociones Cercanas

```kotlin
@Composable
fun NearbyPromotionsScreen(viewModel: PromosViewModel) {
    val nearbyPromotions by viewModel.nearbyPromotions.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val selectedPromotion = remember { mutableStateOf<NearbyPromotion?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa con promociones
        NearbyPromotionsMap(
            userLocation = userLocation,
            nearbyPromotions = nearbyPromotions,
            onPromotionMarkerClick = { promo ->
                selectedPromotion.value = promo
            },
            modifier = Modifier.fillMaxSize()
        )

        // Bottom Sheet con detalles de promoción seleccionada
        selectedPromotion.value?.let { promo ->
            PromotionDetailsSheet(
                promotion = promo,
                onDismiss = { selectedPromotion.value = null }
            )
        }
    }
}
```

#### Vista Combinada: Lista + Mapa

```kotlin
@Composable
fun NearbyPromotionsWithMapScreen(viewModel: PromosViewModel) {
    val nearbyPromotions by viewModel.nearbyPromotions.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    var showMap by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle para cambiar entre lista y mapa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showMap = false },
                enabled = showMap
            ) {
                Icon(Icons.Default.List, "Lista")
                Text("Lista")
            }
            Button(
                onClick = { showMap = true },
                enabled = !showMap
            ) {
                Icon(Icons.Default.Map, "Mapa")
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
                    PromotionCard(promotion = promo)
                }
            }
        }
    }
}
```

#### Mapa Combinado (Promociones + Colaboradores)

```kotlin
@Composable
fun ExploreNearbyScreen(
    promosViewModel: PromosViewModel,
    collabViewModel: CollaboratorsViewModel
) {
    val nearbyPromotions by promosViewModel.nearbyPromotions.collectAsState()
    val nearbyCollaborators by collabViewModel.nearbyCollaborators.collectAsState()
    val userLocation by promosViewModel.userLocation.collectAsState()

    var selectedItem by remember { mutableStateOf<Any?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        CombinedNearbyMap(
            userLocation = userLocation,
            nearbyPromotions = nearbyPromotions,
            nearbyCollaborators = nearbyCollaborators,
            onPromotionMarkerClick = { promo ->
                selectedItem = promo
            },
            onCollaboratorMarkerClick = { collab ->
                selectedItem = collab
            },
            modifier = Modifier.fillMaxSize()
        )

        // Mostrar detalles según el tipo seleccionado
        when (val item = selectedItem) {
            is NearbyPromotion -> {
                PromotionDetailsSheet(
                    promotion = item,
                    onDismiss = { selectedItem = null }
                )
            }
            is NearbyCollaborator -> {
                CollaboratorDetailsSheet(
                    collaborator = item,
                    onDismiss = { selectedItem = null }
                )
            }
        }
    }
}
```

#### Mapa Simple (Solo Ubicación)

```kotlin
@Composable
fun ProfileLocationSection(userLocation: UserLocation?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        SimpleLocationMap(
            userLocation = userLocation,
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

## Consideraciones Importantes

### Backend

1. **Rendimiento:** Para bases de datos grandes, considera agregar índices espaciales:
   ```sql
   CREATE INDEX idx_branch_location ON sucursal USING GIST(ubicacion);
   ```

2. **Paginación:** Considera agregar paginación para muchos resultados:
   ```typescript
   async findNearbyPromotions(lat, lon, radius, page = 1, limit = 20)
   ```

3. **Cache:** Considera cachear resultados por coordenadas redondeadas

### App Móvil

1. **Permisos:** Siempre verifica permisos antes de obtener ubicación
2. **Manejo de errores:** La ubicación puede ser null (GPS desactivado, sin permisos, etc.)
3. **Batería:** No uses `observeLocation()` con intervalos muy cortos
4. **Precisión:** `getLastKnownLocation()` es más rápido pero menos preciso que `getCurrentLocation()`

## Testing

### Backend
```bash
# Test endpoint de promociones
curl "https://localhost:3000/promotions/nearby/search?latitude=25.6866&longitude=-100.3161&radius=3"

# Test endpoint de colaboradores
curl "https://localhost:3000/collaborators/nearby/search?latitude=25.6866&longitude=-100.3161&radius=5"
```

### App Móvil
```kotlin
// Test LocationManager
@Test
fun `test location manager returns coordinates`() = runTest {
    val locationManager = LocationManager(context)
    val location = locationManager.getCurrentLocation()
    assertNotNull(location)
    assertTrue(location.latitude in -90.0..90.0)
    assertTrue(location.longitude in -180.0..180.0)
}
```

## Integración con Google Maps

### Configuración de API Key

La API Key de Google Cloud está configurada en:
- **AndroidManifest.xml**: Como meta-data
- **Valor**: `AIzaSyCOe4P-4mbkN1eO0vjbV0BAV2Pe03gczXU`

### APIs Habilitadas en Google Cloud

Asegúrate de que las siguientes APIs estén habilitadas en tu proyecto de Google Cloud:
1. **Maps SDK for Android**
2. **Places API** (para geocoding en el backend)
3. **Geolocation API** (opcional, para mejorar precisión)

### Personalización de Mapas

Los componentes de mapa incluyen varias opciones de personalización:

```kotlin
// Cambiar estilo del mapa
GoogleMap(
    properties = MapProperties(
        mapType = MapType.NORMAL, // NORMAL, SATELLITE, TERRAIN, HYBRID
        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    )
)

// Controlar zoom y gestos
GoogleMap(
    uiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        zoomGesturesEnabled = true,
        scrollGesturesEnabled = true,
        tiltGesturesEnabled = false,
        rotateGesturesEnabled = false,
        compassEnabled = true,
        myLocationButtonEnabled = true
    )
)
```

### Tipos de Marcadores

Los mapas usan diferentes colores de marcadores para distinguir tipos:

| Tipo | Color | Ícono | Descripción |
|------|-------|-------|-------------|
| Usuario | Azul Claro | 🔵 | Ubicación actual del usuario |
| Promoción | Azul | 🎟️ | Ofertas y descuentos |
| Colaborador | Naranja | 🏢 | Negocios/sucursales |

### Optimización de Rendimiento

1. **Clustering**: Para muchos marcadores (>50), considera usar clustering:
```kotlin
// TODO: Implementar clustering con maps-utils-ktx
implementation("com.google.maps.android:maps-utils-ktx:5.1.1")
```

2. **Límite de marcadores**: El endpoint ya limita resultados a 3 km, pero puedes ajustar:
```kotlin
getNearbyPromotions(latitude, longitude, radius = 1.5) // 1.5 km
```

3. **Cache de ubicación**: Usa `getLastKnownLocation()` primero, luego actualiza con `getCurrentLocation()`

## Próximos Pasos (Opcional)

1. **Filtros combinados:** Búsqueda por categoría + ubicación
2. **Clustering de marcadores:** Agrupar marcadores cercanos para mejor rendimiento
3. **Rutas:** Integrar Directions API para mostrar cómo llegar
4. **Notificaciones:** Notificar cuando el usuario esté cerca de una promoción
5. **Favoritos cercanos:** Mostrar solo favoritos que estén cerca
6. **Historial:** Recordar ubicaciones frecuentes del usuario
7. **Modo oscuro:** Estilo de mapa personalizado para tema oscuro
8. **Heatmap:** Visualizar densidad de promociones por zona

## Arquitectura del Sistema

```
Usuario móvil (lat, lon)
         ↓
   LocationManager (obtiene GPS)
         ↓
   PromoApiService.getNearbyPromotions(lat, lon, radius)
         ↓
   Backend: /promotions/nearby/search
         ↓
   PromotionsService.findNearbyPromotions()
         ↓
   1. Query: Obtener todas las promociones activas + sucursales
   2. Filter: Calcular distancia con Haversine
   3. Sort: Ordenar por distancia (más cercano primero)
         ↓
   Respuesta: List<NearbyPromotion> con distance y closestBranch
         ↓
   UI: Mostrar lista con distancia formateada
```

## Notas Técnicas

- **Radio de la Tierra:** 6371 km (usado en fórmula Haversine)
- **Formato de coordenadas PostgreSQL:** `(longitude, latitude)` como Point
- **Precisión de distancia:** Redondeada a 2 decimales (ej: 1.23 km)
- **Radio máximo:** 50 km (configurable en backend)
- **Radio por defecto:** 3 km

## Soporte

Para dudas o problemas con la implementación:
1. Revisar logs del backend: `npm run start:dev` en `server-bj/`
2. Revisar Logcat de Android Studio para la app
3. Verificar que las sucursales tengan el campo `location` poblado en la BD
