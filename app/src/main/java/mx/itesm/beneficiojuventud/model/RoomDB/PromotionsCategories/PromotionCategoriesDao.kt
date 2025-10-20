package mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PromotionCategoriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotionCategory(promotionCategory: PromotionCategories)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotionCategories(vararg promotionCategories: PromotionCategories)

    @Query("DELETE FROM PromotionCategories WHERE promotionId = :promotionId")
    suspend fun deletePromotionCategories(promotionId: Int)
}
