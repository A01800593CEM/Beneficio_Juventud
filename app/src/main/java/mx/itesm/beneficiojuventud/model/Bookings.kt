package mx.itesm.beneficiojuventud.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Bookings(
    val promotions : Int,
    val userId : Int,
    val limitUseDate : Date?,
    val bookStatus : BookStatus,
    val bookedPromotion : Int?
)

enum class BookStatus {
    @SerializedName("pendiente") pendiente,
    @SerializedName("usada") usada,
    @SerializedName("cancelada")  cancelada
}

