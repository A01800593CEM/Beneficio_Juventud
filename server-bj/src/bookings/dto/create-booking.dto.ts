import { IsDate, IsEnum, IsInt, IsOptional} from 'class-validator';
import { Type } from 'class-transformer';
import { BookStatus } from '../enums/book-status.enum';

export class CreateBookingDto {
  @IsInt()
  booking_id: number;

  @IsInt()
  promotion_id: number;

  @IsDate() @Type(() => Date)
  limit_use_date?: Date;

  @IsEnum(BookStatus)
  BookStatus: BookStatus;

  @IsOptional() @IsInt()
  booked_promotion?: number;
}
