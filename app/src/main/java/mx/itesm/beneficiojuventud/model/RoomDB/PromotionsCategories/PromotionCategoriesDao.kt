package mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PromotionCategoriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotionCategory(vararg promotionCategories: PromotionCategories)

    @Delete
    suspend fun deletePromotionCategory(promotionCategory: PromotionCategories)

    @Query("DELETE FROM PromotionCategories WHERE promotionId = :promotionId")
    suspend fun deleteAllCategoriesForPromotion(promotionId: Int)

    @Query("DELETE FROM PromotionCategories WHERE categoryId = :categoryId")
    suspend fun deleteAllPromotionsForCategory(categoryId: Int)

    @Query("SELECT * FROM PromotionCategories WHERE promotionId = :promotionId")
    suspend fun getCategoriesForPromotion(promotionId: Int): List<PromotionCategories>

    @Query("SELECT * FROM PromotionCategories WHERE categoryId = :categoryId")
    suspend fun getPromotionsForCategory(categoryId: Int): List<PromotionCategories>
}
