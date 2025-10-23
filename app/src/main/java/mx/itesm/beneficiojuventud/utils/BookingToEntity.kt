package mx.itesm.beneficiojuventud.utils

import mx.itesm.beneficiojuventud.model.RoomDB.Bookings.BookingEntity
import mx.itesm.beneficiojuventud.model.bookings.Booking

fun Booking.toEntity(): BookingEntity {
    return BookingEntity(
        bookingId = this.bookingId,
        userId = this.userId,
        promotionId = this.promotionId,
        bookingDate = this.bookingDate,
        limitUseDate = this.limitUseDate,
        status = this.status,
        cancelledDate = this.cancelledDate,
        autoExpireDate = this.autoExpireDate,
        cooldownUntil = this.cooldownUntil
    )
}

fun List<Booking>.toBookingEntityList(): List<BookingEntity> {
    return this.map { it.toEntity() }
}
