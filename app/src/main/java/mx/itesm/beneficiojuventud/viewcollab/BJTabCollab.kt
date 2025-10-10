package mx.itesm.beneficiojuventud.viewcollab

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BJTabCollab(val route: String, val icon: ImageVector, val label: String) {
    data object Menu : BJTabCollab("collab_menu", Icons.Default.Home, "Home")
    data object Stats : BJTabCollab("collab_stats", Icons.Outlined.BarChart, "Estadísticas")
    data object Promotions : BJTabCollab("collab_promos", Icons.Outlined.LocalOffer, "Promociones")
    data object Profile : BJTabCollab("collab_profile", Icons.Default.Person, "Perfil")
}