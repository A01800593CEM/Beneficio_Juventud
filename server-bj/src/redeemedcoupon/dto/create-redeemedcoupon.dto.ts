import { IsInt,} from 'class-validator';

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
