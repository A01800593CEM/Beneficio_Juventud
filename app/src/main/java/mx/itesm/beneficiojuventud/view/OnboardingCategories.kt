// mx/itesm/beneficiojuventud/view/OnboardingCategories.kt
package mx.itesm.beneficiojuventud.view

import mx.itesm.beneficiojuventud.viewmodel.CategoryViewModel
import mx.itesm.beneficiojuventud.viewmodel.UserViewModel
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
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
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import androidx.compose.ui.graphics.RectangleShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
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

    // ---- State: AuthViewModel para obtener currentUserId ----
    val currentUserId by authViewModel.currentUserId.collectAsState()

    // Conjunto de IDs seleccionados
    var selected by rememberSaveable { mutableStateOf(setOf<Int>()) }

    // ⭐️ IMPORTANTE: Cargar el usuario si NO está cargado aún
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrBlank()) {
            Log.d("OnboardingCategories", "🔵 OnboardingCategories - currentUserId: $currentUserId")
            val currentUser = userViewModel.userState.value

            // Si el usuario NO está cargado O no coincide con el currentUserId, cargar
            if (currentUser == null || currentUser.cognitoId != currentUserId) {
                Log.d("OnboardingCategories", "📥 Cargando usuario para categorías: $currentUserId")
                userViewModel.getUserById(currentUserId!!)
            } else {
                Log.d("OnboardingCategories", "✅ Usuario ya cargado para categorías: ${currentUser.email}")
            }
        } else {
            Log.e("OnboardingCategories", "❌ currentUserId es null - No se puede cargar usuario")
        }
    }

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

        // ⭐️ IMPORTANTE: Mostrar carga si el usuario se está cargando
        if (user == null || user.cognitoId.isNullOrBlank()) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                "Preparando tu perfil...",
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF7D7A7A)),
                fontWeight = FontWeight.SemiBold
            )
        } else if (isLoadingCategories) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Cargando categorías...")
        } else if (errorCategories != null) {
            Text(
                text = "Error al cargar categorías: ${errorCategories ?: ""}",
                color = Color(0xFFB00020),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { categoryViewModel.loadCategories() }) { Text("Reintentar") }
        } else {
            // ✅ Mostrar categorías solo si el usuario Y las categorías se han cargado
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
        // ⭐️ Deshabilitado mientras se carga el usuario O las categorías
        MainButton(
            text = when {
                user == null || user.cognitoId.isNullOrBlank() -> "Preparando perfil..."
                isSaving -> "Guardando…"
                else -> "Continuar"
            },
            enabled = user != null &&
                     !user.cognitoId.isNullOrBlank() &&
                     selected.size >= 3 &&
                     !isLoadingCategories &&
                     errorCategories == null &&
                     !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            val cognitoId = user.cognitoId
            if (cognitoId.isNullOrBlank()) {
                Log.e("OnboardingCategories", "❌ No se puede continuar - cognitoId es null")
                return@MainButton
            }

            Log.d("OnboardingCategories", "✅ Guardando categorías para usuario: $cognitoId")

            // Construye la lista de categorías seleccionadas
            val selectedCats = categories.filter { it.id != null && selected.contains(it.id!!) }

            Log.d("OnboardingCategories", "📦 Categorías seleccionadas: ${selectedCats.map { it.name }.joinToString(", ")}")

            // Copia del perfil con categories actualizadas
            val payload = user.copy(categories = selectedCats)

            // Llama a tu update de backend
            userViewModel.updateUser(cognitoId, payload)

            Log.d("OnboardingCategories", "🔄 updateUser llamado - Navegando a Home en 1 segundo...")

            // Navega a Home después de un pequeño delay para permitir que se procese
            // ⭐️ Usar scope.launch (Main Thread) en lugar de GlobalScope.launch
            scope.launch {
                delay(1000)
                nav.navigate(Screens.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
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
                        .border(2.dp, gradient, RectangleShape),
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
                        .border(2.dp, Color(0xFFCBCBCB), RectangleShape)
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
