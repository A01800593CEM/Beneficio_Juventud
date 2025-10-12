package mx.itesm.beneficiojuventud.model

import com.google.gson.annotations.SerializedName

data class Branch(
    val collaboratorId: Int,
    val name: String,
    val address: String,
    val phone: String,
    val zipCode: String,
    val location: String?,
    val jsonSchedule: String?
)

enum class BranchState {
    @SerializedName("activa") activa,
    @SerializedName("inactiva") inactiva
}

