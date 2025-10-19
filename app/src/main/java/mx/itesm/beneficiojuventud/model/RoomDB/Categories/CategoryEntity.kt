package mx.itesm.beneficiojuventud.model.RoomDB.Categories

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey val categoryId: Int?,
    val name: String?
)
