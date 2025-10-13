import { IsInt,} from 'class-validator';

/**
 * DTO representing the data required to register a redeemed coupon.
 *
 * @remarks
 * This class ensures type safety and input validation for the
 * creation of a `Redeemedcoupon` entity through the API.
 *
 * It validates that all related foreign keys (user, collaborator, branch,
 * and promotion) are integers before persisting to the database.
 *
 * @example
 * ```json
 * {
 *   "userId": 14,
 *   "collaboratorId": 3,
 *   "branchId": 8,
 *   "promotionId": 27
 * }
 * ```
 */
export class CreateRedeemedcouponDto {
  @IsInt()
  userId: string;

  @IsInt()
  collaboratorId: string;

  @IsInt()
  branchId: number;

  @IsInt()
  promotionId: number;
}
