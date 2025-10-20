package mx.itesm.beneficiojuventud.model

import android.util.Log
import kotlinx.coroutines.flow.Flow
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryDao
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategories
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategoriesDao
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionDao
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity
import mx.itesm.beneficiojuventud.model.bookings.Booking
import mx.itesm.beneficiojuventud.model.bookings.RemoteServiceBooking
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.RemoteServicePromos
import mx.itesm.beneficiojuventud.model.users.RemoteServiceUser
import mx.itesm.beneficiojuventud.model.users.UserApiService
import mx.itesm.beneficiojuventud.utils.downloadAndSaveImage
import mx.itesm.beneficiojuventud.utils.deletePromotionImage
import mx.itesm.beneficiojuventud.utils.toCategoryEntityList
import mx.itesm.beneficiojuventud.utils.toEntity
import mx.itesm.beneficiojuventud.utils.toPromotionList
import mx.itesm.beneficiojuventud.model.categories.RemoteServiceCategory
import android.content.Context

class SavedCouponRepository(
    private val context: Context,
    private val promotionDao: PromotionDao,
    private val categoryDao: CategoryDao,
    private val promotionCategoriesDao: PromotionCategoriesDao
) {


    suspend fun favoritePromotion(couponId: Int, userId: String) {
        try {
            RemoteServiceUser.favoritePromotion(couponId, userId)
            val promo: Promotions = RemoteServicePromos.getPromotionById(couponId)

            // Download and save image to file
            val imagePath = promo.promotionId?.let { promoId ->
                downloadAndSaveImage(context, promo.imageUrl, promoId)
            }
            val entity = promo.toEntity(isReserved = false, imagePath = imagePath)

            // Save the promotion
            promotionDao.insertPromotions(entity)

            // Save categories (if they don't already exist)
            val categoryEntities = promo.categories.toCategoryEntityList()
            categoryDao.insertCategory(*categoryEntities.toTypedArray())

            // Save the junction table entries
            promo.promotionId?.let { promoId ->
                val junctionEntries = promo.categories.mapNotNull { category ->
                    category.id?.let { categoryId ->
                        PromotionCategories(categoryId = categoryId, promotionId = promoId)
                    }
                }
                if (junctionEntries.isNotEmpty()) {
                    promotionCategoriesDao.insertPromotionCategories(*junctionEntries.toTypedArray())
                }
            }

            Log.d("CouponRepository", "Successfully saved promotion ${promo.promotionId} to RoomDB with ${promo.categories.size} categories and image: $imagePath")
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to save coupon", e)
            throw e
        }
    }

    suspend fun unfavoritePromotion(couponId: Int, userId: String) {
        try {
            RemoteServiceUser.unfavoritePromotion(couponId, userId)
            val promo: Promotions = RemoteServicePromos.getPromotionById(couponId)

            // Delete the image file from storage
            deletePromotionImage(context, couponId)

            // Delete junction table entries first
            promotionCategoriesDao.deletePromotionCategories(couponId)

            // Then delete the promotion
            promotionDao.deletePromotions(promo.toEntity())
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to delete coupon", e)
            throw e
        }
    }

    suspend fun getFavoritePromotions(userId: String): List<Promotions> {
        // First, always check what's in RoomDB
        val entityPromos: List<PromotionWithCategories> = promotionDao.getFavoritePromotions()
        Log.d("CouponRepository", "RoomDB currently has ${entityPromos.size} favorite promotions")

        try {
            Log.d("CouponRepository", "Attempting to fetch favorites from server for user: $userId")
            val serverPromos = RemoteServiceUser.getFavoritePromotions(userId)
            Log.d("CouponRepository", "Successfully fetched ${serverPromos.size} favorites from server")
            return serverPromos
        } catch (e: Exception) {
            Log.w("CouponRepository", "Failed to fetch from server, falling back to RoomDB", e)
            val localPromos = entityPromos.toPromotionList()
            Log.d("CouponRepository", "Returning ${localPromos.size} favorites from RoomDB")
            return localPromos
        }
    }

    suspend fun createBooking(booking: Booking) {
        try {
            RemoteServiceBooking.createBooking(booking)
            val promo: Promotions = RemoteServicePromos.getPromotionById(booking.promotionId!!)

            // Download and save image to file
            val imagePath = promo.promotionId?.let { promoId ->
                downloadAndSaveImage(context, promo.imageUrl, promoId)
            }
            val entity = promo.toEntity(isReserved = true, imagePath = imagePath)

            // Save the promotion
            promotionDao.insertPromotions(entity)

            // Save categories (if they don't already exist)
            val categoryEntities = promo.categories.toCategoryEntityList()
            categoryDao.insertCategory(*categoryEntities.toTypedArray())

            // Save the junction table entries
            promo.promotionId?.let { promoId ->
                val junctionEntries = promo.categories.mapNotNull { category ->
                    category.id?.let { categoryId ->
                        PromotionCategories(categoryId = categoryId, promotionId = promoId)
                    }
                }
                if (junctionEntries.isNotEmpty()) {
                    promotionCategoriesDao.insertPromotionCategories(*junctionEntries.toTypedArray())
                }
            }

            Log.d("CouponRepository", "Successfully saved booking ${promo.promotionId} to RoomDB with ${promo.categories.size} categories and image: $imagePath")
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to book coupon", e)
            throw e
        }
    }

    /**
     * Fetches all categories from the server and populates the local database.
     * This should be called when the app has internet connectivity to ensure
     * categories are available for offline use.
     */
    suspend fun syncCategoriesFromServer() {
        try {
            Log.d("CouponRepository", "Syncing categories from server...")
            val categories = RemoteServiceCategory.getCategories()
            val categoryEntities = categories.toCategoryEntityList()
            categoryDao.insertCategory(*categoryEntities.toTypedArray())
            Log.d("CouponRepository", "Successfully synced ${categories.size} categories to RoomDB")
        } catch (e: Exception) {
            Log.e("CouponRepository", "Failed to sync categories from server", e)
            // Don't throw - this is a background sync operation
        }
    }


}