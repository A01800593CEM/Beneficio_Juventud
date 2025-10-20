package mx.itesm.beneficiojuventud.utils

import mx.itesm.beneficiojuventud.model.RoomDB.Categories.CategoryEntity
import mx.itesm.beneficiojuventud.model.categories.Category

fun CategoryEntity.toCategory(): Category {
    return Category(
        id = this.categoryId,
        name = this.name
    )
}

fun List<CategoryEntity>.toCategoryList(): List<Category> {
    return this.map { it.toCategory() }
}