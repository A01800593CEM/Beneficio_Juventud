import { PartialType } from '@nestjs/mapped-types';
import { CreateFavoriteDto } from './create-favorite.dto';

/**
 * Data Transfer Object for updating an existing favorite relationship.
 * Extends CreateFavoriteDto but makes all properties optional using NestJS's PartialType.
 * 
 * @description This DTO inherits all properties and validation rules from CreateFavoriteDto
 * but makes them optional for partial updates. Available fields for update (all optional):
 * - userId: number
 * - collaboratorId: number
 * 
 * Note: In practice, updating favorite relationships might be rare as they are typically
 * created or deleted rather than modified.
 */
export class UpdateFavoriteDto extends PartialType(CreateFavoriteDto) {}
