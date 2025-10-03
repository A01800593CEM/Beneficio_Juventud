package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.max
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.BoxWithConstraints

@Composable
fun CodeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true,
    isError: Boolean = false,
    onFilled: ((String) -> Unit)? = null,
    // límites responsivos
    minBoxSize: Dp = 40.dp,
    maxBoxSize: Dp = 56.dp,
    boxCorner: Dp = 12.dp,
    boxSpacing: Dp = 4.dp,
    emptyBorderColor: Color = Color(0xFFE3E3E3),
    filledBorderColor: Color = Color(0xFF008D96),
    errorBorderColor: Color = Color(0xFFD32F2F),
) {
    var tf by remember { mutableStateOf(TextFieldValue(value)) }

    // Mantener cursor al final si el texto externo cambia
    LaunchedEffect(value) {
        if (tf.text != value) {
            tf = tf.copy(text = value, selection = TextRange(value.length))
        }
    }

    val shape = RoundedCornerShape(boxCorner)

    BasicTextField(
        value = tf,
        onValueChange = { new ->
            if (!enabled) return@BasicTextField
            val filtered = new.text.filter { it.isDigit() }.take(length)
            tf = new.copy(text = filtered, selection = TextRange(filtered.length))
            onValueChange(filtered)
            if (filtered.length == length && filtered != value) {
                onFilled?.invoke(filtered)
            }
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        modifier = modifier,
        decorationBox = { inner ->
            // Campo invisible para conservar foco/teclado
            Box(Modifier.size(0.dp)) { inner() }

            // Layout responsivo
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Ancho disponible
                val availableWidth = maxWidth
                val totalSpacing = boxSpacing * (length - 1)

                // tamaño calculado = (ancho - espacios) / casillas, acotado entre min y max
                var calcBox = (availableWidth - totalSpacing) / length
                calcBox = max(minBoxSize, min(calcBox, maxBoxSize))

                // ancho real del grupo (para centrarlo)
                val groupWidth = calcBox * length + boxSpacing * (length - 1)

                // Tamaño de letra proporcional a la caja
                val fontSize = (calcBox.value * 0.40f).sp

                Row(
                    modifier = Modifier.width(groupWidth),
                    horizontalArrangement = Arrangement.spacedBy(boxSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(length) { i ->
                        val char = value.getOrNull(i)?.toString() ?: ""
                        val borderColor = when {
                            isError -> errorBorderColor
                            char.isEmpty() -> emptyBorderColor
                            else -> filledBorderColor
                        }

                        Box(
                            modifier = Modifier
                                .size(calcBox)
                                .border(width = 2.dp, color = borderColor, shape = shape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = TextStyle(
                                    fontSize = fontSize,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    )
}
