package mx.itesm.beneficiojuventud.model.history

import android.util.Log
import kotlinx.coroutines.flow.Flow
import mx.itesm.beneficiojuventud.model.RoomDB.History.HistoryDao
import mx.itesm.beneficiojuventud.model.RoomDB.History.HistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryService(private val historyDao: HistoryDao) {

    fun getHistoryByUser(userId: String): Flow<List<HistoryEntity>> {
        return historyDao.getHistoryByUser(userId)
    }

    suspend fun addHistoryEvent(
        userId: String,
        type: String,
        title: String,
        subtitle: String,
        iso: String,
        promotionId: Int? = null,
        branchId: Int? = null
    ) {
        try {
            val date = formatDate(iso)
            val history = HistoryEntity(
                userId = userId,
                type = type,
                title = title,
                subtitle = subtitle,
                date = date,
                iso = iso,
                timestamp = System.currentTimeMillis(),
                promotionId = promotionId,
                branchId = branchId
            )
            historyDao.insertHistory(history)
            Log.d("HistoryService", "History event added: $type - $title")
        } catch (e: Exception) {
            Log.e("HistoryService", "Error adding history event", e)
        }
    }

    suspend fun clearHistoryForUser(userId: String) {
        try {
            historyDao.deleteAllByUser(userId)
            Log.d("HistoryService", "Cleared history for user: $userId")
        } catch (e: Exception) {
            Log.e("HistoryService", "Error clearing history", e)
        }
    }

    suspend fun deleteOldHistory(daysOld: Int = 90) {
        try {
            val olderThanTimestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
            historyDao.deleteOldHistory(olderThanTimestamp)
            Log.d("HistoryService", "Deleted history older than $daysOld days")
        } catch (e: Exception) {
            Log.e("HistoryService", "Error deleting old history", e)
        }
    }

    private fun formatDate(isoString: String): String {
        return try {
            // Parse ISO 8601 format: 2024-10-22T15:30:45.123Z or 2024-10-22T15:30:45Z
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val date = isoFormat.parse(isoString)

            // Format as: "22 de octubre de 2024, 3:30 PM"
            val displayFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy, h:mm a", Locale("es", "MX"))
            date?.let { displayFormat.format(it) } ?: isoString
        } catch (e: Exception) {
            Log.w("HistoryService", "Error parsing date: $isoString", e)
            isoString
        }
    }
}
