package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.*
import mx.itesm.beneficiojuventud.ui.theme.BeneficioJuventudTheme
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.viewcollab.ProfileViewModel

private val TextGrey = Color(0xFF616161)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val SubtitleGray = Color(0xFF7D7A7A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    nav: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf<BJTabCollab>(BJTabCollab.Profile) }

    // Control del Dialog
    uiState.isEditingBranch?.let { branch ->
        EditSucursalDialog(
            branch = branch,
            onDismiss = viewModel::onDismissEditDialog,
            onSave = { updatedBranch -> viewModel.onSaveChangesForBranch(updatedBranch) },
            onDelete = { /* TODO: Lógica de eliminar */ viewModel.onDismissEditDialog() }
        )
    }

    Scaffold(
        bottomBar = {
            BJBottomBarCollab(selected = selectedTab, onSelect = { newTab -> selectedTab = newTab }, onAddClick = { /* TODO */ })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues)) {
            ProfileScreenHeader(nav = nav)
            Column(
                modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(24.dp).dismissKeyboardOnTap(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileImageSection()
                Spacer(Modifier.height(24.dp))
                ProfileTextField(value = uiState.contactName, onValueChange = viewModel::onContactNameChange, label = "Nombre del Contacto", leadingIcon = Icons.Default.Person)
                Spacer(Modifier.height(16.dp))
                ProfileTextField(value = uiState.businessName, onValueChange = viewModel::onBusinessNameChange, label = "Nombre del Negocio", leadingIcon = Icons.Default.Store)
                Spacer(Modifier.height(16.dp))
                ProfileTextField(value = uiState.email, onValueChange = viewModel::onEmailChange, label = "Correo Electrónico", leadingIcon = Icons.Default.Email)
                Spacer(Modifier.height(16.dp))
                ProfileTextField(value = uiState.phone, onValueChange = viewModel::onPhoneChange, label = "Teléfono", leadingIcon = Icons.Default.Phone)
                Spacer(Modifier.height(16.dp))
                ProfileDropdownField(value = uiState.category, label = "Categoría", leadingIcon = Icons.Default.Category, onClick = { /* TODO */ })
                Spacer(Modifier.height(16.dp))
                ProfileTextField(value = uiState.description, onValueChange = viewModel::onDescriptionChange, label = "Descripción", leadingIcon = Icons.Default.Description, maxLines = 4)
                Spacer(Modifier.height(16.dp))

                // Sección de Sucursales
                Text(
                    "Sucursales",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubtitleGray,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                uiState.branches.forEach { branch ->
                    SucursalCard(branch = branch, onEditClick = { viewModel.onEditBranchClicked(branch) })
                    Spacer(Modifier.height(8.dp))
                }
                Button(onClick = { /* TODO: Agregar sucursal */ }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar Sucursal")
                }

                Spacer(Modifier.height(24.dp))
                SaveChangesButton(onClick = { /* TODO */ })
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileImageSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(DarkBlue, Teal))), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Business, contentDescription = "Logo del negocio", tint = Color.White, modifier = Modifier.size(60.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(text = "Cambiar Foto", color = Teal, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { /* TODO */ })
    }
}

@Composable
private fun SaveChangesButton(onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(DarkBlue, Teal))), contentAlignment = Alignment.Center) {
            Text(
                "Guardar Cambios",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ProfileScreenHeader(nav: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextGrey) }
            Spacer(Modifier.width(16.dp))
            Text(text = "Editar Perfil", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextGrey)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* TODO */ }) { Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Ajustes", tint = TextGrey) }
        }
        Spacer(Modifier.height(16.dp))
        GradientDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    BeneficioJuventudTheme {
        ProfileScreen(nav = rememberNavController())
    }
}