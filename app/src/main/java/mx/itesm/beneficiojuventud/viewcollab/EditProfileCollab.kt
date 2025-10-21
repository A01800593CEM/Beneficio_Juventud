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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.model.categories.Category
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.collaborators.CollaboratorsState
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.view.Screens
import mx.itesm.beneficiojuventud.view.StatusType
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel

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

    // Estados base
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val collab by collabViewModel.collabState.collectAsState()

    // Imagen
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }

    // Campos de formulario
    var contactName by rememberSaveable { mutableStateOf("") }
    var businessName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var rfc by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var postalCode by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    // Estado (enum)
    var selectedState by rememberSaveable { mutableStateOf<CollaboratorsState?>(null) }
    var stateDisplay by rememberSaveable { mutableStateOf("") }

    // Categorías
    var allCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategoryIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var categoryDisplay by rememberSaveable { mutableStateOf("") }

    // Sheets (Estado y Categorías)
    var showStateSheet by remember { mutableStateOf(false) }
    var showCategoriesSheet by remember { mutableStateOf(false) }
    val stateSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val catSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Save state tracking
    var justSaved by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    // Cargar colaborador
    LaunchedEffect(currentUserId) {
        currentUserId?.let { id ->
            runCatching { collabViewModel.getCollaboratorById(id) }
                .onFailure { Log.e("EditProfileCollab", "Error loading collaborator: ${it.message}") }
        }
        // Cargar catálogo de categorías
        runCatching { collabViewModel.getCategories() }
            .onSuccess { allCategories = it }
            .onFailure { Log.e("EditProfileCollab", "Error loading categories: ${it.message}") }
    }

    // Poblar campos e imagen al llegar collab
    LaunchedEffect(collab) {
        contactName = collab.representativeName.orEmpty()
        businessName = collab.businessName.orEmpty()
        email = collab.email.orEmpty()
        phone = collab.phone.orEmpty()
        rfc = collab.rfc.orEmpty()
        address = collab.address.orEmpty()
        postalCode = collab.postalCode.orEmpty()
        description = collab.description.orEmpty()

        selectedState = collab.state
        stateDisplay = collab.state?.name.orEmpty()

        // Categorías preseleccionadas
        selectedCategoryIds =
            (collab.categoryIds?.toSet()
                ?: collab.categories?.mapNotNull { it.id }?.toSet()
                ?: emptySet())
        categoryDisplay = collab.categories?.joinToString(" · ") { it.name ?: "" }
            ?: ""

        val s3Id = collab.cognitoId ?: currentUserId
        when {
            !collab.logoUrl.isNullOrBlank() -> profileImageUrl = collab.logoUrl
            !s3Id.isNullOrBlank() -> runCatching {
                // Implementación propia existente en tu proyecto:
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

    // Recalcular display de categorías si cambian ids o catálogo
    LaunchedEffect(selectedCategoryIds, allCategories) {
        if (selectedCategoryIds.isEmpty()) {
            categoryDisplay = ""
        } else {
            val names = allCategories.filter { it.id in selectedCategoryIds }.mapNotNull { it.name }
            categoryDisplay = names.joinToString(" · ")
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = { BJBottomBarCollab(nav) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
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

                    // Representante / Negocio
                    ProfileTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = "Nombre del Contacto",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(Modifier.height(12.dp))

                    ProfileTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = "Nombre del Negocio",
                        leadingIcon = Icons.Default.Store
                    )
                    Spacer(Modifier.height(12.dp))

                    // Contacto
                    ProfileTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo Electrónico",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(Modifier.height(12.dp))

                    ProfileTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Teléfono",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(Modifier.height(12.dp))

                    // RFC / Dirección / CP
                    ProfileTextField(
                        value = rfc,
                        onValueChange = { rfc = it.uppercase() },
                        label = "RFC",
                        leadingIcon = Icons.Default.Badge
                    )
                    Spacer(Modifier.height(12.dp))

                    ProfileTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Dirección",
                        leadingIcon = Icons.Default.LocationOn
                    )
                    Spacer(Modifier.height(12.dp))

                    ProfileTextField(
                        value = postalCode,
                        onValueChange = { postalCode = it.filter { ch -> ch.isDigit() }.take(5) },
                        label = "Código Postal",
                        leadingIcon = Icons.Default.MarkunreadMailbox,
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(12.dp))

                    // Categorías
                    ProfileDropdownField(
                        value = if (categoryDisplay.isBlank()) "Selecciona categorías" else categoryDisplay,
                        label = "Categorías",
                        leadingIcon = Icons.Default.Category,
                        onClick = { showCategoriesSheet = true }
                    )
                    Spacer(Modifier.height(12.dp))

                    // Descripción grande (multilínea)
                    ProfileMultilineField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Descripción del Negocio",
                        leadingIcon = Icons.Default.Description,
                        minLines = 4,
                        maxLines = 8
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
                                rfc = rfc.ifBlank { null },
                                address = address.ifBlank { null },
                                postalCode = postalCode.ifBlank { null },
                                description = description.ifBlank { null },
                                // Envía solo IDs (tu backend los usa para actualizar)
                                categoryIds = if (selectedCategoryIds.isEmpty()) null else selectedCategoryIds.toList(),
                                // Estado
                                state = selectedState
                            )

                            justSaved = true
                            scope.launch {
                                runCatching { collabViewModel.updateCollaborator(id, update) }
                                    .onSuccess {
                                        saveSuccess = true
                                        saveError = null
                                    }
                                    .onFailure {
                                        saveSuccess = false
                                        saveError = it.message ?: "Error desconocido"
                                    }
                            }
                        }
                    )

                    Spacer(Modifier.height(92.dp)) // margen para no chocar con bottom bar
                }
            }

            // Manejar navegación a StatusScreen cuando se guarda
            LaunchedEffect(justSaved, saveSuccess, saveError) {
                if (justSaved && (saveSuccess || saveError != null)) {
                    justSaved = false

                    if (saveSuccess) {
                        // Éxito: navegar a StatusScreen que muestra éxito y luego va a ProfileCollab
                        nav.navigate(
                            Screens.Status.createRoute(
                                StatusType.USER_INFO_UPDATED,
                                Screens.ProfileCollab.route
                            )
                        ) {
                            // Limpia la pila de navegación para que no se pueda volver a EditProfileCollab
                            popUpTo(Screens.EditProfileCollab.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        // Error: navegar a StatusScreen que muestra error y luego vuelve a EditProfileCollab
                        nav.navigate(
                            Screens.Status.createRoute(
                                StatusType.USER_INFO_UPDATE_ERROR,
                                Screens.EditProfileCollab.route
                            )
                        ) {
                            // Limpia la pila de navegación para que no se pueda volver atrás al EditProfileCollab anterior
                            popUpTo(Screens.EditProfileCollab.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    // Reset flags
                    saveSuccess = false
                    saveError = null
                }
            }

            // Sheet: Estado
            if (showStateSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showStateSheet = false },
                    sheetState = stateSheet
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Selecciona el estado", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        StateOptionItem(
                            title = "activo",
                            selected = selectedState == CollaboratorsState.activo
                        ) {
                            selectedState = CollaboratorsState.activo
                            stateDisplay = "activo"
                            showStateSheet = false
                        }
                        StateOptionItem(
                            title = "inactivo",
                            selected = selectedState == CollaboratorsState.inactivo
                        ) {
                            selectedState = CollaboratorsState.inactivo
                            stateDisplay = "inactivo"
                            showStateSheet = false
                        }
                        StateOptionItem(
                            title = "suspendido",
                            selected = selectedState == CollaboratorsState.suspendido
                        ) {
                            selectedState = CollaboratorsState.suspendido
                            stateDisplay = "suspendido"
                            showStateSheet = false
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }

            // Sheet: Categorías
            if (showCategoriesSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCategoriesSheet = false },
                    sheetState = catSheet
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Selecciona categorías", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        if (allCategories.isEmpty()) {
                            Text("No hay categorías disponibles", color = TextGrey)
                        } else {
                            allCategories.forEach { cat ->
                                val id = cat.id ?: return@forEach
                                val checked = id in selectedCategoryIds
                                CategoryCheckboxItem(
                                    name = cat.name ?: "Sin nombre",
                                    checked = checked
                                ) { isChecked ->
                                    selectedCategoryIds =
                                        if (isChecked) selectedCategoryIds + id
                                        else selectedCategoryIds - id
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { showCategoriesSheet = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Listo") }
                        Spacer(Modifier.height(16.dp))
                    }
                }
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
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(DarkBlue, Teal))),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
                !imageUrl.isNullOrBlank() -> AsyncImage(
                    model = imageUrl,
                    contentDescription = "Logo del negocio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF008D96),
            unfocusedBorderColor = Color(0xFFD3D3D3),
            cursorColor = Color(0xFF008D96),
            focusedLabelColor = Color(0xFF008D96)
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    minLines: Int = 4,
    maxLines: Int = 8
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = Color(0xFFAEAEAE), fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
        leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = null, tint = Color(0xFF616161), modifier = Modifier.size(28.dp)) },
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF008D96),
            unfocusedBorderColor = Color(0xFFD3D3D3),
            cursorColor = Color(0xFF008D96),
            focusedLabelColor = Color(0xFF008D96)
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF616161))
    )
}

@Composable
private fun StateOptionItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = title,
            tint = if (selected) Teal else TextGrey
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = TextGrey
        )
    }
}

@Composable
private fun CategoryCheckboxItem(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(12.dp))
        Text(text = name, fontSize = 16.sp, color = TextGrey, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SaveChangesButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(DarkBlue, Teal))),
            contentAlignment = Alignment.Center
        ) {
            Text("Guardar Cambios", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ProfileScreenHeader(nav: NavHostController) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)) {
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
