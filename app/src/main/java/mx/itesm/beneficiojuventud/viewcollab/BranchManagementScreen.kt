package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.components.BJTopHeader
import mx.itesm.beneficiojuventud.model.Branch
import mx.itesm.beneficiojuventud.model.BranchState
import mx.itesm.beneficiojuventud.model.branch.CreateBranchRequest
import mx.itesm.beneficiojuventud.model.branch.UpdateBranchRequest
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.BranchViewModel

private val Teal = Color(0xFF008D96)
private val DarkBlue = Color(0xFF4B4C7E)
private val TextColor = Color(0xFF616161)
private val TextSecondary = Color(0xFFAEAEAE)
private val CardWhite = Color(0xFFFFFFFF)
private val BorderColor = Color(0xFFE0E0E0)
private val ActiveGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchManagementScreen(
    nav: NavHostController,
    branchViewModel: BranchViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val branches by branchViewModel.branchListState.collectAsState()

    // Get current collaborator ID
    LaunchedEffect(Unit) { authViewModel.getCurrentUser() }
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val collaboratorId = currentUserId ?: ""

    // State for managing dialog
    var showDialog by remember { mutableStateOf(false) }
    var editingBranch by remember { mutableStateOf<Branch?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load branches on first composition
    LaunchedEffect(collaboratorId) {
        if (collaboratorId.isNotEmpty()) {
            isLoading = true
            runCatching {
                branchViewModel.getBranchesByCollaborator(collaboratorId)
            }.onFailure { error ->
                errorMessage = "Error al cargar sucursales: ${error.message}"
            }.also {
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            BJTopHeader(
                title = "Gestión de Sucursales",
                nav = nav,
                showNotificationsIcon = false
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Loading indicator
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Teal)
                        }
                    }
                } else if (branches.isEmpty()) {
                    // Empty state
                    item {
                        EmptyStateCard {
                            editingBranch = Branch(
                                collaboratorId = collaboratorId,
                                state = BranchState.ACTIVE
                            )
                            showDialog = true
                        }
                    }
                } else {
                    // Branches list
                    items(branches) { branch ->
                        BranchItemCard(
                            branch = branch,
                            onEditClick = {
                                editingBranch = branch
                                showDialog = true
                            }
                        )
                    }

                    // Add new branch button
                    item {
                        AddBranchButton {
                            editingBranch = Branch(
                                collaboratorId = collaboratorId,
                                state = BranchState.ACTIVE
                            )
                            showDialog = true
                        }
                    }
                }

                // Error message
                errorMessage?.let { error ->
                    item {
                        ErrorCard(
                            message = error,
                            onDismiss = { errorMessage = null }
                        )
                    }
                }
            }

            // Version text at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Gestiona las ubicaciones de tu negocio",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Edit/Create Dialog
        if (showDialog && editingBranch != null) {
            EditSucursalDialog(
                branch = editingBranch!!,
                onDismiss = {
                    showDialog = false
                    editingBranch = null
                },
                onSave = { updatedBranch ->
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        runCatching {
                            if (updatedBranch.branchId == null) {
                                // Create new branch
                                val request = CreateBranchRequest(
                                    collaboratorId = collaboratorId,
                                    name = updatedBranch.name ?: "",
                                    phone = updatedBranch.phone ?: "",
                                    address = updatedBranch.address ?: "",
                                    zipCode = updatedBranch.zipCode ?: "",
                                    location = updatedBranch.location,
                                    jsonSchedule = updatedBranch.jsonSchedule,
                                    state = updatedBranch.state ?: BranchState.ACTIVE
                                )
                                branchViewModel.createBranch(request)
                            } else {
                                // Update existing branch
                                val updateRequest = UpdateBranchRequest(
                                    name = updatedBranch.name,
                                    phone = updatedBranch.phone,
                                    address = updatedBranch.address,
                                    zipCode = updatedBranch.zipCode,
                                    location = updatedBranch.location,
                                    jsonSchedule = updatedBranch.jsonSchedule,
                                    state = updatedBranch.state
                                )
                                branchViewModel.updateBranch(updatedBranch.branchId, updateRequest)
                            }
                            // Reload branches
                            branchViewModel.getBranchesByCollaborator(collaboratorId)
                        }.onSuccess {
                            showDialog = false
                            editingBranch = null
                        }.onFailure { error ->
                            errorMessage = "Error al guardar sucursal: ${error.message}"
                        }.also {
                            isLoading = false
                        }
                    }
                },
                onDelete = { branchToDelete ->
                    scope.launch {
                        if (branchToDelete.branchId != null) {
                            isLoading = true
                            errorMessage = null

                            runCatching {
                                branchViewModel.deleteBranch(branchToDelete.branchId)
                                // Reload branches
                                branchViewModel.getBranchesByCollaborator(collaboratorId)
                            }.onSuccess {
                                showDialog = false
                                editingBranch = null
                            }.onFailure { error ->
                                errorMessage = "Error al eliminar sucursal: ${error.message}"
                            }.also {
                                isLoading = false
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BranchItemCard(
    branch: Branch,
    onEditClick: () -> Unit
) {
    var showLocationDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
        color = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Branch info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = branch.name ?: "Sin nombre",
                    fontWeight = FontWeight.Bold,
                    color = TextColor,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = branch.address ?: "Sin dirección",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!branch.phone.isNullOrBlank()) {
                    Text(
                        text = branch.phone,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Status badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (branch.state == BranchState.ACTIVE) {
                        StatusBadge(
                            text = "Activa",
                            color = ActiveGreen
                        )
                    }
                    if (branch.location != null) {
                        LocationBadge(
                            onClick = { showLocationDialog = true }
                        )
                    }
                }
            }

            // Edit button
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    tint = Teal
                )
            }
        }
    }

    // Location viewer dialog
    if (showLocationDialog && branch.location != null) {
        ViewBranchLocationDialog(
            branch = branch,
            onDismiss = { showLocationDialog = false }
        )
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun LocationBadge(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2196F3).copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(14.dp)
            )
            Text(
                "Ver mapa",
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun EmptyStateCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Outlined.Store,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                "No hay sucursales registradas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextColor,
                textAlign = TextAlign.Center
            )
            Text(
                "Agrega la primera ubicación de tu negocio para comenzar",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(DarkBlue, Teal))),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            "Agregar Sucursal",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddBranchButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Teal, RoundedCornerShape(12.dp)),
        color = Teal.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Teal,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Agregar Nueva Sucursal",
                color = Teal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                color = Color(0xFFD32F2F),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Cerrar",
                    tint = Color(0xFFD32F2F)
                )
            }
        }
    }
}
