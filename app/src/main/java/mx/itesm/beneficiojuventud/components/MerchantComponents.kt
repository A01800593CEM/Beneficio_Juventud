package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

/**
 * Carrusel de merchants (colaboradores) desde backend.
 * Mantiene el diseño de tus MerchantCards originales.
 */
@Composable
fun MerchantRow(
    collaborators: List<Collaborator>,
    onItemClick: (Collaborator) -> Unit = {}
) {
    // ancho aprox 62% del ancho de pantalla (igual que tu componente original)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.62f

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(collaborators.size) { i ->
            MerchantCard(
                collab = collaborators[i],
                index = i,
                modifier = Modifier
                    .width(cardWidth)
                    .height(150.dp)
                    .clickable { onItemClick(collaborators[i]) }
            )
        }
    }
}

/**
 * Card individual. Conserva border, esquinas, altura y tipografías.
 * Carga imagen real (url) con Coil; si no hay, alterna tus drawables locales.
 */
@Composable
fun MerchantCard(
    collab: Collaborator,
    index: Int,
    modifier: Modifier = Modifier
) {
    // ====== Mapeo flexible a campos comunes del backend ======
    // Ajusta estos accesos si tu Collaborator usa otros nombres.
    val name: String = runCatching { // p. ej. collab.name ?: collab.title ?: ...
        val n = collab::class.members.firstOrNull { it.name == "name" }?.call(collab) as? String
        n ?: "Colaborador"
    }.getOrDefault("Colaborador")

    val category: String = runCatching {
        // intenta "category" directo; si no existe, deja vacío
        (collab::class.members.firstOrNull { it.name == "category" }?.call(collab) as? String)
            ?: ""
    }.getOrDefault("")

    val imageUrl: String? = runCatching {
        // intenta primero "imageUrl", si no, "logoUrl" o "bannerUrl" si existieran
        (collab::class.members.firstOrNull { it.name == "imageUrl" }?.call(collab) as? String)
            ?: (collab::class.members.firstOrNull { it.name == "logoUrl" }?.call(collab) as? String)
            ?: (collab::class.members.firstOrNull { it.name == "bannerUrl" }?.call(collab) as? String)
    }.getOrNull()

    val rating: Double = runCatching {
        (collab::class.members.firstOrNull { it.name == "rating" }?.call(collab) as? Number)?.toDouble()
    }.getOrNull() ?: 0.0
    // =========================================================

    // Fallback local alternando entre tus imágenes como antes
    val fallbackRes = if (index % 2 == 0) R.drawable.brasa else R.drawable.ensalada

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE6E6E6))
        ),
        modifier = modifier
    ) {
        Column {
            // Imagen superior (ahora AsyncImage con URL real)
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .placeholder(fallbackRes)
                        .error(fallbackRes)
                        .build(),
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Misma UI si no hay imagen en backend
                androidx.compose.foundation.Image(
                    painter = painterResource(id = fallbackRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFF3A3A3A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = if (rating > 0) String.format("%.1f", rating) else "—",
                        fontSize = 11.sp,
                        color = Color(0xFF7A7A7A)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = category.ifBlank { " " }, // preserva layout aunque venga vacío
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
