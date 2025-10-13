/**
 * Represents the current operational state of a user account.
 *
 * @remarks
 * This enum is used throughout the system to control user access and availability.
 * 
 * - `ACTIVE`: The user account is active and can access the platform.
 * - `INACTIVE`: The account has been deactivated by the user or an admin.
 * - `SUSPENDED`: The account has been temporarily restricted due to policy violations or other administrative actions.
 *
 * @example
 * ```ts
 * if (user.accountState === UserState.SUSPENDED) {
 *   throw new Error("User access is restricted.");
 * }
 * ```
 */
export enum UserState {
    /**
   * The user account is active and can access all features of the platform.
   */
    ACTIVE = "activo",
    /**
   * The user account is deactivated — cannot log in or perform actions.
   */
    INACTIVE = "inactivo",
    /**
   * The user account is temporarily suspended due to administrative reasons.
   */
    SUSPENDED = "suspendido"
}