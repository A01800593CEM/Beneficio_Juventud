# 📱 Integración en App Móvil - Mostrar Mapa con Punto

## 📌 Ubicación Actual

La funcionalidad de geocodificación automática está **100% lista en el backend**. Cuando un colaborador se registra, la sucursal se crea automáticamente CON el punto en el mapa.

Ahora necesitamos mostrar esto en la app móvil.

---

## 🎯 Qué Mostrar

Después de que el usuario se registra, cuando vaya a **BranchManagementScreen** o vea sus sucursales, debería ver:

```
┌─────────────────────────┐
│ Mi Primera Sucursal     │
├─────────────────────────┤
│                         │
│    📍 [MAPA]           │
│   Con punto rojo       │
│   en la ubicación      │
│                         │
├─────────────────────────┤
│ Pizzería La Bella       │
│ 📍 Av. Reforma 123      │
│ 📞 5555551234           │
│ Coordenadas:           │
│ (-99.1452, 19.4263)    │
└─────────────────────────┘
```

---

## 🔧 Cambios Necesarios en Mobile

### 1. **Actualizar BranchLocationPicker.kt** (RECOMENDADO)

**Archivo:** `app/src/main/java/mx/itesm/beneficiojuventud/viewcollab/BranchLocationPicker.kt`

Cambio: Si la sucursal YA tiene coordenadas (de geocodificación automática), mostrar el mapa en esa ubicación en lugar de esperar que el usuario seleccione.

```kotlin
// EN: BranchLocationPicker.kt

@Composable
fun BranchLocationPickerDialog(
    branch: Branch,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Si la sucursal ya tiene ubicación (geocodificada)
    var initialLocation by remember {
        mutableStateOf(
            if (branch.location != null) {
                // Parsear "(-99.1452, 19.4263)" a LatLng
                val coords = parseLocationString(branch.location)
                if (coords != null) {
                    LatLng(coords.latitude, coords.longitude)
                } else null
            } else null
        )
    }

    // Usar initialLocation para centrar el mapa
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.Builder()
                .target(initialLocation ?: LatLng(19.4326, -99.1332)) // Default: CDMX
                .zoom(initialLocation?.let { 15f } ?: 12f) // Zoom más cerca si hay ubicación
                .build()
        }
    )
}
```

### 2. **Actualizar BranchManagementScreen.kt** (SUGERIDO)

**Archivo:** `app/src/main/java/mx/itesm/beneficiojuventud/viewcollab/BranchManagementScreen.kt`

Mostrar indicador de "Ubicación geocodificada automáticamente" para la primera sucursal.

```kotlin
// Agregar después de cargar las sucursales:

branches.forEachIndexed { index, branch ->
    BranchCard(
        branch = branch,
        isFirstBranch = index == 0,
        isAutoGeocoded = index == 0 && branch.location != null,
        onEdit = { ... }
    )
}

// Crear composable para BranchCard:
@Composable
fun BranchCard(
    branch: Branch,
    isFirstBranch: Boolean = false,
    isAutoGeocoded: Boolean = false,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        branch.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    if (isFirstBranch) {
                        Text(
                            "Primera sucursal",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF008D96)
                            )
                        )
                    }

                    if (isAutoGeocoded) {
                        Row(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Text(
                                "Ubicación geocodificada",
                                fontSize = 10.sp,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }

            Text(branch.address, fontSize = 14.sp)
            Text(branch.phone, fontSize = 14.sp)

            if (branch.location != null) {
                Text(
                    "📍 ${branch.location}",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    "⚠️ Sin ubicación en el mapa",
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}
```

### 3. **Crear API Method para ver primera sucursal** (OPCIONAL)

Si quieres una pantalla especial que muestre la primera sucursal con mapa:

```kotlin
// EN: BranchViewModel.kt

@Composable
fun FirstBranchMapScreen(
    collabId: String,
    navController: NavHostController,
    viewModel: BranchViewModel = viewModel()
) {
    val branches by viewModel.branches.collectAsState()
    val firstBranch = branches.firstOrNull()

    LaunchedEffect(collabId) {
        viewModel.getBranchesByCollaborator(collabId)
    }

    if (firstBranch == null) {
        Text("Cargando...")
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Tu Primera Sucursal",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(16.dp)
        )

        // Mostrar mapa si hay ubicación
        if (firstBranch.location != null) {
            GoogleMapView(
                branch = firstBranch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        // Detalles de la sucursal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(firstBranch.name, fontWeight = FontWeight.Bold)
                Text(firstBranch.address)
                Text(firstBranch.phone)

                if (firstBranch.location != null) {
                    Text(
                        "✓ Ubicación geocodificada automáticamente",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                navController.navigate(Screens.BranchManagement.route)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text("Ir a Gestión de Sucursales")
        }
    }
}
```

---

## 📊 Datos que Vienen del Backend

Cuando el usuario se registra, el backend retorna las coordenadas de la sucursal:

```json
{
  "branchId": 1,
  "collaboratorId": "user-123",
  "name": "Pizzería La Bella Italia",
  "phone": "5555551234",
  "address": "Av. Reforma 123, CDMX",
  "zipCode": "06500",
  "location": "(-99.1452, 19.4263)",  // ← Coordenadas geocodificadas
  "state": "ACTIVE"
}
```

### Parsear coordenadas en Kotlin:

```kotlin
// EN: Utils.kt o Extension.kt

fun parseLocationString(locationString: String?): Coordinates? {
    if (locationString == null) return null

    // Formato esperado: "(-99.1452, 19.4263)"
    val regex = """^\(?\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*\)?$""".toRegex()
    val match = regex.find(locationString) ?: return null

    return try {
        val longitude = match.groupValues[1].toDouble()
        val latitude = match.groupValues[2].toDouble()
        Coordinates(latitude, longitude)
    } catch (e: Exception) {
        null
    }
}

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)
```

---

## 🗺️ Mostrar en Google Maps

### Opción 1: Usar Google Maps Compose (RECOMENDADO)

```kotlin
@Composable
fun GoogleMapWithMarker(branch: Branch) {
    val coordinates = parseLocationString(branch.location)

    if (coordinates == null) {
        Text("Sin ubicación en el mapa")
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(LatLng(coordinates.latitude, coordinates.longitude))
            .zoom(15f)
            .build()
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(
                position = LatLng(coordinates.latitude, coordinates.longitude)
            ),
            title = branch.name,
            snippet = branch.address
        )
    }
}
```

### Opción 2: Usar Fragment de Maps (Tradicional)

```kotlin
// En BranchLocationPicker.kt, usar MapFragment como ya está
// Solo necesita parsear la ubicación y centrar el mapa
```

---

## 🎯 Flujo de Integración

```
1. Usuario se registra en RegisterCollab
   ↓
2. Backend crea Collaborator + Branch con geocodificación
   ↓
3. App navega a HomeScreenCollab
   ↓
4. Usuario puede:
   a) Ver un "Splash" mostrando primera sucursal con mapa
   b) Ir directo a BranchManagementScreen
   ↓
5. En BranchManagementScreen:
   - Mostrar tarjeta "Primera sucursal"
   - Indicar que está "Geocodificada automáticamente"
   - Mostrar mapa con punto rojo
   ↓
6. Usuario puede:
   - Hacer clic para ver mapa ampliado
   - Editar ubicación si quiere
   - Agregar más sucursales
```

---

## 📋 Checklist de Implementación

- [ ] Crear función `parseLocationString()` en Utils
- [ ] Crear `Coordinates` data class
- [ ] Actualizar `BranchLocationPickerDialog` para mostrar ubicación inicial
- [ ] Actualizar `BranchCard` para indicar "geocodificada automáticamente"
- [ ] Crear `GoogleMapWithMarker()` Composable
- [ ] (Opcional) Crear pantalla de "Primera sucursal" con mapa
- [ ] Probar con dirección real (ej: "Av. Reforma 123, CDMX")
- [ ] Probar con dirección que falle en geocodificación

---

## 🧪 Testing en Mobile

### Prueba 1: Verificar que se carguen coordenadas
```kotlin
// Cuando cargues la primera sucursal, verifica:
val branch = branches.firstOrNull()
println("Location: ${branch?.location}")  // Debe ser: "(-99.1452, 19.4263)"
```

### Prueba 2: Parsear coordenadas
```kotlin
val coords = parseLocationString("(-99.1452, 19.4263)")
println("Lat: ${coords?.latitude}, Lon: ${coords?.longitude}")
// Debe imprimir: Lat: 19.4263, Lon: -99.1452
```

### Prueba 3: Mostrar en mapa
```kotlin
// Navega a BranchManagementScreen
// Verifica que el mapa muestre un punto en la ubicación correcta
```

---

## 💡 Tips y Mejoras

### Mejora 1: Mostrar "Geocodificada automáticamente"
```kotlin
if (isFirstBranch && branch.location != null) {
    Badge(
        backgroundColor = Color(0xFFE8F5E9),
        contentColor = Color(0xFF4CAF50)
    ) {
        Text("Geocodificada automáticamente", fontSize = 10.sp)
    }
}
```

### Mejora 2: Zoom inteligente
```kotlin
// Si hay ubicación, acercar más (zoom 15)
// Si no hay ubicación, mostrar toda la ciudad (zoom 12)
val zoom = if (branch.location != null) 15f else 12f
```

### Mejora 3: Mostrar botón "Actualizar ubicación"
```kotlin
// Si no tiene ubicación:
Button(onClick = { openGeocodeDialog() }) {
    Text("Agregar ubicación en mapa")
}

// Si tiene ubicación:
Button(onClick = { openGeocodeDialog() }) {
    Text("Actualizar ubicación en mapa")
}
```

---

## 🔗 Integración con Endpoints

### Obtener sucursales con ubicación geocodificada:
```kotlin
// GET /branch/collaborator/:collaboratorId
// Retorna array de Branch
// Cada Branch incluye: location (con coordenadas geocodificadas)
```

### Actualizar ubicación manualmente:
```kotlin
// PATCH /branch/:id/geocode?address=Dirección&country=MX
// Retorna Branch actualizada con nuevas coordenadas
```

---

## 📖 Documentación Relacionada

- `GEOCODING_GUIDE.md` - Guía completa de geocodificación
- `IMPLEMENTACION_RESUMEN.md` - Resumen de lo que se implementó
- `BranchLocationPicker.kt` - Código existente que puedes reutilizar

---

**Estado:** ✅ Listo para integración
**Tiempo estimado:** 2-3 horas
**Complejidad:** Media
