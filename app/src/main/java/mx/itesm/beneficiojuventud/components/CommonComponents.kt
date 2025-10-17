package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.FaceRetouchingNatural
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF616161),
        modifier = modifier
    )
}

@Composable
fun CategoryPill(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 120.dp // ⇦ ajústalo si quieres
) {
    val shape = RoundedCornerShape(999.dp)
    val border = if (selected) null else BorderStroke(1.dp, Color(0xFFE5E5E5))

    Surface(
        modifier = modifier
            .heightIn(min = 36.dp)
            .widthIn(max = maxWidth) // ⇦ limita el tamaño del chip
            .clickable(onClick = onClick),
        color = if (selected) Color(0xFFE0F7F8) else Color.White,
        contentColor = if (selected) Color(0xFF008D96) else Color(0xFF4B4C7E),
        shape = shape,
        border = border,
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis, // ⇦ corta con “…”
                softWrap = false,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun iconForCategoryName(name: String?): ImageVector {
    if (name.isNullOrBlank()) return Icons.Outlined.Category

    val normalized = name
        .lowercase()
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace("ü", "u")
        .replace("ñ", "n")
        .trim()

    val CATEGORY_ICON_MAP: Map<String, ImageVector> = mapOf(
        "comida" to Icons.Outlined.Fastfood,
        "restaurantes" to Icons.Outlined.Fastfood,
        "pizzas" to Icons.Outlined.LocalPizza,
        "bebidas" to Icons.Outlined.LocalDrink,
        "cafeteria" to Icons.Outlined.Coffee,
        "cine" to Icons.Outlined.Movie,
        "peliculas" to Icons.Outlined.Movie,
        "musica" to Icons.Outlined.MusicNote,
        "moda" to Icons.Outlined.Checkroom,
        "ropa" to Icons.Outlined.Checkroom,
        "zapatos" to Icons.Outlined.ShoppingBag,
        "deportes" to Icons.Outlined.SportsSoccer,
        "gym" to Icons.Outlined.FitnessCenter,
        "tecnologia" to Icons.Outlined.Memory,
        "electronica" to Icons.Outlined.Memory,
        "belleza" to Icons.Outlined.Brush,
        "maquillaje" to Icons.Outlined.FaceRetouchingNatural,
        "viajes" to Icons.Outlined.Flight,
        "educacion" to Icons.Outlined.School,
        "libros" to Icons.Outlined.MenuBook,
        "salud" to Icons.Outlined.MedicalServices,
        "servicios" to Icons.Outlined.Build,
        "entretenimiento" to Icons.Outlined.Theaters,
        "hogar" to Icons.Outlined.Home,
        "mascotas" to Icons.Outlined.Pets,
        "otros" to Icons.Outlined.Category
    )

    return CATEGORY_ICON_MAP[normalized] ?: Icons.Outlined.Category
}


@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    MaterialTheme {
        Surface(color = Color(0xFFF4F4F4), modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                SectionTitle("Categorías Populares")
            }
        }
    }
}

