package mx.itesm.beneficiojuventud.model.users

data class UserProfile(
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val fechaNacimiento: String, // Formato ISO (YYYY-MM-DD) para base de datos
    val telefono: String,
    val email: String,
    val categorias: List<String> = emptyList(), // Para guardar las categorías seleccionadas del onboarding
    val profileImageKey: String? = null // Key de la imagen en S3
)

data class RegistrationData(
    val userProfile: UserProfile,
    val password: String
)