package mx.itesm.beneficiojuventud.model.RoomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryDao
import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryEntity
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionCategories
import mx.itesm.beneficiojuventud.model.RoomDB.PromotionsCategories.PromotionWithCategories
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionDao
import mx.itesm.beneficiojuventud.model.RoomDB.SavedPromos.PromotionEntity


@Database(entities = [
    PromotionEntity::class,
    CategoryEntity::class,
    PromotionCategories::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun promotionDao(): PromotionDao
    abstract fun categoryDao(): CategoryDao
}