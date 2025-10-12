/**
 * Enum representing the different types of notifications that can be sent in the system.
 * Used to categorize notifications based on their purpose and content.
 */
export enum NotificationType {
    /**
     * General informational notifications.
     * Used for system updates, news, and general communications.
     * @value "info"
     */
    INFO = 'info',

    /**
     * Promotional notifications.
     * Used for marketing campaigns, special offers, and deals.
     * @value "promo"
     */
    PROMOTION = 'promo',

    /**
     * Alert notifications.
     * Used for important announcements, warnings, or time-sensitive information.
     * @value "alerta"
     */
    ALERT = 'alerta'
}