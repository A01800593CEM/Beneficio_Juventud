package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel

private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val TextGrey = Color(0xFF616161)
private val TextLightGrey = Color(0xFF969696)
private val CardBorderGradient = Brush.horizontalGradient(listOf(DarkBlue, Teal))
private val TextGradient = Brush.horizontalGradient(listOf(DarkBlue, Teal))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenCollab(
    nav: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf<BJTabCollab>(BJTabCollab.Menu) }
    val collabState by collabViewModel.collabState.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()

    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            try {
                collabViewModel.getCollaboratorById(currentUserId!!)
            } catch (e: Exception) {
                println("Error al cargar colaborador: ${e.message}")
            }
        }
    }

    Scaffold(
        bottomBar = {
            BJBottomBarCollab(
                selected = selectedTab,
                onSelect = { newTab -> selectedTab = newTab },
                onAddClick = { nav.navigate("qr_scanner") } // 👈 ahora sí navega
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            when (selectedTab) {
                BJTabCollab.Menu -> CollabMenuContent(
                    nav = nav,
                    collaborator = collabState,
                    onNavigateTo = { tab -> selectedTab = tab }
                )
                BJTabCollab.Stats -> CollabStatsScreen()
                BJTabCollab.Promotions -> CollabPromotionsScreen()
                BJTabCollab.Profile -> CollabProfileScreen()
            }
        }
    }
}

/**
 * Contenido de la pestaña "Menú" (la pantalla principal).
 */
@Composable
private fun CollabMenuContent(
    nav: NavHostController,
    collaborator: Collaborator,
    onNavigateTo: (BJTabCollab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.logo_beneficio_joven),
            contentDescription = "Logo de la App",
            modifier = Modifier.size(width = 45.dp, height = 33.dp)
        )
        Spacer(Modifier.height(24.dp))
        HeaderSectionCollab(collaborator)
        Spacer(Modifier.height(24.dp))
        CtaSectionCollab()
        Spacer(Modifier.height(24.dp))
        InfoCardCollab(
            icon = Icons.Outlined.LocalOffer,
            title = "Promociones Activas",
            description = "Visualiza, edita o desactiva las ofertas que tienes vigentes.",
            onClick = { onNavigateTo(BJTabCollab.Promotions) }
        )
        Spacer(Modifier.height(16.dp))
        InfoCardCollab(
            icon = Icons.Outlined.BarChart,
            title = "Estadísticas",
            description = "Analiza el rendimiento y la conversión de tus cupones.",
            onClick = { onNavigateTo(BJTabCollab.Stats) }
        )
        Spacer(Modifier.height(16.dp))
        InfoCardCollab(
            icon = Icons.Default.Person,
            title = "Perfil",
            description = "Modifica la información de tu negocio.",
            onClick = { onNavigateTo(BJTabCollab.Profile) }
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HeaderSectionCollab(collaborator: Collaborator) {
    val fallbackRes = R.drawable.logo_beneficio_joven

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(collaborator.logoUrl.takeIf { !it.isNullOrBlank() } ?: fallbackRes)
                    .crossfade(true)
                    .error(fallbackRes)
                    .build(),
                contentDescription = "Logo Colaborador",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = Teal)
                    }
                },
                success = { SubcomposeAsyncImageContent() },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(TextGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Logo Colaborador",
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }
                }
            )

            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hola, ${collaborator.representativeName?.split(" ")?.get(0) ?: "..."}",
                    style = TextStyle(
                        brush = TextGradient,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    ),
                    maxLines = 1
                )
                Text(
                    text = collaborator.businessName ?: "Colaborador",
                    fontSize = 14.sp,
                    color = TextGrey,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(32.dp).clickable { /* TODO: Nav a Ajustes Collab */ },
                tint = TextGrey
            )
        }
        Spacer(Modifier.height(16.dp))
        GradientDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}

@Composable
private fun CtaSectionCollab() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Listo para atraer más clientes?",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = TextGrey
        )
        Text(
            text = "Genera una nueva promoción",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextGrey
        )
    }
}

@Composable
private fun InfoCardCollab(icon: ImageVector, title: String, description: String, onClick: () -> Unit = {}) {
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
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(CardBorderGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = TextGrey,
                    modifier = Modifier.weight(1f)
                )
                Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Teal, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                color = TextLightGrey,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Pantallas Placeholder
@Composable
fun CollabStatsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Stats Screen", fontSize = 24.sp, color = TextGrey)
    }
}
@Composable
fun CollabPromotionsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Promotions Screen", fontSize = 24.sp, color = TextGrey)
    }
}
@Composable
fun CollabProfileScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Profile Screen", fontSize = 24.sp, color = TextGrey)
    }
}


@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun HomeScreenCollabPreview() {
    BeneficioJuventudTheme {
        HomeScreenCollab(nav = rememberNavController())
    }
}