package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
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
fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "beneficio_user@juventud.com"
) {
    val shape = RoundedCornerShape(18.dp)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = shape,
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Start
            )
        },
        textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF2F2F2F)),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        colors = TextFieldDefaults.colors(
            // Fondo
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            // Borde (indicator en M3)
            focusedIndicatorColor = Color(0xFFE0E0E0),
            unfocusedIndicatorColor = Color(0xFFE0E0E0),
            // Cursor
            cursorColor = Color(0xFF008D96),
            // Placeholder
            focusedPlaceholderColor = Color(0xFF7D7A7A),
            unfocusedPlaceholderColor = Color(0xFF7D7A7A),
            // Icono
            focusedLeadingIconColor = Color(0xFF7D7A7A),
            unfocusedLeadingIconColor = Color(0xFF7D7A7A)
        )
    )
}
