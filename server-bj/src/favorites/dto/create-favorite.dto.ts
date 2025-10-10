import { IsInt, IsNotEmpty } from 'class-validator';

export class CreateFavoriteDto {
  @IsInt()
  @IsNotEmpty()
  userId: number;

  @IsInt()
  @IsNotEmpty()
  collaboratorId: number;
}
