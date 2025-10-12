import { IsDate, IsEnum, IsInt} from 'class-validator';
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
  userId: number;

  @IsDate() @Type(() => Date)
  limitUseDate?: Date;

  @IsEnum(BookStatus)
  bookStatus: BookStatus;

  @IsInt()
  bookedPromotion?: number;
}
