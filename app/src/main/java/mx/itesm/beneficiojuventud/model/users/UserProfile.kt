package mx.itesm.beneficiojuventud.model.users

import com.google.gson.annotations.SerializedName

data class UserProfile(

    val id: Int? = null,
    val cognitoId: String? = null,
    val name: String? = null,
    val lastNamePaternal: String? = null,
    val lastNameMaternal: String? = null,
    val birthDate: String? = null, // Formato ISO (YYYY-MM-DD) para base de datos
    val phoneNumber: String? = null,
    val email: String? = null,
    val accountState: AccountState? = null,
    val registrationDate: String? = null,
    val updatedAt: String? = null,
    val notificationToken: String? = null,
    val profileImageKey: String? = null // Key de la imagen en S3
)

enum class AccountState {
    @SerializedName("activo") activo,
    @SerializedName("inactivo") inactivo,
    @SerializedName("suspendido") suspendido
}