package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
        // ⬇️ Quita height(48.dp) y usa altura mínima segura
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TextFieldDefaults.MinHeight), // ~56.dp
        shape = shape,
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            )
        },
        // ⬇️ Un pelín más de lineHeight evita cortes en algunas fuentes/escalas
        textStyle = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color(0xFF2F2F2F)
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFFD3D3D3),
            unfocusedIndicatorColor = Color(0xFFD3D3D3),
            cursorColor = Color(0xFF008D96),
            focusedPlaceholderColor = Color(0xFF7D7A7A),
            unfocusedPlaceholderColor = Color(0xFF7D7A7A),
            focusedLeadingIconColor = Color(0xFF7D7A7A),
            unfocusedLeadingIconColor = Color(0xFF7D7A7A)
        )
    )
}

