package mx.itesm.beneficiojuventud.model


data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val needsConfirmation: Boolean = false
)
