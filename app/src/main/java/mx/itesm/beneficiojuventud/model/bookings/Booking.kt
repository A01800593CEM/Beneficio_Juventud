package mx.itesm.beneficiojuventud.model.bookings


data class Booking(

    val bookingId: Int? = null,
    val userId: String? = null, // lo ocupamos
    val promotionId: Int? = null, // lo ocupamos
    val bookingDate: String? = null, // lo ocupamos
    val limitUseDate: String? = null, // lo ocupamos
    val status: BookingStatus? = null, // lo ocupamos

)
