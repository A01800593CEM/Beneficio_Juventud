package mx.itesm.beneficiojuventud.model.RoomDB.Categories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    suspend fun getAll(): List<CategoryEntity>
    @Query("SELECT * FROM category WHERE categoryId = :categoryId")
    suspend fun findById(categoryId: Int): CategoryEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(vararg gategories: CategoryEntity)
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}