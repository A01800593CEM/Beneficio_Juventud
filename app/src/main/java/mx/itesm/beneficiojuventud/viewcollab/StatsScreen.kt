package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.viewcollab.StatsSummaryCard
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewcollab.StatsViewModel

private val TextGrey = Color(0xFF616161)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    nav: NavHostController,
    viewModel: StatsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf<BJTabCollab>(BJTabCollab.Stats) }

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
            modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo de la App", modifier = Modifier.size(width = 45.dp, height = 33.dp))
            Spacer(Modifier.height(24.dp))
            StatsScreenHeader(nav = nav)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    uiState.summary?.let { summary ->
                        StatsSummaryCard(summary = summary)
                    }
                }
            }
        }
    }
}
@Composable
private fun StatsScreenHeader(nav: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextGrey)
            }
            Spacer(Modifier.width(16.dp))
            Text(text = "Estadísticas", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextGrey)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }) {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Ajustes", tint = TextGrey)
            }
        }
        Spacer(Modifier.height(16.dp))
        GradientDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun StatsScreenPreview() {
    BeneficioJuventudTheme {
        StatsScreen(nav = rememberNavController())
    }
}