package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.itesm.beneficiojuventud.viewmodel.PlacesAutocompleteViewModel
import mx.itesm.beneficiojuventud.utils.PlacesPrediction

/**
 * TextField con autocompletado de direcciones usando Google Places SDK
 * Proporciona sugerencias en tiempo real mientras el usuario escribe
 *
 * CARACTERÍSTICAS:
 * - Usa Google Places Autocomplete SDK nativo de Android
 * - No requiere backend o billing del servidor
 * - Incluye debounce automático para optimizar requests
 * - Manejo de sesiones para reducir costos
 * - Integración con coroutines y StateFlow
 *
 * @param value Valor actual del campo
 * @param onValueChange Callback cuando cambia el valor
 * @param onAddressSelected Callback cuando se selecciona una dirección (devuelve dirección formateada)
 * @param modifier Modificador de Compose
 * @param label Etiqueta del campo
 * @param placeholder Texto placeholder
 * @param country Código de país para filtrado (ej: "MX", "US")
 * @param viewModel ViewModel de Places (se crea automáticamente si no se proporciona)
 */
@Composable
fun AddressAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddressSelected: (address: String) -> Unit = {},
    modifier: Modifier = Modifier,
    label: String = "Dirección",
    placeholder: String = "Escribe tu dirección...",
    country: String = "MX",
    viewModel: PlacesAutocompleteViewModel = viewModel()
) {
    // Recolectar estado del ViewModel
    val state by viewModel.state.collectAsState()
    var showSuggestions by rememberSaveable { mutableStateOf(false) }
    var previousQuery by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        // TextField con autocompletado
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                previousQuery = newValue

                // Mostrar sugerencias si hay texto suficiente
                if (newValue.length >= 2) {
                    showSuggestions = true
                    viewModel.searchAddresses(newValue, country)
                } else {
                    showSuggestions = false
                    viewModel.clearSearch()
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TextFieldDefaults.MinHeight),
            shape = RoundedCornerShape(18.dp),
            leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
            placeholder = { Text(placeholder, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFD3D3D3),
                unfocusedIndicatorColor = Color(0xFFD3D3D3),
                cursorColor = Color(0xFF008D96),
                focusedLeadingIconColor = Color(0xFF7D7A7A),
                unfocusedLeadingIconColor = Color(0xFF7D7A7A),
            )
        )

        // Mostrar error si existe
        if (state.error != null) {
            Text(
                text = "Error: ${state.error}",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Mostrar sugerencias en dropdown
        if (showSuggestions && state.predictions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    items(state.predictions) { prediction ->
                        PlacesSuggestionItem(
                            prediction = prediction,
                            onSelect = { selectedPlaceId ->
                                // Obtener detalles del lugar seleccionado
                                viewModel.selectAddress(selectedPlaceId)
                                showSuggestions = false

                                // Usar la dirección formateada desde la predicción
                                onValueChange(prediction.fullText)
                                onAddressSelected(prediction.fullText)
                            }
                        )
                    }
                }
            }
        }

        // Mostrar indicador de carga
        if (state.isLoading && state.predictions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF008D96)
                )
            }
        }

        // Mostrar "sin resultados" si no hay predicciones
        if (showSuggestions && !state.isLoading && state.predictions.isEmpty() && previousQuery.length >= 2) {
            Text(
                text = "No se encontraron direcciones",
                color = Color(0xFF999999),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Item individual de una sugerencia de dirección desde Google Places
 */
@Composable
private fun PlacesSuggestionItem(
    prediction: PlacesPrediction,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect(prediction.placeId)
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                prediction.mainText,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = Color(0xFF2F2F2F)
                )
            )
            if (prediction.secondaryText.isNotEmpty()) {
                Text(
                    prediction.secondaryText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color(0xFFE0E0E0),
        thickness = 0.5.dp
    )
}
