package mx.itesm.beneficiojuventud.model.promos

import com.google.gson.annotations.SerializedName
import mx.itesm.beneficiojuventud.model.Branch
import mx.itesm.beneficiojuventud.model.categories.Category

/**
 * Modelo de promoción con información de distancia y sucursal más cercana
 */
data class NearbyPromotion(
    val promotionId: Int? = null,
    val collaboratorId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val initialDate: String? = null,
    val endDate: String? = null,
    val promotionType: PromotionType? = null,
    val promotionString: String? = null,
    val totalStock: Int? = null,
    val availableStock: Int? = null,
    val limitPerUser: Int? = null,
    val dailyLimitPerUser: Int? = null,
    val promotionState: PromotionState? = null,

    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("is_bookable") val isBookable: Boolean? = null,

    val theme: PromoTheme? = null,
    val businessName: String? = null,
    val logoUrl: String? = null,
    val categories: List<Category> = emptyList(),

    // Campos específicos de ubicación
    val distance: Double? = null, // Distancia en kilómetros
    val closestBranch: Branch? = null // Sucursal más cercana
) {
    /**
     * Formatea la distancia para mostrar en la UI
     * Ej: "0.5 km", "1.2 km", "500 m"
     */
    fun getFormattedDistance(): String {
        return when {
            distance == null -> ""
            distance < 1.0 -> "${(distance * 1000).toInt()} m"
            else -> String.format("%.1f km", distance)
        }
    }

    /**
     * Convierte a Promotions regular (sin información de ubicación)
     */
    fun toPromotion(): Promotions {
        return Promotions(
            promotionId = promotionId,
            collaboratorId = collaboratorId,
            title = title,
            description = description,
            imageUrl = imageUrl,
            initialDate = initialDate,
            endDate = endDate,
            promotionType = promotionType,
            promotionString = promotionString,
            totalStock = totalStock,
            availableStock = availableStock,
            limitPerUser = limitPerUser,
            dailyLimitPerUser = dailyLimitPerUser,
            promotionState = promotionState,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isBookable = isBookable,
            theme = theme,
            businessName = businessName,
            categories = categories
        )
    }
}
