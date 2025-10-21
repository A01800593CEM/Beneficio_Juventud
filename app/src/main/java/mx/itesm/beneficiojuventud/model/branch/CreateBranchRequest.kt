package mx.itesm.beneficiojuventud.model.branch

import mx.itesm.beneficiojuventud.model.BranchState

/**
 * DTO para crear una nueva sucursal
 */
data class CreateBranchRequest(
    val collaboratorId: String,
    val name: String,
    val phone: String,
    val address: String,
    val zipCode: String,
    val location: String? = null,
    val jsonSchedule: Any? = null,
    val state: BranchState = BranchState.ACTIVE
)
