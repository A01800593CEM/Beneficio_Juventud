package mx.itesm.beneficiojuventud.utils

import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryEntity
import mx.itesm.beneficiojuventud.model.categories.Category

fun Category.toCategoryEntity() : CategoryEntity {
    return CategoryEntity(
        categoryId = this.id,
        name = this.name
    )
}

fun List<Category>.toCategoryEntityList(): List<CategoryEntity> {
    return this.map { it.toCategoryEntity() }
}