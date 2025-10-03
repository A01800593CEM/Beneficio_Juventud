package mx.itesm.beneficiojuventud.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.components.MainButton

@Composable
fun OnboardingCategoriesScreen(
    nav: NavHostController,
    modifier: Modifier = Modifier
) {
    val gradient = remember {
        Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96)))
    }

    val categories = listOf(
        "Alimentos",
        "Moda y Estilo",
        "Entretenimiento y Ocio",
        "Bienestar y Deporte",
        "Experiencias y Estilo de Vida"
    )

    var selected by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp, 75.dp,24.dp,0.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título con gradiente
        Text(
            "Personaliza tu\nExperiencia",
            style = TextStyle(
                brush = gradient,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Selecciona al menos 3 categorías para personalizar tus promociones",
            style = TextStyle(fontSize = 14.sp, color = Color(0xFF7D7A7A)),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        categories.forEach { c ->
            val isSel = selected.contains(c)
            CategoryItem(
                text = c,
                selected = isSel,
                gradient = gradient,
                onClick = {
                    selected = if (isSel) selected - c else selected + c
                }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(20.dp))

        MainButton(
            text = "Continuar",
            enabled = selected.size >= 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            // TODO: Guarda selección y navega a lo que siga del onboarding
            nav.navigate(Screens.MainMenu.route)
        }
    }
}

@Composable
private fun CategoryItem(
    text: String,
    selected: Boolean,
    gradient: Brush,
    onClick: () -> Unit
) {
    val borderBrush = if (selected) gradient else SolidColor(Color(0xFFE5E5E5))
    val borderWidth = if (selected) 2.dp else 1.dp
    val textColor = if (selected) Color(0xFF008D96) else Color(0xFF2F2F2F)
    val weight = if (selected) FontWeight.SemiBold else FontWeight.Medium

    // Card + borde con Brush para replicar el estilo
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp), clip = false)
            .border(borderWidth, borderBrush, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador circular a la izquierda (vacío o con check y borde en gradiente)
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(2.dp, gradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF008D96),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .border(2.dp, Color(0xFFCBCBCB), CircleShape)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = weight,
                    color = textColor
                )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingCategoriesPreview() {
    OnboardingCategoriesScreen(nav = rememberNavController())
}
