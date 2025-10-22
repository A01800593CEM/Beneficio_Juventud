# Migración de Google Places a Mapbox Geocoding API

## Resumen de Cambios

Se ha realizado una migración completa del servicio de geocoding de **Google Places API** a **Mapbox Geocoding API** para resolver problemas de billing y restricciones de Google Places.

### Archivos Modificados

#### 1. **PlacesService.kt** 
   - Eliminado: Dependencias de `com.google.android.libraries.places`
   - Agregado: Implementación REST directa con OkHttp
   - Cambio: La clase ahora usa la API de Mapbox directamente

**Ventajas de la migración:**
- ✅ Sin problemas de billing restrictivos como Google Places
- ✅ API REST simple sin SDK pesado
- ✅ Información de coordenadas (latitud/longitud) incluida en respuestas
- ✅ Excelente cobertura geográfica mundial
- ✅ Compatible con la interfaz existente (mismo nombre de métodos, mismos tipos de datos)

#### 2. **PlacesAutocompleteViewModel.kt**
   - Cambio: `AutocompleteSessionToken?` → `String?`
   - Actualización: Eliminada importación de Google Places
   - La funcionalidad se mantiene completamente igual

#### 3. **build.gradle.kts**
   - Eliminado: `implementation(libs.places)`
   - Eliminado: `implementation("com.google.android.libraries.places:places:4.1.0")`
   - Comentario: OkHttp ya está disponible a través de Retrofit

#### 4. **AndroidManifest.xml**
   - Agregado: Meta-data para token de Mapbox
   - Ubicación: `<meta-data android:name="MAPBOX_ACCESS_TOKEN" android:value="YOUR_TOKEN" />`

## Configuración Requerida

### Paso 1: Obtener Token de Mapbox

1. Ir a https://account.mapbox.com/access-tokens/
2. Crear un nuevo token con permisos de lectura
3. Copiar el token generado

### Paso 2: Configurar en AndroidManifest.xml

```xml
<meta-data
    android:name="MAPBOX_ACCESS_TOKEN"
    android:value="pk.eyJ1IjoiTlVTVFJFSTEyMzQiLCJhIjoiY2xqTmk..." />
```

Reemplazar `YOUR_MAPBOX_ACCESS_TOKEN_HERE` con tu token real.

## API Mapbox Geocoding

### Estructura de Respuesta

**Predicciones (búsqueda):**
```json
{
  "features": [
    {
      "id": "place.12345",
      "text": "Mexico City",
      "place_name": "Mexico City, Mexico",
      "geometry": {
        "type": "Point",
        "coordinates": [-99.1332, 19.4326]
      }
    }
  ]
}
```

**Detalles (coordinadas):**
- Latitud: `coordinates[1]`
- Longitud: `coordinates[0]`

## Métodos Disponibles

```kotlin
class PlacesService(context: Context) {
    // Obtener predicciones de direcciones
    suspend fun getAddressPredictions(
        input: String,
        country: String = "MX",
        sessionToken: Any? = null
    ): List<PlacesPrediction>
    
    // Obtener detalles de un lugar
    suspend fun getPlaceDetails(
        placeId: String,
        sessionToken: Any? = null
    ): PlacesDetails
    
    // Generar token de sesión (compatible, retorna String)
    fun generateSessionToken(): String
}
```

## Tipos de Datos

```kotlin
data class PlacesPrediction(
    val placeId: String,        // ID del lugar de Mapbox
    val mainText: String,       // Nombre principal
    val secondaryText: String,  // Información adicional
    val fullText: String        // Nombre completo
)

data class PlacesDetails(
    val address: String,    // Dirección completa
    val latitude: Double,   // Latitud
    val longitude: Double,  // Longitud
    val placeId: String     // ID del lugar
)
```

## Limits y Consideraciones

### Rate Limiting
- Mapbox ofrece límites generosos en su tier gratuito
- 600 requests/minuto en desarrollo

### Cobertura
- Cobertura mundial de direcciones
- Soporte para múltiples idiomas
- Filtrado por país (parámetro `country`)

### Diferencias respecto a Google Places

| Aspecto | Google Places | Mapbox |
|---------|---------------|--------|
| Billing | Restrictivo | Más flexible |
| Coordinadas | Requiere API aparte | Incluidas |
| SDK | Dependencia pesada | API REST |
| Session Tokens | Requerido | Opcional |
| Cobertura | Excelente | Excelente |

## Testing

Para probar la migración:

```bash
./gradlew assembleDebug
# O para ejecutar en emulador:
./gradlew installDebugAndroidTest
```

## Notas de Desarrollo

1. **Token en Producción**: Usar variables de entorno o gradle secrets para proteger el token
2. **Manejo de Errores**: La clase captura excepciones HTTP y de parsing JSON
3. **Logging**: Todos los eventos se registran con tag "PlacesService"
4. **Compatibilidad**: La interfaz pública es completamente compatible con el código existente

## Rollback

Si es necesario volver a Google Places:
1. Restaurar `build.gradle.kts` con la dependencia de Google Places
2. Cambiar `String?` → `AutocompleteSessionToken?` en ViewModel
3. Restaurar la implementación anterior de PlacesService.kt

