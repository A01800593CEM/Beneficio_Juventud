package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LabelColor = Color(0xFFAEAEAE)
private val ValueColor = Color(0xFF2F2F2F)
private val IconColor = Color(0xFF7D7A7A)
private val BorderColor = Color(0xFFD3D3D3)
private val BackgroundColor = Color.White

@Composable
fun ProfileDropdownField(
    value: String,
    label: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(LocalContentColor provides Color(0xFF008D96)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .background(BackgroundColor, RoundedCornerShape(18.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(18.dp))
                .clickable(
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication = ripple()
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = label,
            tint = IconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                color = LabelColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                color = ValueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = IconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}