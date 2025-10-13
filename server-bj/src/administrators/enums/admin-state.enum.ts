/**
 * Represents the possible states of an administrator account.
 * Used to track and control administrator access to the system.
 */
export enum AdminState {
    /**
     * Administrator account is active and has full access.
     * Can perform all actions according to their role.
     * @value 'activo'
     */
    ACTIVE = "activo",

    /**
     * Administrator account is inactive.
     * Cannot access the system but can be reactivated.
     * @value 'inactivo'
     */
    INACTIVE = "inactivo",

    /**
     * Administrator account is temporarily suspended.
     * Access is blocked but can be restored after review.
     * @value 'suspendido'
     */
    SUSPENDED = "suspendido"
}