import { IsInt, IsString, IsOptional, IsNumber} from 'class-validator';

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
 *   "userId": "a1fbe500-a091-70e3-5a7b-3b1f4537f10f",
 *   "branchId": 8,
 *   "promotionId": 27,
 *   "nonce": "abc12345",
 *   "qrTimestamp": 1729458361234
 * }
 * ```
 */
export class CreateRedeemedcouponDto {
  @IsString()
  userId: string;

  @IsInt()
  branchId: number;

  @IsInt()
  promotionId: number;

  @IsOptional()
  @IsString()
  nonce?: string;

  @IsOptional()
  @IsNumber()
  qrTimestamp?: number;
}
