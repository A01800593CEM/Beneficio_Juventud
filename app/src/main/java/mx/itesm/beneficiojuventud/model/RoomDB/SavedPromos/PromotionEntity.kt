package mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import mx.itesm.beneficiojuventud.model.promos.PromoTheme
import mx.itesm.beneficiojuventud.model.promos.PromotionState
import mx.itesm.beneficiojuventud.model.promos.PromotionType

@Entity(tableName = "promotion")
data class PromotionEntity(
    @PrimaryKey val promotionId: Int?,
    val title: String?,
    val description: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.Companion.BLOB) val image: ByteArray?,
    val initialDate: String?,
    val endDate: String?,
    val promotionType: PromotionType?,
    val promotionString: String?,
    val totalStock: Int?,
    val availableStock: Int?,
    val limitPerUser: Int?,
    val dailyLimitPerUser: Int?,
    val promotionState: PromotionState?,
    val isBookable: Boolean?,
    val theme: PromoTheme?,
    val businessName: String?,
    val isReserved: Boolean?
)