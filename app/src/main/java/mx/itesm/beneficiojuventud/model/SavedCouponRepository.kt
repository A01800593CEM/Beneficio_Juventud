package mx.itesm.beneficiojuventud.model

import android.util.Log
import mx.itesm.beneficiojuventud.model.RoomDB.Bookings.BookingDao
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionDao
import mx.itesm.beneficiojuventud.model.bookings.Booking
import mx.itesm.beneficiojuventud.model.bookings.BookingStatus
import mx.itesm.beneficiojuventud.model.bookings.RemoteServiceBooking
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.utils.toBooking
import mx.itesm.beneficiojuventud.utils.toEntity
import mx.itesm.beneficiojuventud.utils.toPromotionList

/**
 * Repository implementing offline-first pattern for promotions and bookings.
 *
 * Strategy:
 * - Write operations: Try remote first, then cache on success
 * - Read operations: Try remote first, fall back to cache on failure
 * - Cache serves as offline backup for read operations
 */
class SavedCouponRepository(
    private val promotionDao: PromotionDao,
    private val bookingDao: BookingDao
) {

    // ============== FAVORITES: WRITE OPERATIONS ==============

    /**
     * Adds a promotion to favorites.
     * Remote-first: Updates server, then caches locally on success.
     */
    suspend fun favoritePromotion(couponId: Int, userId: String) {
        try {
            // Step 1: Update remote
            RemoteServiceUser.favoritePromotion(couponId, userId)

            // Step 2: Fetch full promotion details
            val promo: Promotions = RemoteServicePromos.getPromotionById(couponId)

            // Step 3: Cache locally (isReserved = false for favorites)
            promotionDao.insertPromotions(promo.toEntity(isReserved = false))

            Log.d("CouponRepository", "✅ Favorited promotion $couponId and cached locally")
        } catch (e: Exception) {
            Log.e("CouponRepository", "❌ Failed to favorite promotion $couponId", e)
            throw e
        }
    }

    /**
     * Removes a promotion from favorites.
     * Remote-first: Updates server, then removes from cache on success.
     */
    suspend fun unfavoritePromotion(couponId: Int, userId: String) {
        try {
            // Step 1: Update remote
            RemoteServiceUser.unfavoritePromotion(couponId, userId)

            // Step 2: Remove from local cache
            promotionDao.deleteById(couponId)

            Log.d("CouponRepository", "✅ Unfavorited promotion $couponId and removed from cache")
        } catch (e: Exception) {
            Log.e("CouponRepository", "❌ Failed to unfavorite promotion $couponId", e)
            throw e
        }
    }

    // ============== FAVORITES: READ OPERATIONS ==============

    /**
     * Gets favorite promotions.
     * Offline-first: Try remote, fall back to cache on failure.
     */
    suspend fun getFavoritePromotions(userId: String): List<Promotions> {
        return try {
            // Try remote first
            val remotePromos = RemoteServiceUser.getFavoritePromotions(userId)

            // Update cache with fresh data
            try {
                // Clear old favorites
                promotionDao.deleteAllFavorites()
                // Insert fresh ones
                remotePromos.forEach { promo ->
                    promotionDao.insertPromotions(promo.toEntity(isReserved = false))
                }
                Log.d("CouponRepository", "✅ Synced ${remotePromos.size} favorite promotions to cache")
            } catch (cacheError: Exception) {
                Log.w("CouponRepository", "⚠️ Failed to update cache after fetching favorites", cacheError)
            }

            remotePromos
        } catch (e: Exception) {
            Log.w("CouponRepository", "⚠️ Remote fetch failed, using cached favorites", e)

            // Fall back to cached data
            val entityPromos: List<PromotionWithCategories> = promotionDao.getFavoritePromotions()
            entityPromos.toPromotionList()
        }
    }

    // ============== BOOKINGS: WRITE OPERATIONS ==============

    /**
     * Creates a new booking.
     * Remote-first: Creates on server, then caches promotion and booking locally.
     * Automatically adds the promotion to favorites.
     */
    suspend fun createBooking(booking: Booking) {
        try {
            // Step 1: Create booking on server
            val createdBooking = RemoteServiceBooking.createBooking(booking)

            // Step 2: Fetch promotion details
            val promo: Promotions = RemoteServicePromos.getPromotionById(booking.promotionId!!)

            // Step 3: Cache promotion as reserved (isReserved = true)
            promotionDao.insertPromotions(promo.toEntity(isReserved = true))

            // Step 4: Automatically add to favorites (if not already favorited)
            // This ensures the coupon appears in the favorites list with "Reservado" badge
            try {
                RemoteServiceUser.favoritePromotion(booking.promotionId!!, booking.userId!!)
                Log.d("CouponRepository", "✅ Auto-favorited promotion ${booking.promotionId}")

                // Also ensure the favorite is saved to local cache by reloading favorites
                try {
                    val favPromos = RemoteServiceUser.getFavoritePromotions(booking.userId!!)
                    promotionDao.deleteAllFavorites()
                    favPromos.forEach { fav ->
                        promotionDao.insertPromotions(fav.toEntity(isReserved = false))
                    }
                    Log.d("CouponRepository", "✅ Reloaded favorites after auto-favorite")
                } catch (favCacheError: Exception) {
                    Log.w("CouponRepository", "⚠️ Failed to reload favorites after auto-favorite", favCacheError)
                }
            } catch (favError: Exception) {
                Log.w("CouponRepository", "⚠️ Failed to auto-favorite promotion (may already be favorited)", favError)
                // Non-critical error, don't throw
            }

            // Step 5: Sync ALL bookings from server to ensure consistency
            // This is crucial when the server reuses/reactivates a previously cancelled booking
            try {
                val allUserBookings = RemoteServiceBooking.getUserBookings(booking.userId!!)
                // Clear old bookings and insert fresh ones from server
                bookingDao.deleteAllByUser(booking.userId!!)
                allUserBookings.forEach { b ->
                    bookingDao.insertBooking(b.toEntity())
                }
                Log.d("CouponRepository", "✅ Synced ${allUserBookings.size} bookings from server after creation")
            } catch (syncError: Exception) {
                Log.w("CouponRepository", "⚠️ Failed to sync bookings after creation", syncError)
                // Fallback: at least insert the created booking
                bookingDao.insertBooking(createdBooking.toEntity())
            }

            Log.d("CouponRepository", "✅ Created booking ${createdBooking.bookingId} and cached locally")
        } catch (e: Exception) {
            Log.e("CouponRepository", "❌ Failed to create booking", e)
            throw e
        }
    }

    /**
     * Cancels an existing booking.
     * Remote-first: Cancels on server, then updates cache.
     * This will trigger cooldown on the server (10 seconds for testing).
     */
    suspend fun cancelBooking(bookingId: Int, promotionId: Int) {
        try {
            // Step 1: Cancel on server (triggers cooldown automatically)
            val cancelledBooking = RemoteServiceBooking.cancelBooking(bookingId)

            // Step 2: Update booking in cache (instead of deleting)
            // This preserves the cooldownUntil information needed for client-side cooldown check
            bookingDao.updateBooking(cancelledBooking.toEntity())

            // Step 3: Update promotion to NOT reserved (but keep in favorites if it was favorited)
            // Fetch fresh promotion details to ensure we have correct favorite status
            try {
                val promo: Promotions = RemoteServicePromos.getPromotionById(promotionId)
                // Update with isReserved = false (no longer reserved, but might still be favorited)
                promotionDao.insertPromotions(promo.toEntity(isReserved = false))
                Log.d("CouponRepository", "✅ Updated promotion $promotionId to non-reserved")
            } catch (promoError: Exception) {
                Log.w("CouponRepository", "⚠️ Failed to update promotion after cancellation", promoError)
                // Non-critical, don't throw
            }

            Log.d("CouponRepository", "✅ Cancelled booking $bookingId and updated cache. Cooldown initiated on server.")
        } catch (e: Exception) {
            Log.e("CouponRepository", "❌ Failed to cancel booking $bookingId", e)
            throw e
        }
    }

    /**
     * Updates booking status.
     * Remote-first with offline fallback for read.
     */
    suspend fun updateBooking(bookingId: Int, status: BookingStatus): Booking {
        return try {
            // Update remote
            val updatedBooking = RemoteServiceBooking.updateBooking(bookingId, status)

            // Update cache
            try {
                bookingDao.updateBooking(updatedBooking.toEntity())
                Log.d("CouponRepository", "✅ Updated booking $bookingId status to $status")
            } catch (cacheError: Exception) {
                Log.w("CouponRepository", "⚠️ Failed to update booking cache", cacheError)
            }

            updatedBooking
        } catch (e: Exception) {
            Log.w("CouponRepository", "⚠️ Remote update failed, checking cache", e)

            // Try to read from cache
            val cachedBooking = bookingDao.getBookingById(bookingId)
            if (cachedBooking != null) {
                Log.d("CouponRepository", "📦 Returning cached booking $bookingId")
                cachedBooking.toBooking()
            } else {
                Log.e("CouponRepository", "❌ Booking $bookingId not found in cache either")
                throw e
            }
        }
    }

    // ============== BOOKINGS: READ OPERATIONS ==============

    /**
     * Gets reserved/booked promotions.
     * Offline-first: Try remote, fall back to cache on failure.
     */
    suspend fun getReservedPromotions(userId: String): List<Promotions> {
        return try {
            // Try remote first
            val remotePromos = RemoteServiceBooking.getReservedPromotions(userId)

            // Update cache with fresh data
            try {
                // Clear old reservations
                promotionDao.deleteAllReserved()
                // Insert fresh ones
                remotePromos.forEach { promo ->
                    promotionDao.insertPromotions(promo.toEntity(isReserved = true))
                }
                Log.d("CouponRepository", "✅ Synced ${remotePromos.size} reserved promotions to cache")
            } catch (cacheError: Exception) {
                Log.w("CouponRepository", "⚠️ Failed to update cache after fetching reservations", cacheError)
            }

            remotePromos
        } catch (e: Exception) {
            Log.w("CouponRepository", "⚠️ Remote fetch failed, using cached reservations", e)

            // Fall back to cached data
            val entityPromos: List<PromotionWithCategories> = promotionDao.getReservedPromotions()
            entityPromos.toPromotionList()
        }
    }

    /**
     * Gets all bookings for a user.
     * Offline-first: Try remote, fall back to cache on failure.
     */
    suspend fun getUserBookings(userId: String): List<Booking> {
        return try {
            // Try remote first
            val remoteBookings = RemoteServiceBooking.getUserBookings(userId)

            // Update cache
            try {
                // Clear old bookings for this user
                bookingDao.deleteAllByUser(userId)
                // Insert fresh ones
                remoteBookings.forEach { booking ->
                    bookingDao.insertBooking(booking.toEntity())
                }
                Log.d("CouponRepository", "✅ Synced ${remoteBookings.size} bookings to cache")
            } catch (cacheError: Exception) {
                Log.w("CouponRepository", "⚠️ Failed to update booking cache", cacheError)
            }

            remoteBookings
        } catch (e: Exception) {
            Log.w("CouponRepository", "⚠️ Remote fetch failed, using cached bookings", e)

            // Fall back to cache
            val cachedBookings = bookingDao.getBookingsByUser(userId)
            cachedBookings.map { it.toBooking() }
        }
    }

    /**
     * Gets a specific booking by ID.
     * Offline-first: Try remote, fall back to cache on failure.
     */
    suspend fun getBookingById(bookingId: Int): Booking {
        return try {
            // Try remote first
            val remoteBooking = RemoteServiceBooking.getBookingById(bookingId)

            // Update cache
            try {
                bookingDao.insertBooking(remoteBooking.toEntity())
                Log.d("CouponRepository", "✅ Cached booking $bookingId")
            } catch (cacheError: Exception) {
                Log.w("CouponRepository", "⚠️ Failed to cache booking", cacheError)
            }

            remoteBooking
        } catch (e: Exception) {
            Log.w("CouponRepository", "⚠️ Remote fetch failed, checking cache for booking $bookingId", e)

            // Fall back to cache
            val cachedBooking = bookingDao.getBookingById(bookingId)
            if (cachedBooking != null) {
                Log.d("CouponRepository", "📦 Returning cached booking $bookingId")
                cachedBooking.toBooking()
            } else {
                Log.e("CouponRepository", "❌ Booking $bookingId not found in cache either")
                throw e
            }
        }
    }

    // ============== CACHE MANAGEMENT ==============

    /**
     * Clears all cached data (useful for logout or data reset).
     */
    suspend fun clearAllCache() {
        try {
            promotionDao.deleteAllFavorites()
            promotionDao.deleteAllReserved()
            bookingDao.deleteAll()
            Log.d("CouponRepository", "🗑️ Cleared all cache")
        } catch (e: Exception) {
            Log.e("CouponRepository", "❌ Failed to clear cache", e)
            throw e
        }
    }

    /**
     * Gets cache statistics for debugging.
     */
    suspend fun getCacheStats(): CacheStats {
        return try {
            CacheStats(
                favoritesCount = promotionDao.getFavoritePromotions().size,
                reservedCount = promotionDao.getReservedPromotions().size,
                bookingsCount = bookingDao.getBookingsByUser("").size // This needs userId, adjust as needed
            )
        } catch (e: Exception) {
            Log.e("CouponRepository", "❌ Failed to get cache stats", e)
            CacheStats(0, 0, 0)
        }
    }

    data class CacheStats(
        val favoritesCount: Int,
        val reservedCount: Int,
        val bookingsCount: Int
    )
}