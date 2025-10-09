package mx.itesm.beneficiojuventud.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*

data class Category(val icon: ImageVector, val label: String)

val popularCategories = listOf(
    Category(Icons.Outlined.Fastfood, "Alimentos"),
    Category(Icons.Outlined.MusicNote, "Música"),
    Category(Icons.Outlined.SportsEsports, "Deportes"),
    Category(Icons.Outlined.Movie, "Cine y Ocio"),
    Category(Icons.Outlined.Storefront, "Moda"),
    Category(Icons.Outlined.Edit, "Papelería")
)
