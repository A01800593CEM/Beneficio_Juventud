import { PartialType } from '@nestjs/mapped-types';
import { CreateBookingDto } from './create-booking.dto';

/**
 * Data Transfer Object (DTO) for updating an existing booking.
 * Extends CreateBookingDto as a partial type, making all fields optional.
 * 
 * @description
 * This DTO inherits all properties from CreateBookingDto but makes them optional,
 * allowing for partial updates of booking information. This means you can update
 * one or more fields without having to provide values for all fields.
 * 
 * Properties inherited from CreateBookingDto:
 * - promotionId: number (optional)
 * - userId: number (optional)
 * - limitUseDate: Date (optional)
 * - bookStatus: BookStatus (optional)
 * - bookedPromotion: number (optional)
 * 
 * All validation rules from CreateBookingDto are preserved but made optional:
 * - @IsInt() for numeric fields
 * - @IsDate() for date fields
 * - @IsEnum(BookStatus) for status field
 */
export class UpdateBookingDto extends PartialType(CreateBookingDto) {}
