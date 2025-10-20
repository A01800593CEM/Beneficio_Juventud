package mx.itesm.beneficiojuventud.model.RoomDB.Bookings

import androidx.room.Entity
import androidx.room.PrimaryKey
import mx.itesm.beneficiojuventud.model.bookings.BookingStatus

@Entity(tableName = "booking")
data class BookingEntity(
    @PrimaryKey val bookingId: Int?,
    val userId: String?,
    val promotionId: Int?,
    val bookingDate: String?,
    val limitUseDate: String?,
    val status: BookingStatus?,
    val cancelledDate: String? = null
)
