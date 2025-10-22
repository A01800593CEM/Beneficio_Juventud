package mx.itesm.beneficiojuventud.model.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mx.itesm.beneficiojuventud.model.RoomDB.Bookings.BookingDao
import mx.itesm.beneficiojuventud.model.RoomDB.Bookings.BookingEntity
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryDao
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryEntity
import mx.itesm.beneficiojuventud.model.RoomDB.History.HistoryDao
import mx.itesm.beneficiojuventud.model.RoomDB.History.HistoryEntity
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategories
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategoriesDao
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionDao
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity


@Database(
    entities = [
        PromotionEntity::class,
        CategoryEntity::class,
        PromotionCategories::class,
        BookingEntity::class,
        HistoryEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun promotionDao(): PromotionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bookingDao(): BookingDao
    abstract fun promotionCategoriesDao(): PromotionCategoriesDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getDatabase(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "beneficio_juventud_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}