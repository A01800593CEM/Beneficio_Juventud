package mx.itesm.beneficiojuventud.viewcollab

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.components.GradientDivider

private val TextGrey = Color(0xFF616161)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileCollab(
    nav: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    collabViewModel: CollabViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val collab by collabViewModel.collabState.collectAsState()

    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    var contactName by rememberSaveable { mutableStateOf("") }
    var businessName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var categoryDisplay by rememberSaveable { mutableStateOf("") }

    // Cargar colaborador
    LaunchedEffect(currentUserId) {
        currentUserId?.let { id ->
            runCatching { collabViewModel.getCollaboratorById(id) }
                .onFailure { Log.e("EditProfileCollab", "Error loading collaborator: ${it.message}") }
        }
    }

    // Poblar campos e imagen al llegar collab
    LaunchedEffect(collab) {
        contactName = collab.representativeName.orEmpty()
        businessName = collab.businessName.orEmpty()
        email = collab.email.orEmpty()
        phone = collab.phone.orEmpty()
        description = collab.description.orEmpty()
        categoryDisplay = collab.categories?.joinToString(" · ") { it.name ?: "" } ?: ""

        val s3Id = collab.cognitoId ?: currentUserId
        when {
            !collab.logoUrl.isNullOrBlank() -> profileImageUrl = collab.logoUrl
            !s3Id.isNullOrBlank() -> runCatching {
                downloadProfileImageForDisplay(
                    context = context,
                    userId = s3Id,
                    onSuccess = { localPath -> profileImageUrl = localPath },
                    onError = { /* ignore */ },
                    onLoading = { loading -> isLoadingImage = loading }
                )
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = { BJBottomBarCollab(nav) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            ProfileScreenHeader(nav)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .dismissKeyboardOnTap(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileImageSection(isLoading = isLoadingImage, imageUrl = profileImageUrl)
                Spacer(Modifier.height(24.dp))

                ProfileTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = "Nombre del Contacto",
                    leadingIcon = Icons.Default.Person
                )
                Spacer(Modifier.height(16.dp))

                ProfileTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = "Nombre del Negocio",
                    leadingIcon = Icons.Default.Store
                )
                Spacer(Modifier.height(16.dp))

                ProfileTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo Electrónico",
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )
                Spacer(Modifier.height(16.dp))

                ProfileTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Teléfono",
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )
                Spacer(Modifier.height(16.dp))

                ProfileDropdownField(
                    value = categoryDisplay,
                    label = "Categoría",
                    leadingIcon = Icons.Default.Category,
                    onClick = { /* TODO: abrir selector de categorías */ }
                )
                Spacer(Modifier.height(16.dp))

                ProfileTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    leadingIcon = Icons.Default.Description
                )
                Spacer(Modifier.height(24.dp))

                SaveChangesButton(
                    onClick = {
                        val id = collab.cognitoId ?: currentUserId
                        if (id.isNullOrBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("No se encontró el ID del colaborador.") }
                            return@SaveChangesButton
                        }

                        val update = Collaborator(
                            cognitoId = id,
                            businessName = businessName.ifBlank { null },
                            representativeName = contactName.ifBlank { null },
                            phone = phone.ifBlank { null },
                            email = email.ifBlank { null },
                            description = description.ifBlank { null },
                            // Conserva categorías existentes (ajusta si agregas selector)
                            categories = collab.categories,
                            categoryIds = collab.categoryIds
                        )

                        scope.launch {
                            runCatching { collabViewModel.updateCollaborator(id, update) }
                                .onSuccess { snackbarHostState.showSnackbar("Cambios guardados.") }
                                .onFailure { snackbarHostState.showSnackbar("Error al guardar: ${it.message ?: "desconocido"}") }
                        }
                    }
                )

                // Separación para no chocar con la bottom bar
                Spacer(Modifier.height(92.dp))
            }
        }
    }
}

/* ---------- UI Helpers (locales a este archivo) ---------- */

@Composable
private fun ProfileImageSection(
    isLoading: Boolean,
    imageUrl: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(DarkBlue, Teal))),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color.White, strokeWidth = 3.dp)
                !imageUrl.isNullOrBlank() -> AsyncImage(
                    model = imageUrl,
                    contentDescription = "Logo del negocio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(CircleShape)
                )
                else -> Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Logo del negocio",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Cambiar Foto",
            color = Teal,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { /* TODO: flujo de cambio de foto */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = Color(0xFFAEAEAE), fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
        leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = null, tint = Color(0xFF616161), modifier = Modifier.size(28.dp)) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF008D96),
            unfocusedBorderColor = Color(0xFFD3D3D3),
            cursorColor = Color(0xFF008D96),
            focusedLabelColor = Color(0xFF008D96)
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF616161))
    )
}



@Composable
private fun SaveChangesButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(DarkBlue, Teal))),
            contentAlignment = Alignment.Center
        ) {
            Text("Guardar Cambios", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ProfileScreenHeader(nav: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextGrey)
            }
            Spacer(Modifier.width(16.dp))
            Text(text = "Editar Perfil (Colaborador)", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextGrey)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* ajustes */ }) {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Ajustes", tint = TextGrey)
            }
        }
        Spacer(Modifier.height(16.dp))
        GradientDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}
