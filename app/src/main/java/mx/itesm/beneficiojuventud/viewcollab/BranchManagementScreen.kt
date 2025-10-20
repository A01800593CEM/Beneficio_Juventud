package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.model.Branch
import mx.itesm.beneficiojuventud.model.BranchState
import mx.itesm.beneficiojuventud.model.branch.CreateBranchRequest
import mx.itesm.beneficiojuventud.model.branch.UpdateBranchRequest
import mx.itesm.beneficiojuventud.viewmodel.AuthViewModel
import mx.itesm.beneficiojuventud.viewmodel.BranchViewModel

private val Teal = Color(0xFF008D96)
private val DarkBlue = Color(0xFF4B4C7E)
private val TextColor = Color(0xFF616161)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchManagementScreen(
    onBack: () -> Unit,
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gestión de Sucursales",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBranch = Branch(
                        collaboratorId = collaboratorId,
                        state = BranchState.ACTIVE
                    )
                    showDialog = true
                },
                containerColor = Teal
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar Sucursal",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFC62828),
                        fontSize = 14.sp
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Teal)
                }
            } else if (branches.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "No hay sucursales registradas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColor
                        )
                        Text(
                            "Presiona + para agregar tu primera sucursal",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Branches list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(branches) { branch ->
                        SucursalCard(
                            branch = branch,
                            onEditClick = {
                                editingBranch = branch
                                showDialog = true
                            }
                        )
                    }
                }
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
