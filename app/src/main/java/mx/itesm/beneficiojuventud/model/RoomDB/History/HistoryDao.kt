package mx.itesm.beneficiojuventud.model.RoomDB.History

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHistoryByUser(userId: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getHistoryByUserLimit(userId: String, limit: Int): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    suspend fun getHistoryByUserAndType(userId: String, type: String): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleHistory(vararg history: HistoryEntity)

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM history WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("DELETE FROM history")
    suspend fun deleteAll()

    @Query("DELETE FROM history WHERE timestamp < :olderThanTimestamp")
    suspend fun deleteOldHistory(olderThanTimestamp: Long)
}
