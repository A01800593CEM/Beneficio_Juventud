import {
  IsDate,
  IsEnum,
  IsInt,
  IsOptional,
  IsString,
  IsUrl,
  IsArray,
  ArrayNotEmpty,
  IsBoolean,
} from 'class-validator';
import { Type } from 'class-transformer';
import { PromotionType } from '../enums/promotion-type.enums';
import { PromotionState } from '../enums/promotion-state.enums';
import { PromotionTheme } from '../enums/promotion-theme.enum';

/**
 * DTO representing the payload required to create a new promotion.
 */
export class CreatePromotionDto {
  // En entidad es string (Cognito ID)
  @IsString()
  collaboratorId: string;

  @IsString()
  title: string;

  @IsString()
  description: string;

  @IsOptional()
  @IsUrl()
  imageUrl?: string;

  @IsDate()
  @Type(() => Date)
  initialDate: Date;

  @IsDate()
  @Type(() => Date)
  endDate: Date;

  @IsOptional()
  @IsInt()
  categoryId?: number;

  @IsEnum(PromotionType)
  promotionType: PromotionType;

  @IsOptional()
  @IsString()
  promotionString?: string;

  @IsOptional()
  @IsInt()
  totalStock?: number;

  @IsOptional()
  @IsInt()
  availableStock?: number;

  @IsOptional()
  @IsInt()
  limitPerUser?: number;

  @IsOptional()
  @IsInt()
  dailyLimitPerUser?: number;

  @IsEnum(PromotionState)
  promotionState: PromotionState;

  @IsOptional()
  @IsArray()
  @ArrayNotEmpty()
  @IsInt({ each: true })
  categoryIds?: number[];

  /**
   * (Legacy) si alguien sigue mandando "promotionTheme", lo aceptamos.
   * Se mapeará a "theme" en el servicio.
   */
  @IsEnum(PromotionTheme)
  @IsOptional()
  promotionTheme?: PromotionTheme;

  @IsEnum(PromotionTheme)
  @IsOptional()
  theme?: PromotionTheme;

  @IsBoolean()
  is_bookable: boolean;
}
