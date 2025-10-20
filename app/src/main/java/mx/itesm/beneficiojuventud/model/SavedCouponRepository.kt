package mx.itesm.beneficiojuventud.model

import android.util.Log
import mx.itesm.beneficiojuventud.model.RoomDB.Bookings.BookingDao
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryDao
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategories
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategoriesDao
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionDao
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity
import mx.itesm.beneficiojuventud.model.bookings.Booking
import mx.itesm.beneficiojuventud.model.bookings.BookingStatus
import mx.itesm.beneficiojuventud.model.bookings.RemoteServiceBooking
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.model.users.UserApiService
import mx.itesm.beneficiojuventud.utils.toBooking
import mx.itesm.beneficiojuventud.utils.toBookingList
import mx.itesm.beneficiojuventud.utils.toCategoryEntityList
import mx.itesm.beneficiojuventud.utils.toEntity
import mx.itesm.beneficiojuventud.utils.toPromotionList

class SavedCouponRepository(
    private val promotionDao: PromotionDao,
    private val categoryDao: CategoryDao,
    private val promotionCategoriesDao: PromotionCategoriesDao,
    private val bookingDao: BookingDao
) {

    suspend fun favoriteCoupon(couponId: Int, userId: String) {
        try {
            RemoteServiceUser.favoritePromotion(couponId, userId)
            val promo: Promotions = RemoteServicePromos.getPromotionById(couponId)

            // Insertar promoción
            promotionDao.insertPromotions(promo.toEntity(isReserved = false))

            // Insertar categorías si existen
            promo.categories?.let { categories ->
                val categoryEntities = categories.toCategoryEntityList()
                categoryDao.insertCategory(*categoryEntities.toTypedArray())

                // Insertar relaciones en la tabla junction
                categories.forEach { category ->
                    category.id?.let { catId ->
                        promotionCategoriesDao.insertPromotionCategory(
                            PromotionCategories(
                                promotionId = couponId,
                                categoryId = catId
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to save coupon", e)
            throw e
        }
    }

    suspend fun unfavoriteCoupon(couponId: Int, userId: String) {
        try {
            RemoteServiceUser.unfavoritePromotion(couponId, userId)

            // Eliminar relaciones de categorías
            promotionCategoriesDao.deleteAllCategoriesForPromotion(couponId)

            // Eliminar promoción
            promotionDao.deleteById(couponId)
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to unfavorite coupon", e)
            throw e
        }
    }

    suspend fun getFavoriteCoupons(userId: String): List<Promotions> {
        try {
            return RemoteServiceUser.getFavoritePromotions(userId)
        } catch (e: Exception) {
            val entityPromos: List<PromotionWithCategories> = promotionDao.getFavoritePromotions()
            return entityPromos.toPromotionList()
        }
    }

    suspend fun createBooking(booking: Booking) {
        try {
            val createdBooking = RemoteServiceBooking.createBooking(booking)
            val promo: Promotions = RemoteServicePromos.getPromotionById(booking.promotionId!!)

            // Insertar booking en la base de datos local
            bookingDao.insertBooking(createdBooking.toEntity())

            // Insertar promoción como reservada
            promotionDao.insertPromotions(promo.toEntity(isReserved = true))

            // Insertar categorías si existen
            promo.categories?.let { categories ->
                val categoryEntities = categories.toCategoryEntityList()
                categoryDao.insertCategory(*categoryEntities.toTypedArray())

                // Insertar relaciones en la tabla junction
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
            Log.e("CouponRepository", "Failed to book coupon", e)
            throw e
        }
    }

    suspend fun getBookings(userId: String): List<Booking> {
        try {
            // Primero obtener los bookings locales (para preservar cancelledDate)
            val localBookings = bookingDao.getBookingsByUser(userId).toBookingList()
            val localCancelledIds = localBookings.filter { it.status == BookingStatus.CANCELLED }.map { it.bookingId }.toSet()

            // Obtener bookings activos del servidor
            val serverBookings = RemoteServiceBooking.getUserBookings(userId)

            // Guardar bookings del servidor localmente, pero NO sobrescribir los cancelados
            serverBookings.forEach { booking ->
                if (booking.bookingId !in localCancelledIds) {
                    bookingDao.insertBooking(booking.toEntity())
                }
            }

            // Obtener TODOS los bookings locales actualizados (incluyendo cancelados con su cancelledDate)
            val updatedLocalBookings = bookingDao.getBookingsByUser(userId).toBookingList()
            Log.d("CouponRepository", "Bookings cargados: ${serverBookings.size} del servidor, ${updatedLocalBookings.size} locales (${localCancelledIds.size} cancelados preservados)")

            // Retornar todos los bookings locales (incluye activos y cancelados con cancelledDate preservado)
            return updatedLocalBookings
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to fetch bookings from server, using local", e)
            return bookingDao.getBookingsByUser(userId).toBookingList()
        }
    }

    suspend fun getReservedPromotions(userId: String): List<Promotions> {
        try {
            // Obtener bookings del servidor y cargar las promociones asociadas
            val bookings = RemoteServiceBooking.getUserBookings(userId)
            return bookings.mapNotNull { booking ->
                booking.promotionId?.let { promotionId ->
                    try {
                        RemoteServicePromos.getPromotionById(promotionId)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            return promotionDao.getReservedPromotions().toPromotionList()
        }
    }

    suspend fun cancelBooking(bookingId: Int, promotionId: Int, cancelledDate: String) {
        try {
            Log.d("CouponRepository", "Cancelando booking $bookingId con cancelledDate: $cancelledDate")
            RemoteServiceBooking.deleteBooking(bookingId)

            // Actualizar booking local con estado CANCELLED y fecha de cancelación
            val booking = bookingDao.getBookingById(bookingId)
            if (booking != null) {
                val updatedBooking = booking.copy(
                    status = BookingStatus.CANCELLED,
                    cancelledDate = cancelledDate
                )
                Log.d("CouponRepository", "Actualizando booking: status=${updatedBooking.status}, cancelledDate=${updatedBooking.cancelledDate}")
                bookingDao.updateBooking(updatedBooking)

                // Verificar que se guardó correctamente
                val verified = bookingDao.getBookingById(bookingId)
                Log.d("CouponRepository", "Booking verificado después de update: status=${verified?.status}, cancelledDate=${verified?.cancelledDate}")
            } else {
                Log.w("CouponRepository", "Booking $bookingId no encontrado en base de datos local")
            }

            // Eliminar relaciones de categorías
            promotionCategoriesDao.deleteAllCategoriesForPromotion(promotionId)

            // Eliminar promoción reservada
            promotionDao.deleteById(promotionId)
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to cancel booking", e)
            throw e
        }
    }

    suspend fun updateBooking(bookingId: Int): Booking {
        try {
            val updatedBooking = RemoteServiceBooking.updateBooking(bookingId)
            // Actualizar en base de datos local
            bookingDao.updateBooking(updatedBooking.toEntity())
            return updatedBooking
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to update booking", e)
            throw e
        }
    }

    suspend fun getBookingById(bookingId: Int): Booking {
        try {
            val booking = RemoteServiceBooking.getOneBooking(bookingId)
            // Guardar en base de datos local
            bookingDao.insertBooking(booking.toEntity())
            return booking
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to fetch booking from server, using local", e)
            return bookingDao.getBookingById(bookingId)?.toBooking()
                ?: throw Exception("Booking not found locally")
        }
    }

    suspend fun isFavorite(promotionId: Int): Boolean {
        return try {
            promotionDao.exists(promotionId)
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to check if favorite", e)
            false
        }
    }

    suspend fun clearAllFavorites() {
        try {
            promotionDao.deleteAllFavorites()
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to clear favorites", e)
            throw e
        }
    }

    suspend fun clearAllReserved() {
        try {
            promotionDao.deleteAllReserved()
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to clear reserved", e)
            throw e
        }
    }
}