import { PartialType } from '@nestjs/mapped-types';
import { CreatePromotionDto } from './create-promotion.dto';

/**
 * DTO representing the data required to update a promotion.
 *
 * @remarks
 * This class inherits all properties from {@link CreatePromotionDto},
 * but makes them optional using the `PartialType()` utility provided by `@nestjs/mapped-types`.
 * 
 * This ensures that updates can include only the fields that need to be changed.
 *
 * @example
 * ```json
 * {
 *   "title": "New Year Mega Sale",
 *   "endDate": "2025-01-10T00:00:00.000Z",
 *   "promotionState": "ACTIVE"
 * }
 * ```
 */
export class UpdatePromotionDto extends PartialType(CreatePromotionDto) {}
