package mx.itesm.beneficiojuventud.model.RoomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import mx.itesm.beneficiojuventud.model.promos.PromotionDao
import mx.itesm.beneficiojuventud.model.promos.PromotionEntity

@Database(entities = [PromotionEntity::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun promotionDao(): PromotionDao
}