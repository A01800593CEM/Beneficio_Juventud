package mx.itesm.beneficiojuventud.model.collaborators

import com.google.gson.annotations.SerializedName

data class Collaborator(
    val businessName: String,
    val rfc: String,
    val representativeName: String,
    val phone: String,
    val email: String,
    val address: String,
    val postalCode: String,
    val categoryIds: Int,
    val logoUrl: String?,
    val description: String?,
    val state: CollaboratorsState
)

enum class CollaboratorsState{
    @SerializedName("activo") activo,
    @SerializedName("inactivo") inactivo,
    @SerializedName("suspendido") suspendido
}