package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.background
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
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

/**
 * Datos de una predicción de dirección
 */
data class AddressPrediction(
    val mainText: String,
    val secondaryText: String,
    val fullText: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

/**
 * TextField con autocompletado de direcciones usando Google Maps SDK
 * Muestra sugerencias mientras el usuario escribe (implementado con simulación)
 *
 * NOTA: Este componente usa SOLO Google Maps SDK for Android.
 * No requiere Places API. El autocompletado se simula mostrando direcciones
 * sugeridas basadas en coincidencias de texto.
 *
 * @param value Valor actual del campo
 * @param onValueChange Callback cuando cambia el valor
 * @param onAddressSelected Callback cuando se selecciona una dirección
 * @param modifier Modificador de Compose
 * @param label Etiqueta del campo
 * @param placeholder Texto placeholder
 * @param country Código de país para filtrar (ej: "MX", "US", "FR")
 */
@Composable
fun AddressAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddressSelected: (address: String) -> Unit = {},
    modifier: Modifier = Modifier,
    label: String = "Dirección",
    placeholder: String = "Escribe tu dirección...",
    country: String = "MX"
) {
    var suggestions by rememberSaveable { mutableStateOf<List<AddressPrediction>>(emptyList()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var showSuggestions by rememberSaveable { mutableStateOf(false) }
    var debounceJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val scope = rememberCoroutineScope()

    // Datos de ejemplo para sugerencias (esto podría conectarse a un API backend)
    val commonAddresses = listOf(
        AddressPrediction(
            mainText = "Av. Reforma",
            secondaryText = "México, CDMX",
            fullText = "Av. Reforma, Cuauhtémoc, 06500 CDMX, México",
            latitude = 19.4263,
            longitude = -99.1452
        ),
        AddressPrediction(
            mainText = "Paseo de la Reforma",
            secondaryText = "Benito Juárez, CDMX",
            fullText = "Paseo de la Reforma 505, Benito Juárez, 06596 CDMX, México",
            latitude = 19.4273,
            longitude = -99.1562
        ),
        AddressPrediction(
            mainText = "Av. Paseo de la Reforma",
            secondaryText = "Polanco, CDMX",
            fullText = "Av. Paseo de la Reforma 800, Polanco, 11560 CDMX, México",
            latitude = 19.4343,
            longitude = -99.1873
        ),
        AddressPrediction(
            mainText = "Calle de la Paz",
            secondaryText = "Centro, CDMX",
            fullText = "Calle de la Paz 200, Centro, 06010 CDMX, México",
            latitude = 19.4353,
            longitude = -99.1345
        ),
        AddressPrediction(
            mainText = "Av. Revolución",
            secondaryText = "San Ángel, CDMX",
            fullText = "Av. Revolución 1500, San Ángel, 01000 CDMX, México",
            latitude = 19.3604,
            longitude = -99.1873
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // TextField con autocompletado
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)

                // Cancelar búsqueda anterior
                debounceJob?.cancel()

                if (newValue.length >= 2) {
                    showSuggestions = true
                    isLoading = true

                    // Debounce: esperar 300ms después de dejar de escribir
                    debounceJob = scope.launch {
                        kotlinx.coroutines.delay(300)
                        try {
                            // Filtrar direcciones que coincidan con el texto ingresado
                            suggestions = commonAddresses.filter { address ->
                                address.mainText.contains(newValue, ignoreCase = true) ||
                                address.secondaryText.contains(newValue, ignoreCase = true) ||
                                address.fullText.contains(newValue, ignoreCase = true)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            suggestions = emptyList()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    suggestions = emptyList()
                    showSuggestions = false
                    isLoading = false
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

        // Mostrar sugerencias en dropdown
        if (showSuggestions && suggestions.isNotEmpty()) {
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
                    items(suggestions) { suggestion ->
                        AddressSuggestionItem(
                            suggestion = suggestion,
                            onSelect = { selectedAddress ->
                                onValueChange(selectedAddress)
                                onAddressSelected(selectedAddress)
                                showSuggestions = false
                                suggestions = emptyList()
                            }
                        )
                    }
                }
            }
        }

        // Mostrar indicador de carga
        if (isLoading && suggestions.isEmpty()) {
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
    }
}

/**
 * Item individual de una sugerencia de dirección
 */
@Composable
private fun AddressSuggestionItem(
    suggestion: AddressPrediction,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect(suggestion.fullText)
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                suggestion.mainText,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = Color(0xFF2F2F2F)
                )
            )
            if (suggestion.secondaryText.isNotEmpty()) {
                Text(
                    suggestion.secondaryText,
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
