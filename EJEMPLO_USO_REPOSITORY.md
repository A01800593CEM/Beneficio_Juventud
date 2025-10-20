# Ejemplo de Uso del SavedCouponRepository

## Inicialización del Repositorio

### 1. Crear la instancia de la base de datos (Application o DI)

```kotlin
// En tu Application class o módulo de inyección de dependencias
class BeneficioJuventudApp : Application() {

    // Singleton de la base de datos
    val database: LocalDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            LocalDatabase::class.java,
            "beneficio_juventud_db"
        )
        .fallbackToDestructiveMigration() // Solo para desarrollo
        .build()
    }

    // Singleton del repositorio
    val savedCouponRepository: SavedCouponRepository by lazy {
        SavedCouponRepository(
            promotionDao = database.promotionDao(),
            categoryDao = database.categoryDao(),
            promotionCategoriesDao = database.promotionCategoriesDao(),
            bookingDao = database.bookingDao()
        )
    }
}
```

### 2. Acceder al repositorio en un ViewModel

```kotlin
class PromotionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BeneficioJuventudApp).savedCouponRepository
    private val userId = "user123" // Obtener del usuario actual

    // LiveData para observar favoritos
    private val _favorites = MutableLiveData<List<Promotions>>()
    val favorites: LiveData<List<Promotions>> = _favorites

    // LiveData para observar bookings
    private val _bookings = MutableLiveData<List<Booking>>()
    val bookings: LiveData<List<Booking>> = _bookings

    // ... continúa abajo
}
```

---

## Casos de Uso Implementados

### 📌 CASO 1: Marcar/Desmarcar Favoritos

```kotlin
class PromotionsViewModel : ViewModel() {

    fun toggleFavorite(promotionId: Int, userId: String) {
        viewModelScope.launch {
            try {
                // Verificar si ya es favorito
                val isFav = repository.isFavorite(promotionId)

                if (isFav) {
                    // Desmarcar favorito
                    repository.unfavoriteCoupon(promotionId, userId)
                    _message.value = "Promoción eliminada de favoritos"
                } else {
                    // Marcar como favorito
                    repository.favoriteCoupon(promotionId, userId)
                    _message.value = "Promoción guardada en favoritos"
                }

                // Recargar favoritos
                loadFavorites(userId)

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            try {
                val favs = repository.getFavoriteCoupons(userId)
                _favorites.value = favs
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar favoritos"
            }
        }
    }
}
```

**En el Composable/Fragment:**
```kotlin
@Composable
fun PromotionCard(promotion: Promotions, viewModel: PromotionsViewModel) {
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(promotion.promotionId) {
        // Verificar si es favorito al cargar
        isFavorite = viewModel.repository.isFavorite(promotion.promotionId ?: 0)
    }

    Card {
        // ... contenido de la tarjeta

        IconButton(
            onClick = {
                viewModel.toggleFavorite(
                    promotionId = promotion.promotionId ?: 0,
                    userId = currentUserId
                )
                isFavorite = !isFavorite
            }
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorito"
            )
        }
    }
}
```

---

### 🎫 CASO 2: Reservar un Cupón

```kotlin
class BookingViewModel : ViewModel() {

    fun reserveCoupon(promotion: Promotions, userId: String) {
        viewModelScope.launch {
            try {
                // Crear objeto Booking
                val booking = Booking(
                    userId = userId,
                    promotionId = promotion.promotionId,
                    bookingDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
                    limitUseDate = promotion.endDate, // o calcular fecha límite
                    status = BookingStatus.ACTIVE
                )

                // Crear reservación
                repository.createBooking(booking)

                _message.value = "¡Cupón reservado exitosamente!"
                _bookingSuccess.value = true

                // Recargar bookings del usuario
                loadUserBookings(userId)

            } catch (e: Exception) {
                _error.value = "Error al reservar: ${e.message}"
            }
        }
    }

    fun loadUserBookings(userId: String) {
        viewModelScope.launch {
            try {
                val bookings = repository.getBookings(userId)
                _bookings.value = bookings
            } catch (e: Exception) {
                _error.value = "Error al cargar reservaciones"
            }
        }
    }
}
```

**En el Composable:**
```kotlin
@Composable
fun PromotionDetailScreen(
    promotion: Promotions,
    viewModel: BookingViewModel
) {
    Column {
        // ... detalles de la promoción

        Button(
            onClick = {
                viewModel.reserveCoupon(
                    promotion = promotion,
                    userId = currentUserId
                )
            },
            enabled = promotion.availableStock ?: 0 > 0
        ) {
            Text("Reservar Cupón")
        }
    }
}
```

---

### 📋 CASO 3: Ver Mis Reservaciones

```kotlin
@Composable
fun MyBookingsScreen(viewModel: BookingViewModel) {
    val bookings by viewModel.bookings.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadUserBookings(currentUserId)
    }

    LazyColumn {
        items(bookings) { booking ->
            BookingCard(
                booking = booking,
                onCancel = { viewModel.cancelBooking(booking) },
                onUse = { viewModel.useBooking(booking) }
            )
        }
    }
}

class BookingViewModel : ViewModel() {

    fun cancelBooking(booking: Booking) {
        viewModelScope.launch {
            try {
                repository.cancelBooking(
                    bookingId = booking.bookingId ?: return@launch,
                    promotionId = booking.promotionId ?: return@launch
                )

                _message.value = "Reservación cancelada"
                loadUserBookings(currentUserId)

            } catch (e: Exception) {
                _error.value = "Error al cancelar: ${e.message}"
            }
        }
    }

    fun useBooking(booking: Booking) {
        viewModelScope.launch {
            try {
                // Actualizar estado del booking a "USED"
                val updatedBooking = repository.updateBooking(
                    bookingId = booking.bookingId ?: return@launch
                )

                _message.value = "¡Cupón utilizado!"
                loadUserBookings(currentUserId)

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }
}
```

---

### ⭐ CASO 4: Pantalla de Favoritos

```kotlin
@Composable
fun FavoritesScreen(viewModel: PromotionsViewModel) {
    val favorites by viewModel.favorites.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    LaunchedEffect(Unit) {
        viewModel.loadFavorites(currentUserId)
    }

    if (isLoading) {
        CircularProgressIndicator()
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(favorites) { promotion ->
                PromotionCard(
                    promotion = promotion,
                    isFavorite = true,
                    onFavoriteClick = {
                        viewModel.toggleFavorite(
                            promotionId = promotion.promotionId ?: 0,
                            userId = currentUserId
                        )
                    },
                    onClick = {
                        navController.navigate("promotion/${promotion.promotionId}")
                    }
                )
            }
        }
    }
}
```

---

### 🔄 CASO 5: Sincronización Manual (Pull to Refresh)

```kotlin
@Composable
fun FavoritesScreen(viewModel: PromotionsViewModel) {
    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            viewModel.syncFavorites(currentUserId)
        }
    )

    Box(Modifier.pullRefresh(refreshState)) {
        LazyColumn {
            // ... lista de favoritos
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

class PromotionsViewModel : ViewModel() {

    fun syncFavorites(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Fuerza la obtención del servidor
                val serverFavorites = RemoteServiceUser.getFavoritePromotions(userId)

                // Limpia favoritos locales
                repository.clearAllFavorites()

                // Guarda los nuevos
                serverFavorites.forEach { promo ->
                    repository.favoriteCoupon(promo.promotionId ?: 0, userId)
                }

                _favorites.value = serverFavorites
                _message.value = "Favoritos sincronizados"

            } catch (e: Exception) {
                _error.value = "Error de sincronización"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
```

---

### 🎯 CASO 6: Ver Promociones Reservadas (con detalles completos)

```kotlin
@Composable
fun ReservedPromotionsScreen(viewModel: BookingViewModel) {
    val reservedPromotions by viewModel.reservedPromotions.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadReservedPromotions(currentUserId)
    }

    LazyColumn {
        items(reservedPromotions) { promotion ->
            ReservedPromotionCard(
                promotion = promotion,
                onViewDetails = {
                    navController.navigate("promotion/${promotion.promotionId}")
                }
            )
        }
    }
}

class BookingViewModel : ViewModel() {

    private val _reservedPromotions = MutableLiveData<List<Promotions>>()
    val reservedPromotions: LiveData<List<Promotions>> = _reservedPromotions

    fun loadReservedPromotions(userId: String) {
        viewModelScope.launch {
            try {
                // Este método retorna List<Promotions> con todos los detalles
                val promos = repository.getReservedPromotions(userId)
                _reservedPromotions.value = promos
            } catch (e: Exception) {
                _error.value = "Error al cargar promociones reservadas"
            }
        }
    }
}
```

---

### 🔍 CASO 7: Obtener Detalles de un Booking Específico

```kotlin
class BookingDetailsViewModel : ViewModel() {

    private val _bookingDetails = MutableLiveData<Booking>()
    val bookingDetails: LiveData<Booking> = _bookingDetails

    fun loadBookingDetails(bookingId: Int) {
        viewModelScope.launch {
            try {
                val booking = repository.getBookingById(bookingId)
                _bookingDetails.value = booking
            } catch (e: Exception) {
                _error.value = "No se pudo cargar el booking"
            }
        }
    }
}

@Composable
fun BookingDetailsScreen(bookingId: Int, viewModel: BookingDetailsViewModel) {
    val booking by viewModel.bookingDetails.observeAsState()

    LaunchedEffect(bookingId) {
        viewModel.loadBookingDetails(bookingId)
    }

    booking?.let { b ->
        Column {
            Text("ID: ${b.bookingId}")
            Text("Fecha de reserva: ${b.bookingDate}")
            Text("Válido hasta: ${b.limitUseDate}")
            Text("Estado: ${b.status}")

            // Botones de acción
            Row {
                Button(onClick = { viewModel.cancelBooking(b) }) {
                    Text("Cancelar")
                }
                Button(onClick = { viewModel.useBooking(b) }) {
                    Text("Usar")
                }
            }
        }
    }
}
```

---

## 🛠️ Manejo de Estados

### ViewModel Completo de Ejemplo

```kotlin
class PromotionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BeneficioJuventudApp).savedCouponRepository

    // Estados
    private val _favorites = MutableLiveData<List<Promotions>>()
    val favorites: LiveData<List<Promotions>> = _favorites

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // Funciones principales
    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val favs = repository.getFavoriteCoupons(userId)
                _favorites.value = favs
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(promotionId: Int, userId: String) {
        viewModelScope.launch {
            try {
                val isFav = repository.isFavorite(promotionId)
                if (isFav) {
                    repository.unfavoriteCoupon(promotionId, userId)
                    _message.value = "Eliminado de favoritos"
                } else {
                    repository.favoriteCoupon(promotionId, userId)
                    _message.value = "Agregado a favoritos"
                }
                loadFavorites(userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessage() {
        _message.value = null
    }
}
```

---

## 📱 Observar Mensajes y Errores en la UI

```kotlin
@Composable
fun FavoritesScreen(viewModel: PromotionsViewModel) {
    val error by viewModel.error.observeAsState()
    val message by viewModel.message.observeAsState()
    val scaffoldState = rememberScaffoldState()

    // Mostrar errores
    LaunchedEffect(error) {
        error?.let {
            scaffoldState.snackbarHostState.showSnackbar(
                message = it,
                actionLabel = "OK"
            )
            viewModel.clearError()
        }
    }

    // Mostrar mensajes de éxito
    LaunchedEffect(message) {
        message?.let {
            scaffoldState.snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(scaffoldState = scaffoldState) {
        // ... contenido
    }
}
```

---

## 🎯 Resumen de Métodos Disponibles

| Método | Descripción | Retorna |
|--------|-------------|---------|
| `favoriteCoupon(couponId, userId)` | Marca como favorito | Unit |
| `unfavoriteCoupon(couponId, userId)` | Desmarca favorito | Unit |
| `getFavoriteCoupons(userId)` | Obtiene favoritos | List<Promotions> |
| `isFavorite(promotionId)` | Verifica si es favorito | Boolean |
| `clearAllFavorites()` | Limpia todos los favoritos | Unit |
| `createBooking(booking)` | Crea reservación | Unit |
| `getBookings(userId)` | Obtiene bookings del usuario | List<Booking> |
| `getReservedPromotions(userId)` | Obtiene promociones reservadas | List<Promotions> |
| `getBookingById(bookingId)` | Obtiene booking específico | Booking |
| `cancelBooking(bookingId, promotionId)` | Cancela reservación | Unit |
| `updateBooking(bookingId)` | Actualiza estado de booking | Booking |
| `clearAllReserved()` | Limpia reservaciones locales | Unit |

---

**Listo para implementar en tu app! 🚀**
