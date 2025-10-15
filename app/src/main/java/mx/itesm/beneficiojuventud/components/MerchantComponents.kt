import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator

@Composable
fun MerchantRow(
    collaborators: List<Collaborator>,
    onItemClick: (Collaborator) -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.62f

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = collaborators,
            key = { collabKey(it) } // clave estable como en promos
        ) { collab ->
            MerchantCard(
                collab = collab,
                modifier = Modifier
                    .width(cardWidth)
                    .height(150.dp),
                onClick = { onItemClick(collab) } // click como en PromoImageBanner
            )
        }
    }
}

private fun collabKey(c: Collaborator): Any =
    c.collaboratorId ?: c.businessName ?: c.email ?: c.hashCode()

@Composable
fun MerchantCard(
    collab: Collaborator,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val name = collab.businessName ?: "Colaborador"
    val imageUrl = collab.logoUrl
    val ratingText = "—"
    val fallbackRes = R.drawable.brasa

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE6E6E6))
        ),
        modifier = modifier.clickable(enabled = true) { onClick() } // ⟵ igual que en promo
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl.takeIf { !it.isNullOrBlank() } ?: fallbackRes)
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
                        text = ratingText,
                        fontSize = 11.sp,
                        color = Color(0xFF7A7A7A)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = collab.categoryIds?.toString().orEmpty(),
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
