package mx.itesm.beneficiojuventud.model.bookings

import com.google.gson.annotations.SerializedName

enum class BookingStatus {
    @SerializedName("pendiente") PENDING,
    @SerializedName("usada") USED,
    @SerializedName("cancelada") CANCELLED
}