package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.model.webhook.PromotionData

private val TextGrey = Color(0xFF616161)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratePromotionScreen(nav: NavHostController) {

    val sheetStateManual = rememberModalBottomSheetState()
    var showBottomSheetManual by remember { mutableStateOf(false) }

    val sheetStateAI = rememberModalBottomSheetState()
    var showBottomSheetAI by remember { mutableStateOf(false) }

    var aiGeneratedPromotion by remember { mutableStateOf<PromotionData?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BJBottomBarCollab(nav)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Image(painter = painterResource(id = R.drawable.logo_beneficio_joven), contentDescription = "Logo de la App", modifier = Modifier.size(width = 45.dp, height = 33.dp))
            Spacer(Modifier.height(24.dp))
            GeneratePromotionHeader(nav = nav)
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¿Listo para atraer más clientes?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextGrey, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("Elige cómo quieres crear tu nueva promoción", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGrey, textAlign = TextAlign.Center)
                Spacer(Modifier.height(32.dp))

                PromotionCreationCard(
                    icon = Icons.Filled.AddBox,
                    title = "Crear Manualmente",
                    description = "Diseña tu promoción, paso a paso, con total control sobre cada detalle.",
                    onClick = {
                        aiGeneratedPromotion = null
                        showBottomSheetManual = true
                    }
                )
                Spacer(Modifier.height(24.dp))
                PromotionCreationCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "Generar con IA",
                    description = "¿No sabes qué cupón crear? Deja que la IA lo haga por ti.",
                    onClick = { showBottomSheetAI = true }
                )
            }
        }
    }

    if (showBottomSheetManual) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheetManual = false },
            sheetState = sheetStateManual
        ) {
            NewPromotionSheet(
                onClose = {
                    scope.launch { sheetStateManual.hide() }.invokeOnCompletion {
                        if (!sheetStateManual.isVisible) {
                            showBottomSheetManual = false
                        }
                    }
                },
                initialPromotionData = aiGeneratedPromotion
            )
        }
    }

    if (showBottomSheetAI) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheetAI = false },
            sheetState = sheetStateAI
        ) {
            GenerateWithAISheet(
                onClose = {
                    scope.launch { sheetStateAI.hide() }.invokeOnCompletion {
                        if (!sheetStateAI.isVisible) {
                            showBottomSheetAI = false
                        }
                    }
                },
                onGeneratePromotion = { promotionData ->
                    aiGeneratedPromotion = promotionData

                    scope.launch {
                        sheetStateAI.hide()
                    }.invokeOnCompletion {
                        if (!sheetStateAI.isVisible) {
                            showBottomSheetAI = false
                            showBottomSheetManual = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun GeneratePromotionHeader(nav: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextGrey) }
            Spacer(Modifier.width(16.dp))
            Text(text = "Generar Promoción", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextGrey)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }) { Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Ajustes", tint = TextGrey) }
        }
        Spacer(Modifier.height(16.dp))
        GradientDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}

@Preview(showBackground = true)
@Composable
private fun GeneratePromotionScreenPreview() {
    BeneficioJuventudTheme {
        GeneratePromotionScreen(nav = rememberNavController())
    }
}