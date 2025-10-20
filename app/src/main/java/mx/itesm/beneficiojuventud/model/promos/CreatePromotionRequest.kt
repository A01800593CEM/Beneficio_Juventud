package mx.itesm.beneficiojuventud.model.promos

import com.google.gson.annotations.SerializedName

/**
 * DTO for creating promotions that matches the backend API expectations.
 * Uses categoryIds (array of IDs) and optional branchIds.
 */
data class CreatePromotionRequest(
    val collaboratorId: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val initialDate: String,
    val endDate: String,
    val promotionType: PromotionType,
    val promotionString: String? = null,
    val totalStock: Int,
    val availableStock: Int,
    val limitPerUser: Int,
    val dailyLimitPerUser: Int,
    val promotionState: PromotionState,
    @SerializedName("promotionTheme") val theme: PromoTheme? = null,
    @SerializedName("is_bookable") val isBookable: Boolean,
    // Category IDs as array of integers
    val categoryIds: List<Int>,
    // Branch IDs as optional array of integers
    // If null or empty, promotion applies to ALL collaborator's branches
    val branchIds: List<Int>? = null
)

/**
 * Extension function to convert Promotions to CreatePromotionRequest
 */
fun Promotions.toCreateRequest(branchIds: List<Int>? = null): CreatePromotionRequest {
    return CreatePromotionRequest(
        collaboratorId = this.collaboratorId ?: throw IllegalArgumentException("collaboratorId is required"),
        title = this.title ?: throw IllegalArgumentException("title is required"),
        description = this.description ?: throw IllegalArgumentException("description is required"),
        imageUrl = this.imageUrl,
        initialDate = this.initialDate ?: throw IllegalArgumentException("initialDate is required"),
        endDate = this.endDate ?: throw IllegalArgumentException("endDate is required"),
        promotionType = this.promotionType ?: throw IllegalArgumentException("promotionType is required"),
        promotionString = this.promotionString,
        totalStock = this.totalStock ?: throw IllegalArgumentException("totalStock is required"),
        availableStock = this.availableStock ?: throw IllegalArgumentException("availableStock is required"),
        limitPerUser = this.limitPerUser ?: throw IllegalArgumentException("limitPerUser is required"),
        dailyLimitPerUser = this.dailyLimitPerUser ?: throw IllegalArgumentException("dailyLimitPerUser is required"),
        promotionState = this.promotionState ?: throw IllegalArgumentException("promotionState is required"),
        theme = this.theme,
        isBookable = this.isBookable ?: false,
        // Extract category IDs from Category objects
        categoryIds = this.categories.mapNotNull { it.id },
        // Pass branch IDs if provided
        branchIds = branchIds
    )
}
