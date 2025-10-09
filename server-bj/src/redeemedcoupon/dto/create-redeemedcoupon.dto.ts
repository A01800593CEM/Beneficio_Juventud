import { IsInt,} from 'class-validator';

export class CreateRedeemedcouponDto {
  @IsInt()
  userId: number;

  @IsInt()
  collaboratorId: number;

  @IsInt()
  branchId: number;

  @IsInt()
  promotionId: number;
}
