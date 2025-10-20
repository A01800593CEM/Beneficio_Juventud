// mx/itesm/beneficiojuventud/view/OnboardingCategories.kt
package mx.itesm.beneficiojuventud.view

import mx.itesm.beneficiojuventud.viewmodel.CategoryViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import mx.itesm.beneficiojuventud.components.MainButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

/**
 * Pantalla de selección de categorías del Onboarding.
 * Mantiene el diseño anterior (título con gradiente + cards con check)
 * pero conserva la lógica de actualizar el usuario vía userViewModel.updateUser(...)
 * usando el campo `categories` del UserProfile.
 */
@Composable
fun OnboardingCategories(
    nav: NavHostController,
    modifier: Modifier = Modifier,
    categoryViewModel: CategoryViewModel,
    userViewModel: UserViewModel
) {
    // Gradiente y colores como en el diseño anterior
    val gradient = remember {
        Brush.linearGradient(listOf(Color(0xFF4B4C7E), Color(0xFF008D96)))
    }

    // ---- State: Categorías (desde CategoryViewModel) ----
    val categories by categoryViewModel.categories.collectAsState()
    val isLoadingCategories by categoryViewModel.loading.collectAsState(initial = false)
    val errorCategories by categoryViewModel.error.collectAsState(initial = null)

    // ---- State: Usuario (desde UserViewModel) ----
    val user by userViewModel.userState.collectAsState()
    val isSaving by userViewModel.isLoading.collectAsState()
    val saveError by userViewModel.error.collectAsState()

    // Conjunto de IDs seleccionados
    var selected by rememberSaveable { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp, 75.dp, 24.dp, 0.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título con gradiente (como tu diseño previo)
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
            isLoadingCategories -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Cargando categorías...")
            }

            errorCategories != null -> {
                Text(
                    text = "Error al cargar categorías: ${errorCategories ?: ""}",
                    color = Color(0xFFB00020),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { categoryViewModel.loadCategories() }) { Text("Reintentar") }
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

        Spacer(Modifier.height(12.dp))

        // Error al guardar usuario (si lo hubiera)
        if (saveError != null) {
            Text(
                text = saveError ?: "",
                color = Color(0xFFB00020),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Botón continuar — usando tu MainButton
        MainButton(
            text = if (isSaving) "Guardando…" else "Continuar",
            enabled = selected.size >= 3 && !isLoadingCategories && errorCategories == null && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            val cognitoId = user.cognitoId
            if (cognitoId.isNullOrBlank()) {
                // Ajusta a tu flujo (snackbar / navegar a login, etc.)
                return@MainButton
            }

            // Construye la lista de categorías seleccionadas
            val selectedCats = categories.filter { it.id != null && selected.contains(it.id!!) }

            // Copia del perfil con categories actualizadas
            val payload = user.copy(categories = selectedCats)

            // Llama a tu update de backend
            userViewModel.updateUser(cognitoId, payload)

            // Navega a Home (si prefieres, puedes esperar confirmación con un LaunchedEffect)
            nav.navigate(Screens.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}

/**
 * Card seleccionable de categoría (diseño previo: borde con gradiente, check circular).
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
    val weight = FontWeight.Bold

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
                        tint = Color(0xFF008D96)
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
