package mx.itesm.beneficiojuventud.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun BackButton(
    nav: NavHostController,
    destination: String? = null
) {
    IconButton(
        onClick = {
            if (destination != null) {
                nav.navigate(destination)
            } else {
                nav.popBackStack()
            }
        },
        modifier = Modifier.padding(10.dp, 16.dp ,0.dp,0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBackIosNew,
            contentDescription = "Regresar",
            tint = Color(0xFF616161),
        )
    }
}


