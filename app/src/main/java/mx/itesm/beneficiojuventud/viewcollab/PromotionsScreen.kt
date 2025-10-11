package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.viewcollab.PromotionCard
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewcollab.PromotionsViewModel

private val TextGrey = Color(0xFF616161)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen(
    nav: NavHostController,
    viewModel: PromotionsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf<BJTabCollab>(BJTabCollab.Promotions) }

    Scaffold(
        bottomBar = {
            BJBottomBarCollab(
                selected = selectedTab,
                onSelect = { newTab -> selectedTab = newTab },
                onAddClick = { /* TODO */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo de la App", modifier = Modifier.size(width = 45.dp, height = 33.dp))
            Spacer(Modifier.height(24.dp))
            PromotionsScreenHeader(nav = nav)

            Column(modifier = Modifier
                .fillMaxSize()
                .background(Color.White)) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.promotions) { promotion ->
                            PromotionCard(
                                promotion = promotion,
                                onClick = { /* TODO */ }
                            )
                        }
                    }
                    CreatePromotionButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onClick = { /* TODO: Navegar a crear promocion */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatePromotionButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val gradient = Brush.horizontalGradient(listOf(DarkBlue, Teal))
    Box(
        modifier = modifier
            .background(gradient, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Crear una Nueva Promoción", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun PromotionsScreenHeader(nav: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextGrey) }
            Spacer(Modifier.width(16.dp))
            Text(text = "Promociones", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextGrey)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }) { Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Ajustes", tint = TextGrey) }
        }
        Spacer(Modifier.height(16.dp))
        GradientDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun PromotionsScreenPreview() {
    BeneficioJuventudTheme {
        PromotionsScreen(nav = rememberNavController())
    }
}