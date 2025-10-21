package mx.itesm.beneficiojuventud.model.branch

import mx.itesm.beneficiojuventud.model.BranchState

/**
 * DTO para actualizar una sucursal existente
 */
data class UpdateBranchRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val zipCode: String? = null,
    val location: String? = null,
    val jsonSchedule: Any? = null,
    val state: BranchState? = null
)
