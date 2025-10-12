import { IsDate, IsEnum, IsInt} from 'class-validator';
import { Type } from 'class-transformer';
import { BookStatus } from '../enums/book-status.enum';

export class CreateBookingDto {
  @IsInt()
  promotionId: number;

  @IsInt()
  userId: string;

  @IsDate() @Type(() => Date)
  limitUseDate?: Date;

  @IsEnum(BookStatus)
  bookStatus: BookStatus;

  @IsInt()
  bookedPromotion?: number;
}
