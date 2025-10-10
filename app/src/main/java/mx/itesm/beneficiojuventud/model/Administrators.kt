package mx.itesm.beneficiojuventud.model

import com.google.gson.annotations.SerializedName

data class Administrator(
    val firstName: String,
    val lastNameFather: String,
    val lastNameMother: String?, // opcional
    val email: String,
    val phone: String?,          // opcional
    val role: AdminRole,
    val status: AdminState
)

enum class AdminRole {
    @SerializedName("admin") admin,
    @SerializedName("superadmin") superadmin
}

enum class AdminState {
    @SerializedName("activo") activo,
    @SerializedName("inactivo") inactivo,
    @SerializedName("suspendido") suspendido
}
