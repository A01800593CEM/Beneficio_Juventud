package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun BJSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar comercios, cupones…",
    onSearch: () -> Unit = {},
    onClear: () -> Unit = { onQueryChange("") }
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Buscar",
                tint = Color(0xFF575757),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Borrar texto",
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                color = Color(0xFFAFAFAF),
                fontWeight = FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp), // evita recorte del placeholder
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color(0xFFBEBEBE),
            unfocusedBorderColor = Color(0xFFE2E2E2),
            disabledBorderColor  = Color(0xFFE2E2E2),
            cursorColor          = Color(0xFF008D96),
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}
