package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "************"
) {
    var visible by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TextFieldDefaults.MinHeight),
        shape = shape,
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                if (visible)
                    Icon(Icons.Outlined.VisibilityOff, contentDescription = "Ocultar contraseña")
                else
                    Icon(Icons.Outlined.Visibility, contentDescription = "Mostrar contraseña")
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            )
        },
        // ← misma tipografía/lineHeight que el Email para evitar cortes en letras como 'g', 'p', etc.
        textStyle = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color(0xFF2F2F2F)
        ),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
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
            unfocusedLeadingIconColor = Color(0xFF7D7A7A),
            focusedTrailingIconColor = Color(0xFF7D7A7A),
            unfocusedTrailingIconColor = Color(0xFF7D7A7A),
        )
    )
}

