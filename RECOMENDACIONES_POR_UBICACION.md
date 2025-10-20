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
implementation("com.google.android.gms:play-services-location:21.3.0")
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

#### 5. Servicios de API

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

## Próximos Pasos (Opcional)

1. **Filtros combinados:** Búsqueda por categoría + ubicación
2. **Mapa:** Integrar Google Maps para mostrar promociones en un mapa
3. **Notificaciones:** Notificar cuando el usuario esté cerca de una promoción
4. **Favoritos cercanos:** Mostrar solo favoritos que estén cerca
5. **Historial:** Recordar ubicaciones frecuentes del usuario

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
