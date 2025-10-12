import { PartialType } from '@nestjs/mapped-types';
import { CreateRedeemedcouponDto } from './create-redeemedcoupon.dto';

/**
 * DTO representing the payload for updating a redeemed coupon record.
 *
 * @remarks
 * This class inherits all properties from {@link CreateRedeemedcouponDto},
 * but makes them optional using the `PartialType()` utility provided by
 * `@nestjs/mapped-types`.
 *
 * It is typically used in `PATCH` routes to allow partial updates of a
 * `Redeemedcoupon` entity.
 *
 * @example
 * ```json
 * {
 *   "branchId": 10
 * }
 * ```
 */
export class UpdateRedeemedcouponDto extends PartialType(CreateRedeemedcouponDto) {}
