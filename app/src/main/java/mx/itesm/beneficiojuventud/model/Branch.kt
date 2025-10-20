package mx.itesm.beneficiojuventud.model

import com.google.gson.annotations.SerializedName

data class Branch(
    val branchId: Int? = null,
    val collaboratorId: String? = null,
    val name: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val zipCode: String? = null,
    val location: String? = null,
    val jsonSchedule: Any? = null,
    val state: BranchState? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

enum class BranchState {
    @SerializedName("activa") ACTIVE,
    @SerializedName("inactiva") INACTIVE
}

