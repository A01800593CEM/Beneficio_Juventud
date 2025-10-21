package mx.itesm.beneficiojuventud.model.redeemedcoupon

import mx.itesm.beneficiojuventud.model.promos.Promotions

data class RedeemedCoupon(
    val usedId: Int? = null,
    val promotionId: Int? = null,
    val userId: String? = null,
    val branchId: Int? = null,
    val promotion: Promotions? = null,
    val usedAt: String? = null,
    val nonce: String? = null,
    val qrTimestamp: Long? = null
)
