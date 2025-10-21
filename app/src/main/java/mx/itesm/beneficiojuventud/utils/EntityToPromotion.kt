package mx.itesm.beneficiojuventud.utils

import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity
import mx.itesm.beneficiojuventud.model.promos.Promotions


/**
 * Converts a database [PromotionEntity] object into a business logic [Promotions] object.
 */
fun PromotionWithCategories.toPromotion(): Promotions {
    return Promotions(
        promotionId = this.promotion.promotionId,
        title = this.promotion.title,
        description = this.promotion.description,
        // The `image` field from the entity (ByteArray?) is not mapped back to the Promotions model,
        // which is common if the UI model uses an image URL or handles loading differently.
        imageUrl = this.promotion.imageUrl,
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
        // Note: We don't map `isReserved` back, as the `Promotions` class might not have this field.
        // If it does, you would add: isReserved = this.isReserved
    )
}

/**
 * Converts a list of database [PromotionEntity] objects into a list of business logic [Promotions] objects.
 */
fun List<PromotionWithCategories>.toPromotionList(): List<Promotions> {
    return this.map { it.toPromotion() }
}



