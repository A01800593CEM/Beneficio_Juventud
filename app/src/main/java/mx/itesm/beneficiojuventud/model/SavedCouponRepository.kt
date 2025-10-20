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


}