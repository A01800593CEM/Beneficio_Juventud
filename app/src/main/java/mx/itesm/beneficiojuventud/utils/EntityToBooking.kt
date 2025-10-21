package mx.itesm.beneficiojuventud.utils

import mx.itesm.beneficiojuventud.model.RoomDB.Bookings.BookingEntity
import mx.itesm.beneficiojuventud.model.bookings.Booking

fun BookingEntity.toBooking(): Booking {
    return Booking(
        bookingId = this.bookingId,
        userId = this.userId,
        promotionId = this.promotionId,
        bookingDate = this.bookingDate,
        limitUseDate = this.limitUseDate,
        status = this.status,
        cancelledDate = this.cancelledDate
    )
}

fun List<BookingEntity>.toBookingList(): List<Booking> {
    return this.map { it.toBooking() }
}
