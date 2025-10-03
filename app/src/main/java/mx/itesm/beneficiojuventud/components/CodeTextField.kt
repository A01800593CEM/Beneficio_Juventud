package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "123456"
) {
    val shape = RoundedCornerShape(18.dp)

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Solo permitir números y máximo 6 caracteres
            if (newValue.all { it.isDigit() } && newValue.length <= 6) {
                onValueChange(newValue)
            }
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = shape,
        leadingIcon = { Icon(Icons.Outlined.Sms, contentDescription = null) },
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Start
            )
        },
        textStyle = TextStyle(
            fontSize = 18.sp,
            color = Color(0xFF2F2F2F),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            letterSpacing = 4.sp // Espaciado entre números para mejor legibilidad
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        colors = TextFieldDefaults.colors(
            // Fondo
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            // Borde (indicator en M3)
            focusedIndicatorColor = Color(0xFF008D96),
            unfocusedIndicatorColor = Color(0xFFE0E0E0),
            // Cursor
            cursorColor = Color(0xFF008D96),
            // Placeholder
            focusedPlaceholderColor = Color(0xFF7D7A7A),
            unfocusedPlaceholderColor = Color(0xFF7D7A7A),
            // Icono
            focusedLeadingIconColor = Color(0xFF008D96),
            unfocusedLeadingIconColor = Color(0xFF7D7A7A)
        )
    )
}