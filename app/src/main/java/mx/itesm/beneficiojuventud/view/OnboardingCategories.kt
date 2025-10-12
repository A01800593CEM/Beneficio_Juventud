package mx.itesm.beneficiojuventud.view

import CategoryViewModel
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Pantalla de selección de categorías durante el onboarding de Beneficio Joven.
 * Permite al usuario elegir al menos tres intereses para personalizar sus promociones.
 * @param nav Controlador de navegación para gestionar la transición hacia la pantalla principal.
 * @param modifier Modificador opcional para ajustar el layout exterior.
 * @return Unit
 */
@Composable
fun OnboardingCategories(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    vm: CategoryViewModel = viewModel() // ⬅️ inyecta VM
) {
    val gradient = remember {
        Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96)))
    }

    // ⬇️ Estados del VM
    val categories by vm.categories.collectAsState()
    val isLoading by vm.loading.collectAsState(initial = false)
    val error by vm.error.collectAsState(initial = null)

    // ⬇️ Selección por ID (asumiendo Category.id:Int? y .name:String?)
    var selected by rememberSaveable { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp, 75.dp, 24.dp, 0.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Personaliza tu\nExperiencia",
            style = TextStyle(
                brush = gradient,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Selecciona al menos 3 categorías para personalizar tus promociones",
            style = TextStyle(fontSize = 14.sp, color = Color(0xFF7D7A7A)),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Cargando categorías...")
            }
            error != null -> {
                Text(
                    text = "Error al cargar categorías: ${error ?: ""}",
                    color = Color(0xFFB00020),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { vm.loadCategories() }) { Text("Reintentar") }
            }
            else -> {
                categories.forEach { c ->
                    val id = c.id ?: return@forEach
                    val name = c.name ?: "Categoría"

                    val isSel = selected.contains(id)
                    CategoryItem(
                        text = name,
                        selected = isSel,
                        gradient = gradient,
                        onClick = {
                            selected = if (isSel) selected - id else selected + id
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        MainButton(
            text = "Continuar",
            enabled = selected.size >= 3 && !isLoading && error == null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            // TODO: persiste `selected` en tu UserProfile.categories si aplica
            nav.navigate(Screens.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}

/**
 * Elemento visual para mostrar una categoría seleccionable.
 * Cambia su borde, color y muestra un ícono de check cuando está seleccionada.
 * @param text Nombre de la categoría.
 * @param selected Indica si la categoría está seleccionada.
 * @param gradient Gradiente aplicado al borde e ícono cuando está activa.
 * @param onClick Acción al presionar el elemento.
 * @return Unit
 */
@Composable
private fun CategoryItem(
    text: String,
    selected: Boolean,
    gradient: Brush,
    onClick: () -> Unit
) {
    val borderBrush = if (selected) gradient else SolidColor(Color(0xFFE5E5E5))
    val borderWidth = if (selected) 2.dp else 1.dp
    val textColor = if (selected) Color(0xFF008D96) else Color(0xFF616161)
    val weight = if (selected) FontWeight.Bold else FontWeight.Bold

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
            // Indicador circular con check o borde gris
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

/**
 * Previsualiza la pantalla de selección de categorías con el tema por defecto.
 * @return Unit
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingCategoriesPreview() {
    OnboardingCategories(nav = rememberNavController())
}
