package mx.itesm.beneficiojuventud.model.bookings

import com.google.gson.GsonBuilder
import mx.itesm.beneficiojuventud.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue

object RemoteServiceBooking {

    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL) // asegúrate de que tenga '/' al final
            .addConverterFactory(GsonConverterFactory.create(RemoteServiceBooking.gson))
            .build()
    }

    private val bookingApiService by lazy { retrofit.create(BookingApiService::class.java) }

    suspend fun getUserBookings(userId: String): List<Booking> {
        val response = bookingApiService.getUserBookings(userId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: emptyList()
    }

    suspend fun createBooking(booking: Booking): Booking {
        val response = bookingApiService.createBooking(booking)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al al crear reservacion")
    }

    suspend fun updateBooking(bookingId: Int): Booking {
        val response = bookingApiService.updateBooking(bookingId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
        return response.body() ?: throw Exception("Respuesta vacía al editar reservacion")
    }

    suspend fun deleteBooking(bookingId: Int) {
        val response = bookingApiService.deleteBooking(bookingId)
        if (!response.isSuccessful) {
            throw Exception("Error ${response.code()}: ${response.errorBody()?.string().orEmpty()}")
        }
    }
}