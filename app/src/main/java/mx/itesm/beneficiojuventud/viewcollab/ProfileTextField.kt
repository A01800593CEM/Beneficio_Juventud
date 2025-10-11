package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LabelColor = Color(0xFFAEAEAE)
private val ValueColor = Color(0xFF616161)
private val IconColor = Color(0xFFBDBDBD)
private val BorderColor = Color(0xFFE0E0E0)

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    maxLines: Int = 1
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        maxLines = maxLines,
        textStyle = TextStyle(color = ValueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold),
        cursorBrush = SolidColor(ValueColor),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = leadingIcon, contentDescription = label, tint = IconColor, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = label, color = LabelColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    innerTextField()
                }
            }
        }
    )
}