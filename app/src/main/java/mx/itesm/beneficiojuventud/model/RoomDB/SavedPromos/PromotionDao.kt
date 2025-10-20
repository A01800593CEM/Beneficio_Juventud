package mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories

import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity

@Dao
interface PromotionDao {
    @Transaction
    @Query("SELECT * FROM promotion WHERE isReserved = 0")
    suspend fun getFavoritePromotions(): List<PromotionWithCategories>

    @Transaction
    @Query("SELECT * FROM promotion WHERE isReserved = 1")
    suspend fun getReservedPromotions(): List<PromotionWithCategories>

    @Transaction
    @Query("SELECT * FROM promotion WHERE promotionId = :promotionId")
    suspend fun findById(promotionId: Int): PromotionWithCategories?

    @Query("SELECT EXISTS(SELECT 1 FROM promotion WHERE promotionId = :promotionId)")
    suspend fun exists(promotionId: Int): Boolean

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertPromotions(vararg promotions: PromotionEntity)

    @androidx.room.Update
    suspend fun updatePromotion(promotion: PromotionEntity)

    @Delete
    suspend fun deletePromotions(promotion: PromotionEntity)

    @Query("DELETE FROM promotion WHERE promotionId = :promotionId")
    suspend fun deleteById(promotionId: Int)

    @Query("DELETE FROM promotion WHERE isReserved = 0")
    suspend fun deleteAllFavorites()

    @Query("DELETE FROM promotion WHERE isReserved = 1")
    suspend fun deleteAllReserved()
}