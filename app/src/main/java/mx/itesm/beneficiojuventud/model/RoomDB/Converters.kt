package mx.itesm.beneficiojuventud.model.RoomDB

import androidx.room.TypeConverter
import mx.itesm.beneficiojuventud.model.bookings.BookingStatus
import mx.itesm.beneficiojuventud.model.promos.PromoTheme
import mx.itesm.beneficiojuventud.model.promos.PromotionState
import mx.itesm.beneficiojuventud.model.promos.PromotionType

class Converters {
    @TypeConverter
    fun fromPromotionType(value: PromotionType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPromotionType(value: String?): PromotionType? {
        return value?.let { PromotionType.valueOf(it) }
    }

    @TypeConverter
    fun fromPromotionState(value: PromotionState?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPromotionState(value: String?): PromotionState? {
        return value?.let { PromotionState.valueOf(it) }
    }

    @TypeConverter
    fun fromPromoTheme(value: PromoTheme?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPromoTheme(value: String?): PromoTheme? {
        return value?.let { PromoTheme.valueOf(it) }
    }

    @TypeConverter
    fun fromBookingStatus(value: BookingStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toBookingStatus(value: String?): BookingStatus? {
        return value?.let { BookingStatus.valueOf(it) }
    }
}
