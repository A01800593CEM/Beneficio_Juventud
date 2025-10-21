package mx.itesm.beneficiojuventud.model.bookings


data class Booking(

    val bookingId: Int? = null,
    val userId: String? = null,
    val promotionId: Int? = null,
    val bookingDate: String? = null,
    val limitUseDate: String? = null,
    val status: BookingStatus? = null,
    val cancelledDate: String? = null

)

data class UpdateBookingRequest(
    val status: BookingStatus? = null,
    val limitUseDate: String? = null
)
