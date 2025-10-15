package mx.itesm.beneficiojuventud.model

enum class PromoTheme { LIGHT, DARK }

data class Promo(
    val bg: Int,
    val title: String,
    val subtitle: String,
    val body: String,
    val theme: PromoTheme = PromoTheme.LIGHT
)
