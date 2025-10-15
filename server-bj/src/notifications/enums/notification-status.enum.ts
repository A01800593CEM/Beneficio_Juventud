/**
 * Enum representing the possible states of a notification in the system.
 * Used to track the delivery and reading status of notifications.
 */
export enum NotificationStatus {
    /**
     * Notification has been successfully sent to the recipient.
     * @value "enviado"
     */
    SENT = 'enviado',

    /**
     * Notification is queued and waiting to be sent.
     * @value "pendiente"
     */
    PENDING = 'pendiente',

    /**
     * Notification has been read by the recipient.
     * @value "leido"
     */
    READ = 'leido'
}