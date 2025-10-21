package mx.itesm.beneficiojuventud.model.collaborators

import com.google.gson.annotations.SerializedName
import mx.itesm.beneficiojuventud.model.Branch
import mx.itesm.beneficiojuventud.model.categories.Category

/**
 * Modelo de colaborador con información de distancia y sucursal más cercana
 */
data class NearbyCollaborator(
    val cognitoId: String? = null,
    val businessName: String? = null,
    val logoUrl: String? = null,
    val description: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val categories: List<Category>? = null,

    // Campos específicos de ubicación
    val distance: Double? = null, // Distancia en kilómetros a la sucursal más cercana
    val closestBranch: Branch? = null, // Sucursal más cercana
    val totalBranches: Int? = null // Total de sucursales del colaborador
) {
    /**
     * Formatea la distancia para mostrar en la UI
     * Ej: "0.5 km", "1.2 km", "500 m"
     */
    fun getFormattedDistance(): String {
        return when {
            distance == null -> ""
            distance < 1.0 -> "${(distance * 1000).toInt()} m"
            else -> String.format("%.1f km", distance)
        }
    }

    /**
     * Convierte a Collaborator regular (sin información de ubicación)
     */
    fun toCollaborator(): Collaborator {
        return Collaborator(
            cognitoId = cognitoId,
            businessName = businessName,
            logoUrl = logoUrl,
            description = description,
            phone = phone,
            email = email,
            address = closestBranch?.address,
            postalCode = closestBranch?.zipCode,
            categories = categories
        )
    }
}
