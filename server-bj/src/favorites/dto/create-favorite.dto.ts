import { IsInt, IsNotEmpty } from 'class-validator';

export class CreateFavoriteDto {
  @IsInt()
  @IsNotEmpty()
  userId: string;

  @IsInt()
  @IsNotEmpty()
  collaboratorId: string;
}
