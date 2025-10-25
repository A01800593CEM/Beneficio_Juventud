package mx.itesm.beneficiojuventud.viewcollab

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.amplifyframework.storage.StoragePath
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.components.GradientDivider
import mx.itesm.beneficiojuventud.model.categories.Category
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.viewcollab.ProfileDropdownField
import mx.itesm.beneficiojuventud.model.collaborators.CollaboratorsState
import mx.itesm.beneficiojuventud.utils.dismissKeyboardOnTap
import mx.itesm.beneficiojuventud.view.Screens
import mx.itesm.beneficiojuventud.view.StatusType
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.CollabViewModel
import java.io.File
import java.io.FileOutputStream

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
    var isUploading by remember { mutableStateOf(false) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var s3ImageUrl by remember { mutableStateOf<String?>(null) }  // URL de S3 para enviar al servidor

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

    // Snackbar State - Debe declararse aquí antes de ser usado
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar colaborador y categorías
    LaunchedEffect(currentUserId) {
        currentUserId?.let { id ->
            // Cargar colaborador
            runCatching { collabViewModel.getCollaboratorById(id) }
                .onSuccess { Log.d("EditProfileCollab", "Collaborator loaded: categories=${collab.categories?.map { it.id }}") }
                .onFailure { Log.e("EditProfileCollab", "Error loading collaborator: ${it.message}") }
        }
    }

    // Cargar catálogo de categorías en un scope separado
    LaunchedEffect(Unit) {
        scope.launch {
            runCatching {
                allCategories = collabViewModel.getCategories()
            }.onFailure { Log.e("EditProfileCollab", "Error loading categories: ${it.message}") }
        }
    }

    // Image picker launcher
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri
            val s3Id = collab.cognitoId ?: currentUserId
            if (!s3Id.isNullOrBlank()) {
                uploadProfileImage(
                    context = context,
                    imageUri = uri,
                    userId = s3Id,
                    onSuccess = { url ->
                        profileImageUrl = url  // Muestra inmediatamente la URL de S3
                        s3ImageUrl = url      // Guarda para enviar al servidor
                        scope.launch { snackbarHostState.showSnackbar("Foto de perfil actualizada correctamente") }
                    },
                    onError = { error ->
                        scope.launch { snackbarHostState.showSnackbar("Error al subir la imagen: $error") }
                    },
                    onLoading = { loading -> isUploading = loading }
                )
            }
        }
    }

    // Cargar datos del colaborador
    LaunchedEffect(currentUserId) {
        currentUserId?.let { id ->
            Log.d("EditProfileCollab", "Loading collaborator on edit: $id")
            runCatching { collabViewModel.getCollaboratorById(id) }
                .onSuccess { Log.d("EditProfileCollab", "Collaborator loaded successfully") }
                .onFailure { Log.e("EditProfileCollab", "Error loading collaborator: ${it.message}") }
        }
    }

    // Poblar campos cuando llega collab
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

        Log.d("EditProfileCollab", "Setting categories from collab:")
        Log.d("EditProfileCollab", "  collab.categoryIds: ${collab.categoryIds}")
        Log.d("EditProfileCollab", "  collab.categories: ${collab.categories?.map { "${it.id}:${it.name}" }}")
        Log.d("EditProfileCollab", "  selectedCategoryIds: $selectedCategoryIds")

        categoryDisplay = collab.categories?.joinToString(" · ") { it.name ?: "" }
            ?: ""

        Log.d("EditProfileCollab", "  categoryDisplay: $categoryDisplay")
    }

    // Descargar imagen existente al entrar (igual a EditProfile.kt)
    LaunchedEffect(currentUserId) {
        currentUserId?.let { id ->
            if (id.isNotBlank()) {
                Log.d("EditProfileCollab", "Downloading existing image for: $id")
                runCatching {
                    downloadProfileImageForDisplayCollab(
                        context = context,
                        userId = id,
                        onSuccess = { localPath -> profileImageUrl = localPath },
                        onError = { Log.d("EditProfileCollab", "Storage not configured yet: $it") },
                        onLoading = { loading -> isLoadingImage = loading }
                    )
                }
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
                    ProfileImageSection(
                    isLoading = isLoadingImage,
                    imageUrl = profileImageUrl,
                    onChangePhoto = {
                        if (!isUploading && !currentUserId.isNullOrBlank()) {
                            pickImage.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    },
                    isUploading = isUploading
                )
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
                                state = selectedState,
                                // URL de imagen: SOLO si es la primera vez (logoUrl actual es null)
                                // Si ya existe logoUrl, NO enviar nada (el link de imagen no cambia)
                                logoUrl = if (collab.logoUrl.isNullOrBlank() && !s3ImageUrl.isNullOrBlank()) s3ImageUrl else null
                            )

                            Log.d("EditProfileCollab", "Guardando cambios...")
                            Log.d("EditProfileCollab", "s3ImageUrl: $s3ImageUrl")
                            Log.d("EditProfileCollab", "s3ImageUrl.isNullOrBlank(): ${s3ImageUrl.isNullOrBlank()}")
                            Log.d("EditProfileCollab", "selectedCategoryIds: $selectedCategoryIds")
                            Log.d("EditProfileCollab", "categoryIds en Collaborator: ${update.categoryIds}")
                            Log.d("EditProfileCollab", "logoUrl en Collaborator: ${update.logoUrl}")
                            Log.d("EditProfileCollab", "Update object: $update")

                            justSaved = true
                            scope.launch {
                                runCatching { collabViewModel.updateCollaborator(id, update) }
                                    .onSuccess {
                                        Log.d("EditProfileCollab", "Update successful, waiting and reloading collaborator...")
                                        // Esperar un poco para asegurar que el backend procesó el cambio
                                        kotlinx.coroutines.delay(500)
                                        // Reload the collaborator to get updated categories and image
                                        runCatching { collabViewModel.getCollaboratorById(id) }
                                            .onSuccess {
                                                Log.d("EditProfileCollab", "Collaborator reloaded successfully with categories")
                                                saveSuccess = true
                                                saveError = null
                                            }
                                            .onFailure {
                                                Log.e("EditProfileCollab", "Error reloading: ${it.message}")
                                                saveSuccess = false
                                                saveError = "Error al cargar datos actualizados: ${it.message}"
                                            }
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
                        // Antes de navegar, refrescar el colaborador para asegurar que ProfileCollab tenga data actualizada
                        currentUserId?.let { id ->
                            Log.d("EditProfileCollab", "Refrescando colaborador antes de navegar a Status")
                            runCatching { collabViewModel.getCollaboratorById(id) }
                                .onSuccess {
                                    Log.d("EditProfileCollab", "Colaborador refrescado, navegando a Status")
                                    // Esperar un poco más para asegurar que el estado se actualizó
                                    scope.launch {
                                        kotlinx.coroutines.delay(200)
                                        nav.navigate(
                                            Screens.Status.createRoute(
                                                StatusType.USER_INFO_UPDATED,
                                                Screens.ProfileCollab.route
                                            )
                                        )
                                    }
                                }
                                .onFailure {
                                    Log.e("EditProfileCollab", "Error refrescando: ${it.message}")
                                    nav.navigate(
                                        Screens.Status.createRoute(
                                            StatusType.USER_INFO_UPDATED,
                                            Screens.ProfileCollab.route
                                        )
                                    )
                                }
                        }
                    } else {
                        // Error: navegar a StatusScreen que muestra error y luego vuelve a EditProfileCollab
                        nav.navigate(
                            Screens.Status.createRoute(
                                StatusType.USER_INFO_UPDATE_ERROR,
                                Screens.EditProfileCollab.route
                            )
                        )
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
    imageUrl: String?,
    onChangePhoto: () -> Unit,
    isUploading: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when {
                isUploading || isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF008D96)
                )
                imageUrl != null -> AsyncImage(
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
                    tint = Color(0xFF008D96),
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Cambiar Foto",
            color = Color(0xFF008D96),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(enabled = !isUploading) { onChangePhoto() }
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
            Text(text = "Editar Perfil", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextGrey)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { /* ajustes */ }) {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Ajustes", tint = TextGrey)
            }
        }
        Spacer(Modifier.height(16.dp))
        GradientDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}

/* ---------- Image Upload/Download Functions ---------- */

fun uploadProfileImage(
    context: android.content.Context,
    imageUri: Uri,
    userId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    try {
        onLoading(true)
        Log.d("CollabProfileUpload", "Starting upload for collaborator: $userId")

        val inputStream = context.contentResolver.openInputStream(imageUri)
        val tempFile = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        val storagePath = StoragePath.fromString("public/profile-images/$userId.jpg")
        Log.d("CollabProfileUpload", "Storage path: public/profile-images/$userId.jpg")

        Amplify.Storage.uploadFile(
            storagePath,
            tempFile,
            { result ->
                Log.d("CollabProfileUpload", "Upload completed: ${result.path}")
                onLoading(false)
                tempFile.delete()
                getProfileImageUrl(userId, onSuccess, onError)
            },
            { error ->
                Log.e("CollabProfileUpload", "Upload failed", error)
                onLoading(false)
                tempFile.delete()
                onError(error.message ?: "Error desconocido")
            }
        )
    } catch (e: Exception) {
        Log.e("CollabProfileUpload", "Exception during upload", e)
        onLoading(false)
        onError(e.message ?: "Error desconocido")
    }
}

private fun getProfileImageUrl(
    userId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        // Construir URL base sin parámetros firmados (para guardar en BD)
        // El backend y ProfileCollab construirán la URL firmada cuando sea necesario
        val bucketName = "beneficiojuventud-profile-images"
        val region = "us-east-2"

        // URL base sin parámetros (válida para almacenar en BD)
        val stableUrl = "https://$bucketName.s3.$region.amazonaws.com/public/profile-images/$userId.jpg"

        Log.d("CollabProfileDownload", "Got stable URL: $stableUrl")
        onSuccess(stableUrl)
    } catch (e: Exception) {
        Log.e("CollabProfileDownload", "Failed to construct URL", e)
        onError(e.message ?: "Error construyendo URL")
    }
}

fun downloadProfileImageForDisplayCollab(
    context: android.content.Context,
    userId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    try {
        onLoading(true)
        Log.d("CollabProfileDownload", "Starting download for collaborator: $userId")

        val storagePath = StoragePath.fromString("public/profile-images/$userId.jpg")
        Log.d("CollabProfileDownload", "Storage path: public/profile-images/$userId.jpg")
        val localFile = File(context.cacheDir, "displayed_profile_$userId.jpg")

        Amplify.Storage.downloadFile(
            storagePath,
            localFile,
            { result ->
                Log.d("CollabProfileDownload", "Download completed: ${result.file.path}")
                onLoading(false)
                onSuccess(localFile.absolutePath)
            },
            { error ->
                Log.e("CollabProfileDownload", "Download failed", error)
                onLoading(false)
                onError(error.message ?: "Error desconocido")
            }
        )
    } catch (e: Exception) {
        Log.e("CollabProfileDownload", "Exception during download", e)
        onLoading(false)
        onError(e.message ?: "Error desconocido")
    }
}
