/**
 * Represents the type of promotion being offered.
 *
 * @remarks
 * The `PromotionType` enum helps differentiate business logic and display behavior
 * depending on how the promotion is applied to the customer.
 *
 * @example
 * ```ts
 * switch (promotion.promotionType) {
 *   case PromotionType.DISCOUNT:
 *     console.log('Applies a percentage discount to the purchase.');
 *     break;
 *   case PromotionType.GIFT:
 *     console.log('Includes a free product with qualifying purchase.');
 *     break;
 * }
 * ```
 */
export enum PromotionType { 
     /**
   * Applies a discount — usually a percentage or fixed amount off the original price.
   * @example "descuento"
   */
    DISCOUNT = 'descuento', 

    /**
   * Promotion that applies when buying multiple items (e.g., “2x1”, “Buy 3, pay 2”).
   * @example "multicompra"
   */
    MULTYBUY = 'multicompra', 

    /**
   * Grants an additional product or service for free when purchasing another.
   * @example "regalo"
   */
    GIFT = 'regalo', 

    /**
   * Used for other or custom types of promotions that do not fit predefined categories.
   * @example "otro"
   */
    OTHER = 'otro' 
}
