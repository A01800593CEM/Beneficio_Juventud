// mx/itesm/beneficiojuventud/model/promos/Promotions.kt
package mx.itesm.beneficiojuventud.model.promos

import com.google.gson.annotations.SerializedName

data class Promotions(
    val promotionId: Int? = null,
    val collaboratorId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,

    // Fechas como String para evitar problemas de parseo
    val initialDate: String? = null,
    val endDate: String? = null,

    val promotionType: PromotionType? = null,
    val promotionString: String? = null,
    val totalStock: Int? = null,
    val availableStock: Int? = null,
    val limitPerUser: Int? = null,
    val dailyLimitPerUser: Int? = null,
    val promotionState: PromotionState? = null,

    // Campos extra que sí vienen en el JSON
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("is_bookable") val isBookable: Boolean? = null,

    val theme: PromoTheme? = null,
    val businessName: String? = null,

    // ¡Importante! Lista de objetos, NO de strings
    val categories: List<Category> = emptyList()
)

data class Category(
    val id: Int? = null,
    val name: String? = null
)

enum class PromotionType {
    @SerializedName("descuento") descuento,
    @SerializedName("multicompra") multicompra,
    @SerializedName("regalo") regalo,
    @SerializedName("otro") otro
}

enum class PromotionState {
    @SerializedName("activa") activa,
    @SerializedName("inactiva") inactiva,
    @SerializedName("finalizada") finalizada
}

enum class PromoTheme {
    @SerializedName("dark") dark,
    @SerializedName("light") light,
}
