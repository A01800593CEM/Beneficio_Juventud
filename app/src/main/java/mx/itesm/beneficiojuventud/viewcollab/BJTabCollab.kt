package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BJTabCollab(val route: String, val label: String, val icon: ImageVector) {
    data object Menu : BJTabCollab("collab_menu", "Menú", Icons.Outlined.Home)
    data object Stats : BJTabCollab("collab_stats", "Stats", Icons.Outlined.Equalizer)
    data object Promotions : BJTabCollab("collab_promos", "Promos", Icons.Outlined.Discount)
    data object Profile : BJTabCollab("collab_profile", "Perfil", Icons.Outlined.Person)
}