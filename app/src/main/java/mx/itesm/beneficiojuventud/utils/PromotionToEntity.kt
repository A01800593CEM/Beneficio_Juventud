package mx.itesm.beneficiojuventud.utils

import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.utils.toCategoryEntityList


fun Promotions.toEntity(isReserved: Boolean = false) : PromotionEntity {
    return PromotionEntity(
        promotionId = this.promotionId,
        title = this.title,
        description = this.description,
        image = null,
        initialDate = this.initialDate,
        endDate = this.endDate,
        promotionType = this.promotionType,
        promotionString = this.promotionString,
        totalStock = this.totalStock,
        availableStock = this.availableStock,
        limitPerUser = this.limitPerUser,
        dailyLimitPerUser = this.dailyLimitPerUser,
        promotionState = this.promotionState,
        isBookable = this.isBookable,
        theme = this.theme,
        businessName = this.businessName,
        isReserved = isReserved,
    )
}
fun List<Promotions>.toEntityList(): List<PromotionEntity> {
    return this.map { it.toEntity() }
}

fun Promotions.toCatEntity(isReserved: Boolean = false) : PromotionWithCategories {
    return PromotionWithCategories(
        promotion = this.toEntity(isReserved),
        categories = this.categories.toCategoryEntityList()
    )
}

fun List<Promotions>.toCatEntityList(): List<PromotionWithCategories> {
    return this.map { it.toCatEntity() }
}

