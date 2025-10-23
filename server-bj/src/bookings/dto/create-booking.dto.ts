import { IsDate, IsEnum, IsInt, IsOptional } from 'class-validator';
import { Type } from 'class-transformer';
import { BookStatus } from '../enums/book-status.enum';

/**
 * Data Transfer Object (DTO) for creating a new booking.
 * This class defines the structure and validation rules for booking creation requests.
 */
export class CreateBookingDto {
  @IsInt()
  promotionId: number;

  @IsInt()
  userId: string;

  @IsDate() @Type(() => Date)
  @IsOptional()
  limitUseDate?: Date;

  @IsEnum(BookStatus)
  @IsOptional()
  status?: BookStatus;

  @IsInt()
  @IsOptional()
  bookedPromotion?: number;

  @IsDate() @Type(() => Date)
  @IsOptional()
  autoExpireDate?: Date;

  @IsDate() @Type(() => Date)
  @IsOptional()
  cooldownUntil?: Date;
}
