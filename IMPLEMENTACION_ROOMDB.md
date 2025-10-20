# Implementación Completa de RoomDB y SavedCouponRepository

## Resumen de la Implementación

Se ha completado la implementación del sistema de persistencia local usando Room Database para manejar:
1. **Promociones favoritas** (guardadas por el usuario)
2. **Cupones reservados** (bookings activos del usuario)
3. **Categorías** asociadas a las promociones
4. **Sistema offline-first** con sincronización al servidor

---

## Estructura de la Base de Datos

### Versión: 2

### Entidades

#### 1. PromotionEntity
**Tabla:** `promotion`
**Archivo:** [PromotionEntity.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/SavedPromos/PromotionEntity.kt)

Campos:
- `promotionId` (Primary Key)
- `title`, `description`, `image`
- `initialDate`, `endDate`
- `promotionType`, `promotionString`
- `totalStock`, `availableStock`
- `limitPerUser`, `dailyLimitPerUser`
- `promotionState`, `isBookable`
- `theme`, `businessName`
- **`isReserved`**: Boolean que diferencia favoritos (false) de reservados (true)

#### 2. CategoryEntity
**Tabla:** `category`
**Archivo:** [CategoryEntity.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/Categories/CategoryEntity.kt)

Campos:
- `categoryId` (Primary Key)
- `name`

#### 3. PromotionCategories (Junction Table)
**Tabla:** `PromotionCategories`
**Archivo:** [PromotionCategories.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/PromotionsCategories/PromotionCategories.kt)

Campos:
- `promotionId` (Primary Key compuesta)
- `categoryId` (Primary Key compuesta)

Relación Many-to-Many entre Promotions y Categories.

#### 4. BookingEntity
**Tabla:** `booking`
**Archivo:** [BookingEntity.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/Bookings/BookingEntity.kt)

Campos:
- `bookingId` (Primary Key)
- `userId`
- `promotionId`
- `bookingDate`
- `limitUseDate`
- `status` (BookingStatus enum)

---

## DAOs (Data Access Objects)

### 1. PromotionDao
**Archivo:** [PromotionDao.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/SavedPromos/PromotionDao.kt:1)

Métodos implementados:
- ✅ `getFavoritePromotions()`: Obtiene promociones favoritas (isReserved = 0)
- ✅ `getReservedPromotions()`: Obtiene promociones reservadas (isReserved = 1)
- ✅ `findById(promotionId)`: Busca promoción por ID
- ✅ `exists(promotionId)`: Verifica si existe una promoción
- ✅ `insertPromotions(...)`: Inserta promociones (REPLACE on conflict)
- ✅ `updatePromotion(promotion)`: Actualiza una promoción
- ✅ `deletePromotions(promotion)`: Elimina una promoción
- ✅ `deleteById(promotionId)`: Elimina por ID
- ✅ `deleteAllFavorites()`: Limpia todos los favoritos
- ✅ `deleteAllReserved()`: Limpia todas las reservaciones

### 2. CategoryDao
**Archivo:** [CategoryDao.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/Categories/CategoryDao.kt:1)

Métodos:
- `getAll()`: Obtiene todas las categorías
- `findById(categoryId)`: Busca categoría por ID
- `insertCategory(...)`: Inserta categorías
- `deleteCategory(category)`: Elimina categoría

### 3. PromotionCategoriesDao (NUEVO)
**Archivo:** [PromotionCategoriesDao.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/PromotionsCategories/PromotionCategoriesDao.kt:1)

Métodos implementados:
- ✅ `insertPromotionCategory(...)`: Inserta relaciones (REPLACE on conflict)
- ✅ `deletePromotionCategory(promotionCategory)`: Elimina relación específica
- ✅ `deleteAllCategoriesForPromotion(promotionId)`: Elimina todas las categorías de una promoción
- ✅ `deleteAllPromotionsForCategory(categoryId)`: Elimina todas las promociones de una categoría
- ✅ `getCategoriesForPromotion(promotionId)`: Obtiene categorías de una promoción
- ✅ `getPromotionsForCategory(categoryId)`: Obtiene promociones de una categoría

### 4. BookingDao (NUEVO)
**Archivo:** [BookingDao.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/Bookings/BookingDao.kt:1)

Métodos implementados:
- ✅ `getBookingsByUser(userId)`: Obtiene todas las reservaciones de un usuario
- ✅ `getBookingById(bookingId)`: Obtiene una reservación por ID
- ✅ `getBookingByPromotionAndUser(promotionId, userId)`: Busca reservación específica
- ✅ `exists(bookingId)`: Verifica si existe una reservación
- ✅ `insertBooking(...)`: Inserta reservaciones (REPLACE on conflict)
- ✅ `updateBooking(booking)`: Actualiza una reservación
- ✅ `deleteBooking(booking)`: Elimina una reservación
- ✅ `deleteById(bookingId)`: Elimina por ID
- ✅ `deleteAllByUser(userId)`: Elimina todas las reservaciones de un usuario
- ✅ `deleteAll()`: Limpia todas las reservaciones

---

## SavedCouponRepository - API Completa

**Archivo:** [SavedCouponRepository.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/SavedCouponRepository.kt:1)

### Constructor
```kotlin
class SavedCouponRepository(
    private val promotionDao: PromotionDao,
    private val categoryDao: CategoryDao,
    private val promotionCategoriesDao: PromotionCategoriesDao,
    private val bookingDao: BookingDao
)
```

### Métodos de Favoritos

#### `favoriteCoupon(couponId: Int, userId: String)`
- Marca una promoción como favorita en el servidor
- Guarda la promoción localmente con `isReserved = false`
- Persiste las categorías asociadas
- Crea las relaciones en la tabla junction

#### `unfavoriteCoupon(couponId: Int, userId: String)`
- Elimina el favorito del servidor
- Elimina las relaciones de categorías
- Elimina la promoción local

#### `getFavoriteCoupons(userId: String): List<Promotions>`
- Intenta obtener favoritos del servidor
- En caso de error (offline), retorna los favoritos locales
- Patrón **offline-first**

#### `isFavorite(promotionId: Int): Boolean`
- Verifica si una promoción está guardada como favorita
- Útil para UI (mostrar corazón lleno/vacío)

#### `clearAllFavorites()`
- Elimina todos los favoritos locales

---

### Métodos de Reservaciones (Bookings)

#### `createBooking(booking: Booking)`
- Crea la reservación en el servidor
- Guarda el booking localmente
- Persiste la promoción como reservada (`isReserved = true`)
- Guarda las categorías asociadas

#### `getBookings(userId: String): List<Booking>`
- Obtiene reservaciones del servidor
- Sincroniza con la base de datos local
- En caso de error, retorna bookings locales
- **Retorna objetos Booking** (no Promotions)

#### `getReservedPromotions(userId: String): List<Promotions>`
- Obtiene bookings del servidor
- Carga las promociones asociadas a cada booking
- En caso de error, retorna promociones reservadas locales
- **Retorna objetos Promotions** (útil para mostrar tarjetas de promoción)

#### `getBookingById(bookingId: Int): Booking`
- Obtiene un booking específico del servidor
- Guarda en cache local
- En caso de error, busca en base de datos local

#### `cancelBooking(bookingId: Int, promotionId: Int)`
- Cancela la reservación en el servidor
- Elimina el booking local
- Elimina las categorías asociadas
- Elimina la promoción reservada local

#### `updateBooking(bookingId: Int): Booking`
- Actualiza el estado de un booking en el servidor
- Sincroniza con la base de datos local

#### `clearAllReserved()`
- Elimina todas las promociones reservadas locales

---

## Type Converters

**Archivo:** [Converters.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/Converters.kt:1)

Convierte enums a String para almacenamiento en Room:
- `PromotionType` ↔ String
- `PromotionState` ↔ String
- `PromoTheme` ↔ String
- `BookingStatus` ↔ String

---

## Funciones de Extensión (Utils)

### Conversión de Promociones
**Archivos:**
- [PromotionToEntity.kt](app/src/main/java/mx/itesm/beneficiojuventud/utils/PromotionToEntity.kt:1)
- [EntityToPromotion.kt](app/src/main/java/mx/itesm/beneficiojuventud/utils/EntityToPromotion.kt:1)

```kotlin
fun Promotions.toEntity(isReserved: Boolean = false): PromotionEntity
fun List<Promotions>.toEntityList(): List<PromotionEntity>
fun PromotionWithCategories.toPromotion(): Promotions
fun List<PromotionWithCategories>.toPromotionList(): List<Promotions>
```

### Conversión de Categorías
**Archivos:**
- [CategoryToEntity.kt](app/src/main/java/mx/itesm/beneficiojuventud/utils/CategoryToEntity.kt:1)
- [EntityToCategory.kt](app/src/main/java/mx/itesm/beneficiojuventud/utils/EntityToCategory.kt:1)

```kotlin
fun Category.toCategoryEntity(): CategoryEntity
fun List<Category>.toCategoryEntityList(): List<CategoryEntity>
fun CategoryEntity.toCategory(): Category
fun List<CategoryEntity>.toCategoryList(): List<Category>
```

### Conversión de Bookings (NUEVO)
**Archivos:**
- [BookingToEntity.kt](app/src/main/java/mx/itesm/beneficiojuventud/utils/BookingToEntity.kt:1)
- [EntityToBooking.kt](app/src/main/java/mx/itesm/beneficiojuventud/utils/EntityToBooking.kt:1)

```kotlin
fun Booking.toEntity(): BookingEntity
fun List<Booking>.toBookingEntityList(): List<BookingEntity>
fun BookingEntity.toBooking(): Booking
fun List<BookingEntity>.toBookingList(): List<Booking>
```

---

## Base de Datos Principal

**Archivo:** [LocalDatabase.kt](app/src/main/java/mx/itesm/beneficiojuventud/model/RoomDB/LocalDatabase.kt:1)

```kotlin
@Database(
    entities = [
        PromotionEntity::class,
        CategoryEntity::class,
        PromotionCategories::class,
        BookingEntity::class
    ],
    version = 2
)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun promotionDao(): PromotionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun promotionCategoriesDao(): PromotionCategoriesDao
    abstract fun bookingDao(): BookingDao
}
```

---

## Flujo de Datos - Offline First

### Guardar Favorito
1. Usuario marca promoción como favorita
2. Se envía al servidor (`RemoteServiceUser.favoritePromotion`)
3. Se obtiene la promoción completa del servidor
4. Se guarda localmente:
   - Promoción en tabla `promotion` con `isReserved = false`
   - Categorías en tabla `category`
   - Relaciones en tabla `PromotionCategories`

### Crear Reservación
1. Usuario reserva un cupón
2. Se crea en el servidor (`RemoteServiceBooking.createBooking`)
3. Se guarda localmente:
   - Booking en tabla `booking`
   - Promoción en tabla `promotion` con `isReserved = true`
   - Categorías y relaciones

### Obtener Datos (Patrón Offline-First)
```kotlin
try {
    // 1. Intenta obtener del servidor
    val data = remoteService.getData()
    // 2. Sincroniza con base de datos local
    localDao.insert(data)
    return data
} catch (e: Exception) {
    // 3. En caso de error (sin conexión), usa datos locales
    return localDao.getData()
}
```

---

## Próximos Pasos Recomendados

### 1. Sincronización Periódica
Considera implementar un WorkManager para sincronizar datos periódicamente:
```kotlin
class SyncWorker : CoroutineWorker() {
    override suspend fun doWork(): Result {
        // Sincronizar favoritos
        // Sincronizar bookings
        return Result.success()
    }
}
```

### 2. Observables (Flow/LiveData)
Convertir los DAOs a usar Flow para actualizaciones reactivas:
```kotlin
@Query("SELECT * FROM promotion WHERE isReserved = 0")
fun observeFavoritePromotions(): Flow<List<PromotionWithCategories>>
```

### 3. Migration Strategy
Al cambiar la estructura de la BD, crear migraciones:
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // SQL para migrar
    }
}
```

---

## Resumen de Archivos Nuevos Creados

✅ `PromotionCategoriesDao.kt` - DAO para tabla junction
✅ `BookingEntity.kt` - Entidad de reservaciones
✅ `BookingDao.kt` - DAO de reservaciones
✅ `Converters.kt` - Type converters para enums
✅ `BookingToEntity.kt` - Conversiones Booking → Entity
✅ `EntityToBooking.kt` - Conversiones Entity → Booking

## Archivos Modificados

✅ `PromotionDao.kt` - Agregados métodos CRUD completos
✅ `LocalDatabase.kt` - Actualizado a versión 2, agregado BookingDao
✅ `SavedCouponRepository.kt` - Implementación completa de todos los CRUDs

---

## Testing Recomendado

### Casos de prueba críticos:
1. ✅ Guardar favorito online → verificar persistencia local
2. ✅ Guardar favorito offline → verificar que falle apropiadamente
3. ✅ Obtener favoritos offline → verificar que use cache local
4. ✅ Crear booking → verificar persistencia de booking + promoción
5. ✅ Cancelar booking → verificar eliminación completa
6. ✅ Sincronización de categorías correcta

---

**Implementación completada el:** 2025-10-19
**Versión de Base de Datos:** 2
**Estado:** ✅ COMPLETO Y LISTO PARA USAR
