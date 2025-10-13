/**
 * Represents the possible operational states of a branch.
 * Used to track whether a branch is currently operating or not.
 */
export enum BranchState {
    /**
     * Branch is currently operating and available.
     * @value 'activa'
     */
    ACTIVE = 'activa',

    /**
     * Branch is currently not operating or unavailable.
     * @value 'inactiva'
     */
    INACTIVE = 'inactiva'
}