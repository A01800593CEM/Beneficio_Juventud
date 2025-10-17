package mx.itesm.beneficiojuventud.model.collaborators

import com.google.gson.annotations.SerializedName
import mx.itesm.beneficiojuventud.model.categories.Category

data class Collaborator(
    val cognitoId: String? = null,
    val businessName: String? = null,
    val rfc: String? = null,
    val representativeName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val postalCode: String? = null,
    val logoUrl: String? = null,
    val description: String? = null,
    val registrationDate: String? = null,
    val state: CollaboratorsState? = null,
    val categories: List<Category>? = null,
)

enum class CollaboratorsState{
    @SerializedName("activo") activo,
    @SerializedName("inactivo") inactivo,
    @SerializedName("suspendido") suspendido
}