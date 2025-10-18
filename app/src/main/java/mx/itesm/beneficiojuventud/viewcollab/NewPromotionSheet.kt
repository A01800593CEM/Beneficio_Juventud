package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.itesm.beneficiojuventud.viewcollab.NewPromotionViewModel

private val TextGrey = Color(0xFF616161)
private val LightGrey = Color(0xFFF5F5F5)
private val DarkBlue = Color(0xFF4B4C7E)
private val Teal = Color(0xFF008D96)
private val Purple = Color(0xFF6200EE)

@Composable
fun NewPromotionSheet(
    onClose: () -> Unit,
    viewModel: NewPromotionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
            }
            Text("Nueva Promoción", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                FormTextField(value = uiState.title, onValueChange = viewModel::onTitleChange, label = "Título de la Promoción*", placeholder = "Ej. Martes 2x1")
                Spacer(Modifier.height(16.dp))
                FormTextField(value = uiState.description, onValueChange = viewModel::onDescriptionChange, label = "Descripción*", placeholder = "Describe los detalles de la promoción", singleLine = false)
                Spacer(Modifier.height(16.dp))
                Row {
                    DatePickerField(label = "Fecha Inicio*", value = "10/11/2025", modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    DatePickerField(label = "Fecha Final*", value = "10/11/2025", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Row {
                    FormTextField(value = uiState.totalStock, onValueChange = {}, label = "Stock Total*", placeholder = "Ej. 100", modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    FormTextField(value = uiState.maxPerUser, onValueChange = {}, label = "Max x Usuario*", placeholder = "Ej. 2", modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Text("Sucursales donde aplica", fontWeight = FontWeight.Bold, color = TextGrey)
                Spacer(Modifier.height(8.dp))
            }

            items(uiState.branches) { branchItem ->
                BranchCheckboxItem(item = branchItem, onCheckedChange = {})
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Imagen de la promoción", fontWeight = FontWeight.Bold, color = TextGrey)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(LightGrey, RoundedCornerShape(12.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Text("Sin Imagen", color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    GradientButton(text = "Generar con IA", brush = Brush.horizontalGradient(listOf(Purple, DarkBlue)), modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    GradientButton(text = "Subir Imagen", brush = Brush.horizontalGradient(listOf(DarkBlue, Teal)), modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        GradientButton(text = "Crear Nueva Promoción", brush = Brush.horizontalGradient(listOf(DarkBlue, Teal)))
    }
}


@Composable
private fun FormTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, modifier: Modifier = Modifier, singleLine: Boolean = true) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextGrey)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine
        )
    }
}

@Composable
private fun DatePickerField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextGrey)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun BranchCheckboxItem(item: SelectableBranch, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = item.isSelected, onCheckedChange = onCheckedChange)
        Column {
            Text(item.branch.name, fontWeight = FontWeight.Bold)
            Text(item.branch.address, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun GradientButton(text: String, brush: Brush, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush), contentAlignment = Alignment.Center) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}