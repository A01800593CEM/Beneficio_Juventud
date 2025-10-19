package mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryEntity
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity

@Entity(primaryKeys = ["promotionId", "categoryId"])
data class PromotionCategories(
    val categoryId: Int,
    val promotionId: Int
)

data class PromotionWithCategories(
    @Embedded val promotion: PromotionEntity,
    @Relation(
        parentColumn = "promotionId", // From PromotionEntity
        entityColumn = "categoryId",   // From CategoryEntity
        associateBy = Junction(
            value = PromotionCategories::class, // The junction table class
            // --- THIS IS THE FIX ---
            // Explicitly define the columns in the junction table
            parentColumn = "promotionId",    // Column in PromotionCategories linking to PromotionEntity
            entityColumn = "categoryId"      // Column in PromotionCategories linking to CategoryEntity
        )
    )
    val categories: List<CategoryEntity>
)


