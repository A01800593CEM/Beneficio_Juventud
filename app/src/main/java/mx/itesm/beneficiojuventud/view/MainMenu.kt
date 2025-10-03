package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

@Composable
fun MainMenu(nav: NavHostController) {
    val gradient = remember {
        Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96)))
    }

    // ------- Datos de ejemplo (sustituir por ViewModel / backend) -------
    val name = "Iván"
    val featured: List<CouponUi> = listOf(
        CouponUi(
            collaborator = "Burger King",
            promotionString = "SÓLO $25c/u",
            description = "Promoción por tiempo limitado. Solo en hamburguesas participantes.",
            imageUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?q=80&w=1200"
        ),
        CouponUi(
            collaborator = "Starbucks",
            promotionString = "2x1 en Frappé",
            description = "Aplican restricciones. Válido en sucursales participantes.",
            imageUrl = "https://images.unsplash.com/photo-1517705008128-361805f42e86?q=80&w=1200"
        ),
        CouponUi(
            collaborator = "Subway",
            promotionString = "Combo a $49",
            description = "Incluye agua y galleta. Válido de Lun a Vie.",
            imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1200"
        )
    )
    val categories = listOf("Moda y Estilo", "Alimentos", "Deporte", "Entretenimiento")
    val recent = listOf(
        Recent("Cupón Usado", "Starbucks Jinetes", "Hace 3 horas"),
        Recent("Cupón Guardado", "Oxxo Margaritas", "Hace 3 días"),
        Recent("Cupón Usado", "Subway Jinetes", "Hace 3 días")
    )
    // --------------------------------------------------------------------

    var search by remember { mutableStateOf(TextFieldValue("")) }
    val pagerState = rememberPagerState(pageCount = { featured.size })

    Scaffold { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Header
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini logo
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(gradient)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Hola, $name",
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            brush = gradient
                        )
                    )
                    Text("¿Listo para ahorrar?", color = Color(0xFF6F6F6F), fontSize = 13.sp)
                }
                IconButton(onClick = { /* TODO notifs */ }) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsNone,
                        contentDescription = "Notificaciones",
                        tint = Color(0xFF2F2F2F)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Search
            TextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("¿Qué estás buscando?") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF5F6F7),
                    focusedContainerColor = Color(0xFFF5F6F7),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF008D96)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            // Cupones Destacados
            SectionTitle(
                title = "Cupones Destacados",
                subtitle = "¡Las ofertas más calientes del momento!\nDesliza a la derecha para guardar o a la izquierda para descartar"
            )

            Spacer(Modifier.height(10.dp))

            // “Barra” superior degradada
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(gradient)
            )

            Spacer(Modifier.height(8.dp))

            // Carrusel
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                CouponCard(
                    collaborator = featured[page].collaborator,
                    promotionString = featured[page].promotionString,
                    description = featured[page].description,
                    imageUrl = featured[page].imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            DotsIndicator(
                totalDots = featured.size,
                selectedIndex = pagerState.currentPage
            )

            Spacer(Modifier.height(18.dp))

            // Categorías Populares
            Text(
                "Categorías Populares",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )
            Spacer(Modifier.height(12.dp))
            CategoriesGrid(
                categories = categories,
                onClick = { /* TODO navegar a listado por categoría */ }
            )

            Spacer(Modifier.height(18.dp))

            // Actividad Reciente
            Text(
                "Actividad Reciente",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            recent.forEach { r ->
                RecentRow(r)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ===================== Helpers & UI components ===================== */

private data class Recent(val title: String, val subtitle: String, val time: String)
private data class CouponUi(
    val collaborator: String,
    val promotionString: String,
    val description: String,
    val imageUrl: String
)

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            style = TextStyle(
                brush = Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96))),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )

        )
        Text(
            subtitle,
            color = Color(0xFF666666),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun CategoriesGrid(categories: List<String>, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryChip(
                text = categories.getOrNull(0) ?: "",
                modifier = Modifier.weight(1f)
            ) { onClick(categories.getOrNull(0) ?: "") }
            CategoryChip(
                text = categories.getOrNull(1) ?: "",
                modifier = Modifier.weight(1f)
            ) { onClick(categories.getOrNull(1) ?: "") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategoryChip(
                text = categories.getOrNull(2) ?: "",
                modifier = Modifier.weight(1f)
            ) { onClick(categories.getOrNull(2) ?: "") }
            CategoryChip(
                text = categories.getOrNull(3) ?: "",
                modifier = Modifier.weight(1f)
            ) { onClick(categories.getOrNull(3) ?: "") }
        }
    }
}

@Composable
private fun CategoryChip(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF4F6F8),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .height(44.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color(0xFF4C4C4C), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RecentRow(r: Recent) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(r.title, fontWeight = FontWeight.Bold, color = Color(0xFF2F2F2F))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(r.subtitle, color = Color(0xFF6F6F6F))
            Text(r.time, color = Color(0xFF6F6F6F))
        }
    }
}

@Composable
private fun DotsIndicator(totalDots: Int, selectedIndex: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalDots) { index ->
            val size = if (index == selectedIndex) 8.dp else 6.dp
            val color = if (index == selectedIndex) Color(0xFF3D6FB5) else Color(0xFFBFD1EA)
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/* ===================== Coupon Card (texto izq + imagen der) ===================== */

@Composable
private fun CouponCard(
    collaborator: String,
    promotionString: String,
    description: String,
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val radius = 16.dp

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(radius),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.fillMaxSize()) {

            // IZQUIERDA: textos
            Column(
                modifier = Modifier
                    .weight(1.15f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = collaborator,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5B5B5B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = promotionString,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2F2F2F),
                    lineHeight = 30.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF7A7A7A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // DERECHA: imagen (Coil)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = radius, bottomEnd = radius))
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen del cupón",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val s = painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF0F0F0))
                            )
                        }
                        is AsyncImagePainter.State.Error -> {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE7EEF5))
                            )
                        }
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }
        }
    }
}

/* ===================== Preview ===================== */

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainMenuPreview() {
    MainMenu(nav = rememberNavController())
}
