package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            color = if (selected) Color(0xFF008D96).copy(alpha = 0.15f) else Color(0xFFEFF7F7),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .size(48.dp)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) Color(0xFF008D96) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (selected) Color(0xFF008D96) else Color(0xFF008D96),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color(0xFF008D96) else Color(0xFF616161)
        )
    }
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

@Preview(showBackground = true)
@Composable
private fun CategoryPillPreview() {
    MaterialTheme {
        Surface(color = Color(0xFFF4F4F4), modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryPill(icon = Icons.Outlined.Fastfood, label = "Alimentos")
                CategoryPill(icon = Icons.Outlined.MusicNote, label = "Música")
                CategoryPill(icon = Icons.Outlined.Movie, label = "Cine")
            }
        }
    }
}

