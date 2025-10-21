package mx.itesm.beneficiojuventud.model

import android.util.Log
import kotlinx.coroutines.flow.Flow
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionDao
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity
import mx.itesm.beneficiojuventud.model.bookings.Booking
import mx.itesm.beneficiojuventud.model.bookings.RemoteServiceBooking
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.model.users.UserApiService
import mx.itesm.beneficiojuventud.utils.toEntity
import mx.itesm.beneficiojuventud.utils.toPromotionList

class SavedCouponRepository(
    private val promotionDao: PromotionDao,
) {


    suspend fun favoritePromotion(couponId: Int, userId: String) {
        try {
            RemoteServiceUser.favoritePromotion(couponId, userId)
            val promo: Promotions = RemoteServicePromos.getPromotionById(couponId)
            promotionDao.insertPromotions(promo.toEntity())
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to save coupon", e)
            throw e
        }
    }

    suspend fun unfavoritePromotion(couponId: Int, userId: String) {
        try {
            RemoteServiceUser.unfavoritePromotion(couponId, userId)
            val promo: Promotions = RemoteServicePromos.getPromotionById(couponId)
            promotionDao.deletePromotions(promo.toEntity())
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to save coupon", e)
            throw e
        }
    }

    suspend fun getFavoritePromotions(userId: String): List<Promotions> {
        try {
            return RemoteServiceUser.getFavoritePromotions(userId)
        } catch (e: Exception) {
            val entityPromos: List<PromotionWithCategories> = promotionDao.getFavoritePromotions()
            return entityPromos.toPromotionList()
        }
    }

    suspend fun createBooking(booking: Booking) {
        try {
            RemoteServiceBooking.createBooking(booking)
            val promo: Promotions = RemoteServicePromos.getPromotionById(booking.promotionId!!)
            promotionDao.insertPromotions(promo.toEntity(true))
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to book coupon", e)
            throw e
        }
    }

    suspend fun getReservedPromotions(userId: String): List<Promotions> {
        try {
            return RemoteServiceBooking.getReservedPromotions(userId)
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to get reserved promotions from remote, checking local cache", e)
            val entityPromos: List<PromotionWithCategories> = promotionDao.getReservedPromotions()
            return entityPromos.toPromotionList()
        }
    }

    suspend fun cancelBooking(bookingId: Int, promotionId: Int) {
        try {
            RemoteServiceBooking.cancelBooking(bookingId)
            val promo: Promotions = RemoteServicePromos.getPromotionById(promotionId)
            promotionDao.deletePromotions(promo.toEntity())
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to cancel booking", e)
            throw e
        }
    }

    suspend fun updateBooking(bookingId: Int, status: mx.itesm.beneficiojuventud.model.bookings.BookingStatus): Booking {
        try {
            return RemoteServiceBooking.updateBooking(bookingId, status)
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to update booking", e)
            throw e
        }
    }

    suspend fun getBookingById(bookingId: Int): Booking {
        try {
            return RemoteServiceBooking.getBookingById(bookingId)
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to get booking by id", e)
            throw e
        }
    }


}