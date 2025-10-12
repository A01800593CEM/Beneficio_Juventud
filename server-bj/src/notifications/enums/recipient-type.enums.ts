/**
 * Enum representing the different types of recipients that can receive notifications.
 * Used to categorize notification recipients based on their role in the system.
 */
export enum RecipientType {
    /**
     * Regular user of the platform.
     * Used for notifications targeting end-users of the application.
     * @value "usuario"
     */
    USER = 'usuario',

    /**
     * Business collaborator or partner.
     * Used for notifications targeting business partners and service providers.
     * @value "colaborador"
     */
    COLLABORATOR = 'colaborador',

    /**
     * System administrator.
     * Used for notifications targeting administrative staff and system managers.
     * @value "admin"
     */
    ADMINISTRATOR = 'admin'
}