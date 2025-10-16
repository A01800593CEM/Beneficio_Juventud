package mx.itesm.beneficiojuventud.model.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import mx.itesm.beneficiojuventud.R
import mx.itesm.beneficiojuventud.view.MainActivity
import kotlin.random.Random

class NotificationsService: FirebaseMessagingService() {

    /**
     * Se ejecuta cuando se genera un nuevo token FCM
     * Este token se debe enviar al servidor para identificar este dispositivo
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Token de este dispositivo: $token")
        // TODO: Enviar el token al servidor para actualizarlo en la BD
    }

    /**
     * Se ejecuta cuando llega una notificación push del servidor
     * Procesa los datos y muestra la notificación al usuario
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        println("Llegó la notificación TITULO: ${message.notification?.title}")
        println("Llegó la notificación BODY: ${message.notification?.body}")

        // Debug: Mostrar todos los datos disponibles
        println("Datos de la notificación: ${message.data}")

        // Acceder a datos específicos de forma segura
        // Los datos disponibles desde el servidor son:
        // - promotion: ID de la promoción
        // - action: acción a realizar (openPromoDetail, openNotification)
        // - collaborator: ID del colaborador (solo en notificaciones de favoritos)
        // - notificationId: ID de la notificación (solo en notificaciones de expiración)
        message.data?.let { data ->
            data["promotion"]?.let { promoId ->
                println("Promoción ID: $promoId")
            }
            data["action"]?.let { action ->
                println("Acción: $action")
            }
            data["collaborator"]?.let { collaborator ->
                println("Colaborador: $collaborator")
            }
            data["notificationId"]?.let { notifId ->
                println("Notificación ID: $notifId")
            }
        }

        message.notification?.let {
            enviarNotificacion(it, message.data)
        }
    }

    /**
     * Crea y muestra la notificación en la barra de notificaciones
     * @param message La notificación recibida con título y mensaje
     * @param data Los datos adicionales de la notificación (promoción, acción, etc.)
     */
    private fun enviarNotificacion(message: RemoteMessage.Notification, data: Map<String, String>?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_CLEAR_TOP)

            // Agregar datos de la notificación al intent para procesamiento posterior
            data?.let { notificationData ->
                notificationData["promotion"]?.let { promoId ->
                    putExtra("promotion_id", promoId)
                }
                notificationData["action"]?.let { action ->
                    putExtra("notification_action", action)
                }
                notificationData["collaborator"]?.let { collaborator ->
                    putExtra("collaborator_id", collaborator)
                }
                notificationData["notificationId"]?.let { notifId ->
                    putExtra("notification_id", notifId)
                }
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, FLAG_IMMUTABLE
        )
        val channelId = this.getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.logo_beneficio_joven) //nombre de la imagen en el drawable
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        manager.notify(Random.nextInt(), notificationBuilder.build())
    }
    companion object {
        const val CHANNEL_NAME = "FCM notification channel"
    }
}