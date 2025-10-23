package mx.itesm.beneficiojuventud.model.RoomDB.History

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String?,
    val type: String?,  // HistoryType enum: CUPON_USADO, CUPON_RESERVADO, RESERVA_CANCELADA, FAVORITO_AGREGADO, FAVORITO_QUITADO
    val title: String?,  // Promotion name or event title
    val subtitle: String?,  // Business name or additional info
    val date: String?,  // Formatted date (human readable)
    val iso: String?,  // ISO 8601 timestamp
    val timestamp: Long = System.currentTimeMillis(),  // For sorting
    val promotionId: Int? = null,  // Reference to promotion (for redeemed coupons)
    val branchId: Int? = null  // Reference to branch (for redeemed coupons)
)
