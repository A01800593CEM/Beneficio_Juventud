package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator

// ============================================================
//  Selección de diseño
// ============================================================

enum class MerchantDesign { Horizontal, Poster }

// ============================================================
//  Helpers
// ============================================================

private fun collabKey(c: Collaborator): Any =
    c.cognitoId ?: c.businessName ?: c.email ?: c.hashCode()

/** Intenta resolver categorías si vienen como lista de String o de objetos con name. */
private fun readableCategories(collab: Collaborator): String {
    val s1 = (collab.categories as? List<String>)?.joinToString(", ")
    if (!s1.isNullOrBlank()) return s1

    val listAny = collab.categories as? List<Any?>
    val s2 = listAny?.mapNotNull {
        runCatching {
            val fld = it?.javaClass?.getDeclaredField("name")
            fld?.isAccessible = true
            fld?.get(it) as? String
        }.getOrNull()
    }?.filter { !it.isNullOrBlank() }?.joinToString(", ")

    return s2 ?: ""
}

// ============================================================
//  Card HORIZONTAL (imagen izquierda + fav)
// ============================================================

@Composable
fun MerchantCardHorizontalFav(
    collab: Collaborator,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val name = collab.businessName ?: "Colaborador"
    val imageUrl = collab.logoUrl
    val categories = readableCategories(collab)
    val locationText = collab.address ?: ""
    val ratingText = "4.7"
    val fallbackRes = R.drawable.el_fuego_sagrado

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp, brush = SolidColor(Color(0xFFE6E6E6))
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen con loading
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl.takeIf { !it.isNullOrBlank() } ?: fallbackRes)
                        .crossfade(true)
                        .error(fallbackRes)
                        .build(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp, color = Color(0xFF008D96))
                        }
                    },
                    success = { SubcomposeAsyncImageContent() },
                    error = { SubcomposeAsyncImageContent() }
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 44.dp)
                ) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color(0xFF3A3A3A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (categories.isNotBlank()) {
                        Text(
                            text = categories,
                            fontSize = 13.sp,
                            color = Color(0xFF7E7E7E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (locationText.isNotBlank()) {
                        Text(
                            text = locationText,
                            fontSize = 13.sp,
                            color = Color(0xFF9E9E9E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = ratingText,
                            fontSize = 13.sp,
                            color = Color(0xFF505050)
                        )
                    }
                }
            }

            // Botón favorito
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.96f),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp, brush = SolidColor(Color(0xFFE5E5E5))
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFE53935) else Color(0xFF505050)
                    )
                }
            }
        }
    }
}

// ============================================================
//  Card POSTER (imagen arriba + fav)
// ============================================================

@Composable
fun MerchantCardPosterFav(
    collab: Collaborator,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val name = collab.businessName ?: "Colaborador"
    val imageUrl = collab.logoUrl
    val categories = readableCategories(collab)
    val ratingText = "4.7"
    val fallbackRes = R.drawable.no_vendor_img

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp, brush = SolidColor(Color(0xFFE6E6E6))
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Box {
            Column {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl.takeIf { !it.isNullOrBlank() } ?: fallbackRes)
                        .crossfade(true)
                        .error(fallbackRes)
                        .build(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp, color = Color(0xFF008D96))
                        }
                    },
                    success = { SubcomposeAsyncImageContent() },
                    error = { SubcomposeAsyncImageContent() }
                )

                Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF3A3A3A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(text = ratingText, fontSize = 10.5.sp, color = Color(0xFF7A7A7A))
                    }

                    if (categories.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = categories,
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.75f),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp, brush = SolidColor(Color(0xFFE5E5E5))
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFE53935) else Color(0xFF505050),
                        modifier = modifier.size(20.dp).padding(4.dp)
                    )
                }
            }
        }
    }
}

// ============================================================
//  Wrapper + Row
// ============================================================

@Composable
fun MerchantCardSelectable(
    design: MerchantDesign,
    collab: Collaborator,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    when (design) {
        MerchantDesign.Horizontal ->
            MerchantCardHorizontalFav(collab, isFavorite, onFavoriteClick, modifier, onClick)
        MerchantDesign.Poster ->
            MerchantCardPosterFav(collab, isFavorite, onFavoriteClick, modifier, onClick)
    }
}

@Composable
fun MerchantRowSelectable(
    collaborators: List<Collaborator>,
    design: MerchantDesign,
    isFavorite: (Collaborator) -> Boolean = { false },
    onFavoriteClick: (Collaborator) -> Unit = {},
    onItemClick: (Collaborator) -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = when (design) {
        MerchantDesign.Horizontal -> screenWidth * 0.92f
        MerchantDesign.Poster     -> screenWidth * 0.62f
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = collaborators, key = { collabKey(it) }) { collab ->
            MerchantCardSelectable(
                design = design,
                collab = collab,
                isFavorite = isFavorite(collab),
                onFavoriteClick = { onFavoriteClick(collab) },
                modifier = Modifier
                    .width(cardWidth)
                    .height(if (design == MerchantDesign.Horizontal) 120.dp else 150.dp),
                onClick = { onItemClick(collab) }
            )
        }
    }
}
