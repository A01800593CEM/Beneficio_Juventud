package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.itesm.beneficiojuventud.viewcollab.Branch

private val ValueColor = Color(0xFF616161)
private val BorderColor = Color(0xFFE0E0E0)
private val ActiveGreen = Color(0xFF4CAF50)
private val DetailsColor = Color(0xFF969696)


data class Branch(
    val id: Long,
    val name: String,
    val address: String,
    val phone: String,
    val isActive: Boolean
)

data class SucursalUiState(
    val isLoading: Boolean = false,
    val branches: List<Branch> = emptyList(),
    val error: String? = null
)

class SucursalViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        SucursalUiState(
            isLoading = false,
            branches = listOf(
                Branch(1, "Sucursal Centro", "Av. Reforma 123, CDMX", "5512345678", true),
                Branch(2, "Sucursal Norte", "Calz. Vallejo 456, CDMX", "5598765432", false)
            )
        )
    )
    val uiState: StateFlow<SucursalUiState> = _uiState.asStateFlow()

    fun refresh() = viewModelScope.launch {
        // TODO: Reemplazar con llamada real a tu backend
        _uiState.emit(_uiState.value.copy(isLoading = false))
    }

    fun toggleActive(id: Long) = viewModelScope.launch {
        val updated = _uiState.value.branches.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        _uiState.emit(_uiState.value.copy(branches = updated))
    }

    fun upsert(branch: Branch) = viewModelScope.launch {
        val existing = _uiState.value.branches.toMutableList()
        val index = existing.indexOfFirst { it.id == branch.id }
        if (index >= 0) existing[index] = branch else existing.add(branch)
        _uiState.emit(_uiState.value.copy(branches = existing))
    }

    fun remove(id: Long) = viewModelScope.launch {
        val updated = _uiState.value.branches.filterNot { it.id == id }
        _uiState.emit(_uiState.value.copy(branches = updated))
    }
}


@Composable
fun SucursalCard(
    branch: Branch,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = branch.name,
                fontWeight = FontWeight.Black,
                color = ValueColor,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = branch.address,
                color = DetailsColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = branch.phone,
                color = DetailsColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (branch.isActive) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ActiveGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Activa",
                        color = ActiveGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Editar Sucursal",
                tint = ValueColor
            )
        }
    }
}