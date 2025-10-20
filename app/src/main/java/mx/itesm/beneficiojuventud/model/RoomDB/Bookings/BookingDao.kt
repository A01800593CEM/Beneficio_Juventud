package mx.itesm.beneficiojuventud.model.RoomDB.Bookings

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookingDao {
    @Query("SELECT * FROM booking WHERE userId = :userId")
    suspend fun getBookingsByUser(userId: String): List<BookingEntity>

    @Query("SELECT * FROM booking WHERE bookingId = :bookingId")
    suspend fun getBookingById(bookingId: Int): BookingEntity?

    @Query("SELECT * FROM booking WHERE promotionId = :promotionId AND userId = :userId")
    suspend fun getBookingByPromotionAndUser(promotionId: Int, userId: String): BookingEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM booking WHERE bookingId = :bookingId)")
    suspend fun exists(bookingId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(vararg bookings: BookingEntity)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Delete
    suspend fun deleteBooking(booking: BookingEntity)

    @Query("DELETE FROM booking WHERE bookingId = :bookingId")
    suspend fun deleteById(bookingId: Int)

    @Query("DELETE FROM booking WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("DELETE FROM booking")
    suspend fun deleteAll()
}
