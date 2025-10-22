package mx.itesm.beneficiojuventud.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Gestiona verificación y solicitud de permisos en tiempo de ejecución.
 * @param context Contexto de la aplicación para verificar permisos del sistema
 */
class PermissionManager(private val context: Context) {

    /**
     * Verifica si el permiso de notificaciones push está concedido.
     * En Android 12 e inferiores, siempre retorna true (no se requiere en tiempo de ejecución).
     * En Android 13+, verifica si POST_NOTIFICATIONS está concedido.
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No se requiere en versiones anteriores
        }
    }

    /**
     * Verifica si el permiso de ubicación está concedido.
     * Retorna true si al menos uno de los permisos de ubicación (FINE o COARSE) está concedido.
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtiene el permiso de notificaciones para solicitar en tiempo de ejecución.
     * Retorna null si la versión de Android no lo requiere.
     */
    fun getNotificationPermission(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
    }

    /**
     * Obtiene los permisos de ubicación para solicitar en tiempo de ejecución.
     */
    fun getLocationPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
