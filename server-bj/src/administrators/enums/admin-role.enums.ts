/**
 * Represents the possible administrative roles in the system.
 * Defines the hierarchy and access levels for administrators.
 */
export enum AdminRole {
    /**
     * Highest level administrator with full system access.
     * Has access to all features and can manage other administrators.
     * @value 'superadmin'
     */
    SUPER_ADMIN = 'superadmin',

    /**
     * Standard administrator with moderation capabilities.
     * Has access to day-to-day administrative functions.
     * @value 'admin'
     */
    MODERATOR = 'admin',
}