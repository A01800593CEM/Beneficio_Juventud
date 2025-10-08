import { IsDate, IsEnum, IsInt, IsNotEmpty, IsOptional, IsString, Min, MinLength, IsUrl } from 'class-validator';
import { Type } from 'class-transformer';
import { PromotionType } from '../enums/promotion-type.enums';
import { PromotionState } from '../enums/promotion-state.enums';

export class CreatePromotionDto {
  @IsInt()
  colaborador_id: number;

  @IsString()
  titulo: string;

  @IsString()
  descripcion: string;

  @IsOptional() @IsUrl()
  imagen_url?: string;

  @IsDate() @Type(() => Date)
  fecha_inicio: Date;

  @IsDate() @Type(() => Date)
  fecha_fin: Date;

  @IsOptional() @IsInt()
  categoria_id?: number;

  @IsEnum(PromotionType)
  tipo_promocion: PromotionType;

  @IsOptional() @IsString()
  promocion_string?: string;

  @IsOptional() @IsInt()
  stock_total?: number;

  @IsOptional() @IsInt()
  stock_disponible?: number;

  @IsOptional() @IsInt()
  limite_por_usuario?: number;

  @IsOptional() @IsInt()
  limite_diario_por_usuario?: number;

  @IsEnum(PromotionState)
  estado: PromotionState;
}
