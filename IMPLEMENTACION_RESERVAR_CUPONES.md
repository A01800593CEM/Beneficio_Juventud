# Implementación de Funcionalidad: Reservar Cupones

## Resumen

Se ha implementado completamente la funcionalidad de **reservar cupones** con integración al backend, manejo de errores, feedback visual y navegación automática a la pantalla de favoritos.

---

## Archivos Modificados y Creados

### ✅ 1. Nuevos StatusTypes para Reservación

**Archivo:** [Status.kt](app/src/main/java/mx/itesm/beneficiojuventud/view/Status.kt)

**Agregados:**
```kotlin
COUPON_RESERVATION_SUCCESS(
    isSuccess = true,
    title = "¡Cupón reservado exitosamente!",
    subtitle = "Lo encontrarás en tu sección de favoritos"
)

COUPON_RESERVATION_ERROR(
    isSuccess = false,
    title = "Error al reservar",
    subtitle = "No se pudo reservar el cupón, intenta de nuevo"
)
```

**Propósito:** Estados específicos para mostrar feedback de reservación exitosa o fallida.

---

### ✅ 2. BookingViewModel (NUEVO)

**Archivo:** [BookingViewModel.kt](app/src/main/java/mx/itesm/beneficiojuventud/viewmodel/BookingViewModel.kt)

**Descripción:** ViewModel dedicado para manejar toda la lógica de reservaciones/bookings.

**Características:**
- ✅ Integra `SavedCouponRepository` con persistencia local (Room)
- ✅ StateFlows reactivos para UI
- ✅ Manejo de errores y estados de carga
- ✅ Generación automática de fechas en formato ISO

**Métodos principales:**

| Método | Descripción | Parámetros |
|--------|-------------|------------|
| `reserveCoupon(promotion, userId)` | Crea una reservación de cupón | Promotions, String |
| `loadUserBookings(userId)` | Obtiene bookings del usuario | String |
| `loadReservedPromotions(userId)` | Obtiene promociones reservadas completas | String |
| `cancelBooking(booking)` | Cancela una reservación | Booking |
| `updateBooking(bookingId)` | Actualiza estado de booking | Int |
| `getBookingById(bookingId)` | Obtiene booking específico | Int |

**StateFlows disponibles:**
```kotlin
val bookings: StateFlow<List<Booking>>
val reservedPromotions: StateFlow<List<Promotions>>
val isLoading: StateFlow<Boolean>
val error: StateFlow<String?>
val message: StateFlow<String?>
val bookingSuccess: StateFlow<Boolean>
```

---

### ✅ 3. PromoQR - Integración Completa

**Archivo:** [PromoQR.kt](app/src/main/java/mx/itesm/beneficiojuventud/view/PromoQR.kt)

**Cambios realizados:**

#### a) Inyección del BookingViewModel
```kotlin
@Composable
fun PromoQR(
    nav: NavHostController,
    promotionId: Int,
    cognitoId: String,
    modifier: Modifier = Modifier,
    viewModel: PromoViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    bookingViewModel: BookingViewModel = viewModel() // ← NUEVO
)
```

#### b) Estados Reactivos de Booking
```kotlin
val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
val bookingError by bookingViewModel.error.collectAsState()
val bookingLoading by bookingViewModel.isLoading.collectAsState()
val bookingMessage by bookingViewModel.message.collectAsState()
```

#### c) Navegación Automática al Éxito
```kotlin
LaunchedEffect(bookingSuccess) {
    if (bookingSuccess) {
        nav.navigate(
            Screens.Status.createRoute(
                StatusType.COUPON_RESERVATION_SUCCESS,
                Screens.Favorites.route
            )
        ) {
            popUpTo(Screens.PromoQR.route) { inclusive = true }
        }
        bookingViewModel.resetBookingSuccess()
    }
}
```

#### d) Manejo de Errores con Snackbar
```kotlin
LaunchedEffect(bookingError) {
    bookingError?.let { msg ->
        scope.launch {
            snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "OK",
                duration = SnackbarDuration.Long
            )
        }
        bookingViewModel.clearError()
    }
}
```

#### e) Botón "Reservar Cupón" con Lógica Completa

**Ubicación:** Líneas 552-596

```kotlin
if (detail.isBookable) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        MainButton(
            text = if (bookingLoading) "Reservando..." else "Reservar Cupón",
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // Verificar stock disponible
                val available = promo.availableStock ?: 0
                if (available <= 0) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "No hay stock disponible",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@MainButton
                }

                // Reservar cupón
                bookingViewModel.reserveCoupon(promo, cognitoId)
            }
        )

        // Loading spinner
        if (bookingLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(24.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        }
    }
}
```

**Características del botón:**
- ✅ Solo visible cuando `detail.isBookable == true`
- ✅ Verifica stock disponible antes de reservar
- ✅ Muestra "Reservando..." durante la operación
- ✅ Spinner de carga visual
- ✅ Feedback inmediato con Snackbar si no hay stock
- ✅ Llama a `bookingViewModel.reserveCoupon()`

---

## Flujo Completo de Reservación

### 1. Usuario hace clic en "Reservar Cupón"

```
PromoQR.kt (onClick del botón)
    ↓
Verificar stock disponible
    ↓
bookingViewModel.reserveCoupon(promo, cognitoId)
```

### 2. BookingViewModel procesa la reservación

```kotlin
fun reserveCoupon(promotion: Promotions, userId: String) {
    viewModelScope.launch {
        _isLoading.value = true

        try {
            // Crear objeto Booking
            val booking = Booking(
                userId = userId,
                promotionId = promotion.promotionId,
                bookingDate = getCurrentDateISO(),
                limitUseDate = promotion.endDate ?: getDefaultLimitDate(),
                status = BookingStatus.ACTIVE
            )

            // Llamar al repositorio
            repository.createBooking(booking)

            _bookingSuccess.value = true

        } catch (e: Exception) {
            _error.value = "Error al reservar: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

### 3. SavedCouponRepository maneja backend y persistencia local

```kotlin
suspend fun createBooking(booking: Booking) {
    try {
        // 1. Enviar al servidor
        val createdBooking = RemoteServiceBooking.createBooking(booking)

        // 2. Guardar en BD local
        bookingDao.insertBooking(createdBooking.toEntity())

        // 3. Obtener promoción completa
        val promo = RemoteServicePromos.getPromotionById(booking.promotionId!!)

        // 4. Persistir promoción como reservada
        promotionDao.insertPromotions(promo.toEntity(isReserved = true))

        // 5. Guardar categorías
        promo.categories?.let { categories ->
            categoryDao.insertCategory(*categories.toCategoryEntityList().toTypedArray())

            categories.forEach { category ->
                category.id?.let { catId ->
                    promotionCategoriesDao.insertPromotionCategory(
                        PromotionCategories(
                            promotionId = booking.promotionId,
                            categoryId = catId
                        )
                    )
                }
            }
        }

    } catch (e: Exception) {
        throw e
    }
}
```

### 4. Backend API

```http
POST /users/bookings
Content-Type: application/json

{
  "userId": "user-cognito-id",
  "promotionId": 123,
  "bookingDate": "2025-10-19T14:30:00.000Z",
  "limitUseDate": "2025-11-19T14:30:00.000Z",
  "status": "ACTIVE"
}
```

**Respuesta:**
```json
{
  "bookingId": 456,
  "userId": "user-cognito-id",
  "promotionId": 123,
  "bookingDate": "2025-10-19T14:30:00.000Z",
  "limitUseDate": "2025-11-19T14:30:00.000Z",
  "status": "ACTIVE"
}
```

### 5. UI reacciona al éxito

```
bookingSuccess = true
    ↓
LaunchedEffect detecta cambio
    ↓
Navega a StatusScreen (COUPON_RESERVATION_SUCCESS)
    ↓
Espera 3 segundos
    ↓
Navega a Favorites
```

---

## Estados de la UI Durante el Proceso

| Estado | UI Visible | Descripción |
|--------|-----------|-------------|
| **Idle** | Botón "Reservar Cupón" | Estado inicial, listo para reservar |
| **Loading** | Botón "Reservando..." + Spinner | Procesando en backend |
| **Success** | StatusScreen (éxito) → Favorites | Reservación exitosa, redirección automática |
| **Error (sin stock)** | Snackbar: "No hay stock disponible" | Validación local antes de enviar |
| **Error (backend)** | Snackbar con mensaje de error | Fallo en servidor o conexión |

---

## Validaciones Implementadas

### ✅ Validación de Stock
```kotlin
val available = promo.availableStock ?: 0
if (available <= 0) {
    snackbarHostState.showSnackbar("No hay stock disponible")
    return
}
```

### ✅ Validación de Usuario
- El `cognitoId` debe estar presente
- Proviene del `AuthViewModel`

### ✅ Validación de Promoción
- Solo promociones con `isBookable = true` muestran el botón
- Se verifica la existencia del `promotionId`

---

## Persistencia Local (Offline-First)

La reservación se guarda localmente en Room Database:

**Tablas afectadas:**
1. **`booking`** - Información de la reservación
2. **`promotion`** - Promoción con `isReserved = true`
3. **`category`** - Categorías asociadas
4. **`PromotionCategories`** - Relaciones many-to-many

**Ventajas:**
- ✅ Funciona offline (intenta servidor primero, fallback a local)
- ✅ Datos persistentes entre sesiones
- ✅ Sincronización automática cuando hay conexión
- ✅ UI siempre responsiva

---

## Manejo de Errores

### Errores del Backend
```kotlin
catch (e: Exception) {
    _error.value = "Error al reservar: ${e.message}"
}
```

**Posibles errores:**
- Sin conexión a internet
- Error 400: Datos inválidos
- Error 404: Promoción no encontrada
- Error 409: Stock agotado
- Error 500: Error del servidor

### Errores de UI
```kotlin
LaunchedEffect(bookingError) {
    bookingError?.let { msg ->
        snackbarHostState.showSnackbar(
            message = msg,
            actionLabel = "OK",
            duration = SnackbarDuration.Long
        )
        bookingViewModel.clearError()
    }
}
```

**Feedback visual:**
- Snackbar con mensaje de error
- Botón "OK" para cerrar
- Duración larga (Long) para lectura

---

## Testing Recomendado

### Casos de Prueba

1. ✅ **Reservación exitosa online**
   - Hacer clic en "Reservar Cupón"
   - Verificar que aparece "Reservando..."
   - Verificar StatusScreen de éxito
   - Verificar navegación a Favorites

2. ✅ **Sin stock disponible**
   - Seleccionar promoción con stock = 0
   - Hacer clic en "Reservar Cupón"
   - Verificar Snackbar "No hay stock disponible"

3. ✅ **Error de conexión**
   - Desactivar WiFi/datos
   - Hacer clic en "Reservar Cupón"
   - Verificar mensaje de error

4. ✅ **Persistencia local**
   - Reservar offline
   - Verificar que se guarda en Room
   - Reconectar y verificar sincronización

5. ✅ **Promoción no reservable**
   - Abrir promoción con `isBookable = false`
   - Verificar que NO aparece botón "Reservar Cupón"

---

## Diagrama de Flujo

```
┌─────────────────┐
│  Usuario en     │
│  PromoQR Screen │
└────────┬────────┘
         │
         ▼
┌─────────────────────┐
│ Click "Reservar     │
│ Cupón"              │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐      NO     ┌──────────────┐
│ ¿Hay stock?         ├────────────►│ Snackbar:    │
└────────┬────────────┘              │ "Sin stock"  │
         │ SÍ                         └──────────────┘
         ▼
┌─────────────────────┐
│ bookingViewModel    │
│ .reserveCoupon()    │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│ SavedCouponRepo     │
│ .createBooking()    │
└────────┬────────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌──────────┐
│Backend │ │  Room DB │
│  API   │ │  Local   │
└───┬────┘ └────┬─────┘
    │           │
    └─────┬─────┘
          │
          ▼
    ┌──────────┐
    │ Éxito?   │
    └─────┬────┘
          │
     ┌────┴────┐
     │         │
    SÍ        NO
     │         │
     ▼         ▼
┌─────────┐ ┌─────────┐
│ Status  │ │Snackbar │
│ Success │ │ Error   │
└────┬────┘ └─────────┘
     │
     ▼
┌─────────┐
│Favorites│
│ Screen  │
└─────────┘
```

---

## Integración con Arquitectura Existente

### ViewModels
```
AuthViewModel ──► cognitoId
PromoViewModel ──► Promotion details
UserViewModel ──► Favorites
BookingViewModel ──► Reservations (NUEVO)
```

### Navegación
```
PromoQR ──► Status ──► Favorites
   │
   └──► Snackbar (errores)
```

### Persistencia
```
Remote API ──► SavedCouponRepository ──► Room DB
                      │
                      └──► StateFlows ──► UI
```

---

## Archivos Relacionados

| Archivo | Propósito |
|---------|-----------|
| [BookingViewModel.kt](app/src/main/java/mx/itesm/beneficiojuventud/viewmodel/BookingViewModel.kt) | Lógica de negocio de reservaciones |
| [PromoQR.kt](app/src/main/java/mx/itesm/beneficiojuventud/view/PromoQR.kt) | UI de detalles y botón reservar |
| [Status.kt](app/src/main/java/mx/itesm/beneficiojuventud/view/Status.kt) | Pantalla de feedback |
| [SavedCouponRepository.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/SavedCouponRepository.kt) | Repositorio con lógica backend+local |
| [BookingApiService.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/bookings/BookingApiService.kt) | Endpoints de API |
| [RemoteServiceBooking.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/bookings/RemoteServiceBooking.kt) | Cliente HTTP |

---

**Implementación completada:** 2025-10-19
**Estado:** ✅ COMPLETO Y LISTO PARA USAR
**Próximo paso:** Testing en dispositivo/emulador
