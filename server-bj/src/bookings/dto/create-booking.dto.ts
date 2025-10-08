import { IsDate, IsEnum, IsInt, IsOptional} from 'class-validator';
import { Type } from 'class-transformer';
import { BookStatus } from '../enums/book-status.enum';

export class CreateBookingDto {
  @IsInt()
  bookingId: number;

  @IsInt()
  promotionId: number;

  @IsDate() @Type(() => Date)
  limitUseDate?: Date;

  @IsEnum(BookStatus)
  bookStatus: BookStatus;

  @IsOptional() @IsInt()
  bookedPromotion?: number;
}
