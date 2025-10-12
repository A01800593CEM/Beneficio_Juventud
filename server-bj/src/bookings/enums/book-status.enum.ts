/**
 * Represents the possible states of a booking in the system.
 * Used to track the lifecycle of a booking from creation to completion or cancellation.
 */
export enum BookStatus {
    /**
     * Indicates that the booking is active but hasn't been used yet.
     * @value 'pendiente'
     */
    PENDING = 'pendiente',

    /**
     * Indicates that the booking has been redeemed/used.
     * @value 'usada'
     */
    USED = 'usada',

    /**
     * Indicates that the booking has been cancelled.
     * @value 'cancelada'
     */
    CANCELLED = 'cancelada'
}