package mx.itesm.beneficiojuventud.model.promos

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Promotions(
    val collaboratorId: Int,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val initialDate: Date,
    val endDate: Date,
    val categoryId: Int? = null,
    val promotionType: PromotionType,
    val promotionString: String? = null,
    val totalStock: Int? = null,
    val availableStock: Int? = null,
    val limitPerUser: Int? = null,
    val dairyLimitPerUser: Int? = null,
    val promotionState: PromotionState
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
