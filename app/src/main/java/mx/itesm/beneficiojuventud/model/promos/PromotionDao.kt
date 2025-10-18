package mx.itesm.beneficiojuventud.model.promos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotion")
    suspend fun getAll(): List<PromotionEntity>

    @Query("SELECT * FROM promotion WHERE promotionId = :promotionId")
    suspend fun findById(promotionId: Int): PromotionEntity

    @Insert
    suspend fun insertPromotions(vararg promotions: PromotionEntity)

    @Delete
    suspend fun deletePromotions(promotion: PromotionEntity)
}