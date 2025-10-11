package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun PromotionCard(
    promotion: Promotion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.height(120.dp)) {
            Image(painter = rememberAsyncImagePainter(promotion.imageUrl), contentDescription = promotion.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        endX = 900f
                    )
                )
            )
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                Text(text = promotion.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(text = promotion.subtitle, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(text = promotion.description, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, lineHeight = 14.sp, maxLines = 2)
            }
            IconButton(
                onClick = { /* TODO: Editar promocion */ },
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
            }

            StatusChip(
                status = promotion.status,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 8.dp)
            )

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorito",
                tint = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).offset(y = 10.dp, x = 10.dp).size(20.dp)
            )
        }
    }
}

@Composable
private fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val backgroundColor = if (status.equals("Activa", ignoreCase = true)) Color(0xFF2E7D32) else Color(0xFF616161)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}