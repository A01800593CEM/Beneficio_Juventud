package mx.itesm.beneficiojuventud.model.promos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotion")
    suspend fun getAll(): List<PromotionEntity>

    @Query("SELECT * FROM promotion WHERE promotionId = :promotionId")
    suspend fun findById(promotionId: Int): PromotionEntity

    @Insert
    fun insertPromotions(promotion: PromotionEntity)

    @Delete
    fun deletePromotions(promotion: PromotionEntity)
}