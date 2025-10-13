/**
 * Represents the current state of a promotion.
 *
 * @remarks
 * The state determines whether a promotion is currently visible and redeemable
 * by users, has been disabled by a collaborator, or has concluded naturally.
 *
 * @example
 * ```ts
 * if (promotion.promotionState === PromotionState.ACTIVE) {
 *   console.log('The promotion is live!');
 * }
 * ```
 */
export enum PromotionState{
    /**
   * The promotion is currently active and available to users.
   */
    ACTIVE = 'activa',
    /**
   * The promotion has been deactivated manually and is not visible or redeemable.
   */ 
    INACTIVE = 'inactiva', 
    /**
   * The promotion has ended after reaching its expiration date or stock limit.
   */
    FINISHED = 'finalizada'
}