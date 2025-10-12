package mx.itesm.beneficiojuventud.model.promos

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Promotions(
    val collaboratorId: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val initialDate: Date? = null,
    val endDate: Date? = null,
    val categoryId: Int? = null,
    val promotionType: PromotionType? = null,
    val promotionString: String? = null,
    val totalStock: Int? = null,
    val availableStock: Int? = null,
    val limitPerUser: Int? = null,
    val dailyLimitPerUser: Int? = null,
    val promotionState: PromotionState? = null
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
    @SerializedName("finaliazada") finalizada
}
