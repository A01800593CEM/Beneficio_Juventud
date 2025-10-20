package mx.itesm.beneficiojuventud.utils

import android.util.Base64
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity
import mx.itesm.beneficiojuventud.model.promos.Promotions


/**
 * Converts a database [PromotionEntity] object into a business logic [Promotions] object.
 */
fun PromotionWithCategories.toPromotion(): Promotions {
    // Use the file path directly - Coil can load from file:// URIs
    val imageUrl = this.promotion.imagePath?.let { path ->
        // Convert to file:// URI for Coil to load
        if (path.startsWith("file://")) {
            path
        } else {
            "file://$path"
        }
    }

    android.util.Log.d("EntityToPromotion", "Converting promotion ${this.promotion.promotionId}: image path = $imageUrl")

    return Promotions(
        promotionId = this.promotion.promotionId,
        title = this.promotion.title,
        description = this.promotion.description,
        imageUrl = imageUrl,  // Use file path for offline images
        initialDate = this.promotion.initialDate,
        endDate = this.promotion.endDate,
        promotionType = this.promotion.promotionType,
        promotionString = this.promotion.promotionString,
        totalStock = this.promotion.totalStock,
        availableStock = this.promotion.availableStock,
        limitPerUser = this.promotion.limitPerUser,
        dailyLimitPerUser = this.promotion.dailyLimitPerUser,
        promotionState = this.promotion.promotionState,
        isBookable = this.promotion.isBookable,
        theme = this.promotion.theme,
        businessName = this.promotion.businessName,
        categories = this.categories.toCategoryList()
    )
}

/**
 * Converts a list of database [PromotionEntity] objects into a list of business logic [Promotions] objects.
 */
fun List<PromotionWithCategories>.toPromotionList(): List<Promotions> {
    return this.map { it.toPromotion() }
}



