package mx.itesm.beneficiojuventud.model.RoomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryDao
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryEntity
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategories
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategoriesDao
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionDao
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity


@Database(entities = [
    PromotionEntity::class,
    CategoryEntity::class,
    PromotionCategories::class], version = 3)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun promotionDao(): PromotionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun promotionCategoriesDao(): PromotionCategoriesDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getDatabase(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "your_database_name"
                )
                    .fallbackToDestructiveMigration() // Drop and recreate DB on version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}