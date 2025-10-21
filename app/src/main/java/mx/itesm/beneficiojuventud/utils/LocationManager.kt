package mx.itesm.beneficiojuventud.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Gestiona la obtención de la ubicación del usuario usando Google Play Services Location
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Verifica si la app tiene permisos de ubicación
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtiene la última ubicación conocida del usuario (más rápido)
     * @return UserLocation o null si no hay ubicación disponible
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): UserLocation? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (continuation.isActive) {
                        continuation.resume(location?.toUserLocation())
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }
    }

    /**
     * Obtiene la ubicación actual del usuario (más preciso pero más lento)
     * @return UserLocation o null si no hay ubicación disponible
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): UserLocation? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            val request = CurrentLocationRequest.Builder()
                .setDurationMillis(10000) // Timeout de 10 segundos
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationClient.getCurrentLocation(request, null)
                .addOnSuccessListener { location: Location? ->
                    if (continuation.isActive) {
                        continuation.resume(location?.toUserLocation())
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }
    }

    /**
     * Observa cambios en la ubicación del usuario en tiempo real
     * @param intervalMillis Intervalo en milisegundos entre actualizaciones (por defecto 10 segundos)
     * @return Flow que emite UserLocation cada vez que cambia la ubicación
     */
    @SuppressLint("MissingPermission")
    fun observeLocation(intervalMillis: Long = 10000L): Flow<UserLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMillis
        ).apply {
            setMinUpdateIntervalMillis(intervalMillis / 2)
            setWaitForAccurateLocation(false)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toUserLocation())
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Calcula la distancia entre dos ubicaciones en kilómetros
     */
    fun calculateDistance(from: UserLocation, to: UserLocation): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude,
            from.longitude,
            to.latitude,
            to.longitude,
            results
        )
        return (results[0] / 1000.0) // Convertir metros a kilómetros
    }
}

/**
 * Modelo de datos para la ubicación del usuario
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null, // Precisión en metros
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Extensión para convertir Location de Android a UserLocation
 */
private fun Location.toUserLocation(): UserLocation {
    return UserLocation(
        latitude = this.latitude,
        longitude = this.longitude,
        accuracy = this.accuracy,
        timestamp = this.time
    )
}
