package mx.itesm.beneficiojuventud.model.promos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "promotion")
data class PromotionEntity(
    @PrimaryKey val promotionId: Int,
    val title: String?,
    val description: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val image: ByteArray?,
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


)
