package mx.itesm.beneficiojuventud.model.bookings

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface BookingApiService {
    @POST("users/bookings")
    suspend fun createBooking(@Body booking: Booking): Response<Booking>

    @GET("users/bookings/user_bookings/{id}")
    suspend fun getUserBookings(@Path("id") userId: String): Response<List<Booking>>

    @GET("users/bookings")
    suspend fun getAllBookings(): Response<List<Booking>>

    @GET("users/bookings/{bookingId}")
    suspend fun getOneBooking(@Path("bookingId") bookingId: Int): Response<Booking>

    @PATCH("users/bookings/{bookingId}")
    suspend fun updateBooking(@Path("bookingId") bookingId: Int): Response<Booking>

    @DELETE("users/bookings/{bookingId}")
    suspend fun deleteBooking(@Path("bookingId") bookingId: Int): Response<Unit>

}