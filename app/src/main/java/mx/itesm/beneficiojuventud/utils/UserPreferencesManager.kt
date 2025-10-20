package mx.itesm.beneficiojuventud.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor de preferencias de usuario para almacenar credenciales de forma segura.
 * Utiliza EncryptedSharedPreferences para cifrar los datos sensibles.
 */
class UserPreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "user_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
    }

    /**
     * Guarda las credenciales del usuario de forma segura.
     * @param email Email del usuario
     * @param password Contraseña del usuario (será cifrada)
     */
    fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_REMEMBER_ME, true)
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, encodePassword(password))
            apply()
        }
    }

    /**
     * Obtiene las credenciales guardadas.
     * @return Pair con email y contraseña, o null si no hay credenciales guardadas
     */
    fun getCredentials(): Pair<String, String>? {
        if (!isRememberMeEnabled()) return null

        val email = sharedPreferences.getString(KEY_EMAIL, null)
        val encodedPassword = sharedPreferences.getString(KEY_PASSWORD, null)

        return if (email != null && encodedPassword != null) {
            Pair(email, decodePassword(encodedPassword))
        } else {
            null
        }
    }

    /**
     * Verifica si el usuario tiene habilitado "Recuérdame".
     */
    fun isRememberMeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    /**
     * Limpia las credenciales guardadas.
     */
    fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove(KEY_REMEMBER_ME)
            remove(KEY_EMAIL)
            remove(KEY_PASSWORD)
            apply()
        }
    }

    /**
     * Codifica la contraseña en Base64 para almacenamiento adicional.
     */
    private fun encodePassword(password: String): String {
        return Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)
    }

    /**
     * Decodifica la contraseña desde Base64.
     */
    private fun decodePassword(encoded: String): String {
        return String(Base64.decode(encoded, Base64.DEFAULT))
    }
}
