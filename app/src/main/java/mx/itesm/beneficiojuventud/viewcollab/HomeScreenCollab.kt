package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.viewcollab.BJBottomBarCollab
import mx.itesm.beneficiojuventud.viewcollab.BJTabCollab
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme

// Colores del diseño
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val TextGrey = Color(0xFF616161)
private val TextLightGrey = Color(0xFF969696)
private val CardBorderGradient = Brush.horizontalGradient(listOf(DarkBlue, Teal))
private val TextGradient = Brush.horizontalGradient(listOf(DarkBlue, Teal))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenCollab(nav: NavHostController) {
    var selectedTab by remember { mutableStateOf<BJTabCollab>(BJTabCollab.Menu) }

    Scaffold(
        bottomBar = {
            BJBottomBarCollab(
                selected = selectedTab,
                onSelect = { newTab -> selectedTab = newTab },
                onAddClick = { /* TODO: Acción del botón '+' */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(50.dp))
            HeaderSectionCollab()
            Spacer(Modifier.height(24.dp))
            CtaSectionCollab()
            Spacer(Modifier.height(24.dp))
            InfoCardCollab(
                icon = Icons.Outlined.LocalOffer,
                title = "Promociones Activas",
                description = "Visualiza, edita o desactiva las ofertas que tienes vigentes.",
                onClick = { }
            )
            Spacer(Modifier.height(16.dp))
            InfoCardCollab(
                icon = Icons.Outlined.BarChart,
                title = "Estadísticas",
                description = "Analiza el rendimiento y la conversión de tus cupones.",
                onClick = { }
            )
            Spacer(Modifier.height(16.dp))
            InfoCardCollab(
                icon = Icons.Default.Person,
                title = "Perfil",
                description = "Modifica la información de tu negocio.",
                onClick = { }
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeaderSectionCollab() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = "Logo Colaborador",
                modifier = Modifier.size(36.dp),
                tint = DarkBlue
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola, María",
                style = TextStyle(brush = TextGradient, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            )
            Text(text = "La Bella Italia", fontSize = 14.sp, color = TextGrey)
        }
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "Settings",
            modifier = Modifier.size(32.dp).clickable { },
            tint = TextGrey
        )
    }
}

@Composable
private fun CtaSectionCollab() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "¿Listo para atraer más clientes?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextGrey)
            Text(text = "Genera una nueva promoción", fontSize = 14.sp, color = TextGrey)
        }
    }
}

@Composable
private fun InfoCardCollab(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, brush = CardBorderGradient, shape = RoundedCornerShape(25.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBorderGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextGrey, modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Teal, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(text = description, fontSize = 14.sp, lineHeight = 17.sp, color = TextLightGrey)
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun HomeScreenCollabPreview() {
    BeneficioJuventudTheme {
        HomeScreenCollab(nav = rememberNavController())
    }
}