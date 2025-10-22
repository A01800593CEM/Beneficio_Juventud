# 📍 Autocompletado de Direcciones con Maps SDK for Android

## 📌 Resumen

Implementaremos **autocompletado de direcciones en tiempo real** usando **Google Places API for Android** (integrada en Maps SDK) directamente en la app móvil, sin necesidad de backend adicional.

---

## 🎯 Qué Implementaremos

### En RegisterCollab.kt:
```
Usuario escribe "Av. Re" en campo de dirección
      ↓
Aparecen sugerencias:
  • "Av. Reforma 505, CDMX"
  • "Av. Revolución, CDMX"
  • "Av. Reina, CDMX"
      ↓
Usuario selecciona "Av. Reforma 505, CDMX"
      ↓
Campo se llena automáticamente + coordenadas se obtienen
      ↓
✓ Listo para registrar
```

### En BranchLocationPicker.kt:
```
Usuario abre diálogo para agregar sucursal
      ↓
Campo de búsqueda con autocompletado
      ↓
Usuario escribe dirección
      ↓
Aparecen sugerencias
      ↓
Usuario selecciona
      ↓
Mapa se centra en esa ubicación
      ↓
✓ Usuario confirma punto en el mapa
```

---

## 🛠️ Instalación de Dependencias

### 1. Agregar a `build.gradle` (Project level)

```gradle
buildscript {
    ext {
        compose_version = '1.3.0'
        google_maps_version = '18.1.0'  // Latest
        places_version = '2.7.0'         // Latest
    }
}
```

### 2. Agregar a `build.gradle.kts` (App level)

```kotlin
dependencies {
    // Google Maps
    implementation("com.google.android.gms:play-services-maps:${google_maps_version}")

    // Google Places (para autocompletado)
    implementation("com.google.android.libraries.places:places:${places_version}")

    // Coroutines (para async)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
}
```

### 3. Habilitar APIs en Google Cloud Console

1. Ir a [Google Cloud Console](https://console.cloud.google.com/)
2. Seleccionar proyecto
3. Habilitar estas APIs:
   - Maps SDK for Android
   - Places API
   - Geocoding API
4. Crear API Key (si no existe)
5. Restringir a Android app (opcional, pero recomendado)

---

## 📝 Implementación en RegisterCollab.kt

### Paso 1: Agregar permisos en AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Metadata for Google Maps API Key -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### Paso 2: Crear composable de autocompletado

```kotlin
// EN: AddressAutocompleteTextField.kt (NUEVO ARCHIVO)

package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PlacePrediction(
    val placeId: String,
    val mainText: String,
    val secondaryText: String,
    val fullText: String
)

@Composable
fun AddressAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddressSelected: (address: String, latitude: Double?, longitude: Double?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Dirección",
    placeholder: String = "Escribe tu dirección",
    country: String = "MX"
) {
    var suggestions by remember { mutableStateOf<List<PlacePrediction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sessionToken = remember { AutocompleteSessionToken.newInstance() }

    // Inicializar Places si no está ya inicializado
    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(androidx.compose.ui.platform.LocalContext.current)
        }
        Places.createClient(androidx.compose.ui.platform.LocalContext.current)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                if (newValue.length >= 2) {
                    isLoading = true
                    showSuggestions = true

                    // Buscar autocomplete después de 300ms (debounce)
                    scope.launch {
                        kotlinx.coroutines.delay(300)
                        try {
                            val request = FindAutocompletePredictionsRequest.builder()
                                .setSessionToken(sessionToken)
                                .setQuery(newValue)
                                .setCountry(country)
                                .setLocationBias(
                                    com.google.android.gms.maps.model.LatLngBounds(
                                        // Default bounds for Mexico
                                        com.google.android.gms.maps.model.LatLng(14.5, -117.0),
                                        com.google.android.gms.maps.model.LatLng(32.7, -86.7)
                                    )
                                )
                                .build()

                            val response = placesClient.findAutocompletePredictions(request).await()
                            suggestions = response.autocompletePredictions.map { prediction ->
                                PlacePrediction(
                                    placeId = prediction.placeId,
                                    mainText = prediction.getPrimaryText(null).toString(),
                                    secondaryText = prediction.getSecondaryText(null)?.toString() ?: "",
                                    fullText = prediction.getFullText(null).toString()
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    suggestions = emptyList()
                    showSuggestions = false
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TextFieldDefaults.MinHeight),
            shape = RoundedCornerShape(18.dp),
            leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFD3D3D3),
                unfocusedIndicatorColor = Color(0xFFD3D3D3),
                cursorColor = Color(0xFF008D96),
                focusedLeadingIconColor = Color(0xFF7D7A7A),
                unfocusedLeadingIconColor = Color(0xFF7D7A7A),
            )
        )

        // Mostrar sugerencias
        if (showSuggestions && suggestions.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionItem(
                        suggestion = suggestion,
                        placesClient = placesClient,
                        sessionToken = sessionToken,
                        onSelect = { address, lat, lon ->
                            onValueChange(address)
                            onAddressSelected(address, lat, lon)
                            showSuggestions = false
                            suggestions = emptyList()
                        }
                    )
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 8.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: PlacePrediction,
    placesClient: com.google.android.libraries.places.api.net.PlacesClient,
    sessionToken: AutocompleteSessionToken,
    onSelect: (address: String, lat: Double?, lon: Double?) -> Unit
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Obtener coordenadas del lugar seleccionado
                scope.launch {
                    try {
                        val placeFields = listOf(
                            Place.Field.ADDRESS,
                            Place.Field.LAT_LNG
                        )
                        val request = FetchPlaceRequest.builder(suggestion.placeId, placeFields)
                            .setSessionToken(sessionToken)
                            .build()

                        val response = placesClient.fetchPlace(request).await()
                        val place = response.place

                        val address = place.address ?: suggestion.fullText
                        val latitude = place.latLng?.latitude
                        val longitude = place.latLng?.longitude

                        onSelect(address, latitude, longitude)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Si falla, usar el texto completo sin coordenadas
                        onSelect(suggestion.fullText, null, null)
                    }
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                suggestion.mainText,
                style = TextStyle(fontSize = 14.sp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            if (suggestion.secondaryText.isNotEmpty()) {
                Text(
                    suggestion.secondaryText,
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFF999999))
                )
            }
        }
    }

    Divider(modifier = Modifier.padding(vertical = 4.dp))
}
```

### Paso 3: Usar en RegisterCollab.kt

Reemplaza el TextField actual de dirección:

```kotlin
// EN: RegisterCollab.kt - En la sección de dirección

var addressLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
var addressLongitude by rememberSaveable { mutableStateOf<Double?>(null) }

// ... en lugar del OutlinedTextField para dirección:

item { Label("Dirección del Negocio") }
item {
    FocusBringIntoView {
        AddressAutocompleteTextField(
            value = address,
            onValueChange = { address = it },
            onAddressSelected = { selectedAddress, lat, lon ->
                address = selectedAddress
                addressLatitude = lat
                addressLongitude = lon
            },
            modifier = it.fillMaxWidth(),
            placeholder = "Busca tu dirección...",
            country = "MX"
        )
    }
}
```

---

## 🗺️ Implementación en BranchLocationPicker.kt

```kotlin
// EN: BranchLocationPicker.kt (MEJORADO CON AUTOCOMPLETADO)

@Composable
fun BranchLocationPickerDialog(
    branch: Branch,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchAddress by remember { mutableStateOf("") }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    var cameraPosition by remember {
        mutableStateOf(
            CameraPosition.Builder()
                .target(LatLng(19.4326, -99.1332)) // Default CDMX
                .zoom(12f)
                .build()
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra de búsqueda con autocompletado
                AddressAutocompleteTextField(
                    value = searchAddress,
                    onValueChange = { searchAddress = it },
                    onAddressSelected = { address, lat, lon ->
                        searchAddress = address
                        if (lat != null && lon != null) {
                            selectedLatitude = lat
                            selectedLongitude = lon
                            cameraPosition = CameraPosition.Builder()
                                .target(LatLng(lat, lon))
                                .zoom(15f)
                                .build()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = "Busca una dirección para la sucursal"
                )

                // Google Map
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cameraPositionState = rememberCameraPositionState {
                        position = cameraPosition
                    },
                    onMapClick = { latLng ->
                        selectedLatitude = latLng.latitude
                        selectedLongitude = latLng.longitude
                    }
                ) {
                    if (selectedLatitude != null && selectedLongitude != null) {
                        Marker(
                            state = MarkerState(
                                position = LatLng(selectedLatitude!!, selectedLongitude!!)
                            ),
                            title = searchAddress.ifEmpty { "Nueva ubicación" }
                        )
                    }
                }

                // Botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Text("Cancelar", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            if (selectedLatitude != null && selectedLongitude != null) {
                                val location = "($selectedLongitude,$selectedLatitude)"
                                onLocationSelected(location)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedLatitude != null && selectedLongitude != null
                    ) {
                        Text("Confirmar ubicación")
                    }
                }
            }
        }
    }
}
```

---

## 🔑 Configuración Final

### 1. AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application
        android:allowBackup="true"
        android:debuggable="true"
        ...>

        <!-- Google Maps API Key -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="AIzaSyD...YOUR_API_KEY..." />

        <!-- Places API será automáticamente inicializado -->

        <activity
            android:name=".MainActivity"
            ...>
```

### 2. MainActivity.kt - Inicializar Places

```kotlin
// EN: MainActivity.kt (onCreate)

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Inicializar Places API
    if (!Places.isInitialized()) {
        Places.initialize(applicationContext, "YOUR_API_KEY")
    }

    setContent {
        BeneficioJovenTheme {
            // ... resto del código
        }
    }
}
```

---

## 📊 API Requests vs Places SDK

### ❌ Backend API (Sin usar)
```
Usuario escribe en el campo
  → Envía request al backend
  → Backend consulta Google Places API
  → Retorna resultados al cliente
  → Latencia: 300-500ms
  → Costo: Múltiples requests
```

### ✅ Maps SDK for Android (Usamos esto)
```
Usuario escribe en el campo
  → Places SDK consulta directamente Google Places
  → Resultados en cliente
  → Latencia: 100-200ms
  → Costo: Sesión agrupada (menor costo)
```

---

## 🧪 Testing

### Prueba 1: Autocompletado en RegisterCollab
```
1. Abre la app
2. Ve a Registro de Colaborador
3. Escribe "Av. Ref" en dirección
4. Deben aparecer sugerencias
5. Selecciona una
6. Campo se llena automáticamente ✓
```

### Prueba 2: Autocompletado en BranchLocationPicker
```
1. Abre gestión de sucursales
2. Abre diálogo de agregar sucursal
3. Escribe dirección en campo de búsqueda
4. Deben aparecer sugerencias
5. Selecciona una
6. Mapa se centra en esa ubicación ✓
7. Confirma ubicación
8. Se guarda con coordenadas ✓
```

### Prueba 3: Sin conexión
```
Si no hay internet, mostrar error graceful
- "No hay conexión a internet"
- Permitir escribir dirección manualmente
- Campo de mapa sigue funcionando
```

---

## 💡 Mejoras Opcionales

### 1. Debounce mejorado
```kotlin
// Esperar 500ms después de dejar de escribir
var debounceJob by remember { mutableStateOf<Job?>(null) }

onValueChange = { newValue ->
    onValueChange(newValue)
    debounceJob?.cancel()
    debounceJob = scope.launch {
        delay(500)
        // Hacer búsqueda
    }
}
```

### 2. Caché de resultados
```kotlin
val autocompleteCache = remember { mutableMapOf<String, List<PlacePrediction>>() }

if (autocompleteCache.containsKey(newValue)) {
    suggestions = autocompleteCache[newValue]!!
} else {
    // Buscar y guardar en caché
    suggestions = buscar(newValue)
    autocompleteCache[newValue] = suggestions
}
```

### 3. Mostrar icono de "cargando"
```kotlin
if (isLoading) {
    CircularProgressIndicator(
        modifier = Modifier.size(16.dp)
    )
}
```

### 4. Limitar número de sugerencias
```kotlin
suggestions = response.autocompletePredictions
    .take(5)  // Solo 5 primeras sugerencias
    .map { ... }
```

---

## 📈 Cuotas y Costos

### Google Places API (Maps SDK for Android):
- **Autocomplete queries**: $0.002286 por sesión
- **Place Details**: $0.0051 por request
- **Sesiones agrupadas**: Reduce costo significativamente

### Ejemplo de costo:
```
100 usuarios registrándose / día
+ 20 búsquedas de sucursales / día
= 120 sesiones de Places
= $0.27 /día ≈ $8 /mes
```

---

## 🚀 Checklist de Implementación

- [ ] Agregar dependencias a build.gradle
- [ ] Habilitar APIs en Google Cloud Console
- [ ] Crear AddressAutocompleteTextField.kt
- [ ] Agregar meta-data a AndroidManifest.xml
- [ ] Actualizar RegisterCollab.kt con autocompletado
- [ ] Actualizar BranchLocationPicker.kt con autocompletado
- [ ] Inicializar Places en MainActivity
- [ ] Probar autocompletado en dirección
- [ ] Probar autocompletado en mapa
- [ ] Probar sin internet (error graceful)

---

## 📚 Documentación Relacionada

- `GEOCODING_GUIDE.md` - Geocodificación automática
- `IMPLEMENTACION_RESUMEN.md` - Resumen general
- `INTEGRACION_MOBILE.md` - Integración de mapas

---

**Status:** ✅ LISTO PARA IMPLEMENTAR
**Tecnología:** Google Places API for Android (Maps SDK)
**Complejidad:** Media
**Tiempo estimado:** 3-4 horas
