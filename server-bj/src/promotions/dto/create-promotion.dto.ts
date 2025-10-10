import { IsDate, IsEnum, IsInt, IsOptional, IsString, IsUrl } from 'class-validator';
import { Type } from 'class-transformer';
import { PromotionType } from '../enums/promotion-type.enums';
import { PromotionState } from '../enums/promotion-state.enums';

export class CreatePromotionDto {
  @IsInt()
  collaboratorId: number;

  @IsString()
  title: string;

  @IsString()
  description: string;

  @IsOptional() @IsUrl()
  imageUrl?: string;

  @IsDate() @Type(() => Date)
  initialDate: Date;

  @IsDate() @Type(() => Date)
  endDate: Date;

  @IsOptional() @IsInt()
  categoryId?: number;

  @IsEnum(PromotionType)
  promotionType: PromotionType;

  @IsOptional() @IsString()
  promotionString?: string;

  @IsOptional() @IsInt()
  totalStock?: number;

  @IsOptional() @IsInt()
  availableStock?: number;

  @IsOptional() @IsInt()
  limitPerUser?: number;

  @IsOptional() @IsInt()
  dailyLimitPerUser?: number;

  @IsEnum(PromotionState)
  promotionState: PromotionState;
}
