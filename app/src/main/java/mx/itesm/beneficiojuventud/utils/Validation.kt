package mx.itesm.beneficiojuventud.utils

/**
 * Valida si un email tiene un formato válido
 * @param email El email a validar
 * @return true si el email tiene un formato válido, false en caso contrario
 */
fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9][A-Za-z0-9+_.-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    return email.isNotBlank() && emailRegex.matches(email)
}

/**
 * Obtiene el mensaje de error para un email inválido
 * @return El mensaje de error
 */
fun getEmailErrorMessage(): String {
    return "El correo no es válido"
}
