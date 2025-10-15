import { IsString, IsNotEmpty } from 'class-validator';

/**
 * Data Transfer Object for creating a new favorite relationship between a user and a collaborator.
 * Used when a user wants to mark a collaborator as favorite.
 */
export class CreateFavoriteDto {
  @IsString()
  @IsNotEmpty()
  userId: string;

  @IsString()
  @IsNotEmpty()
  collaboratorId: string;
}
