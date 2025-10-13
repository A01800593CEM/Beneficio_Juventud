/**
 * Enum representing the possible states of a collaborator in the system.
 * Used to track and manage the collaborator's current status.
 */
export enum CollaboratorState {
    /**
     * Collaborator is active and can operate normally in the system.
     * @value "activo"
     */
    ACTIVE = "activo",

    /**
     * Collaborator is inactive and temporarily cannot operate in the system.
     * @value "inactivo"
     */
    INACTIVE = "inactivo",

    /**
     * Collaborator is suspended due to policy violations or administrative decision.
     * @value "suspendido"
     */
    SUSPENDED = "suspendido"
}