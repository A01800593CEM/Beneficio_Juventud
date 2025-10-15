import { PartialType } from '@nestjs/mapped-types';
import { CreateCategoryDto } from './create-category.dto';

/**
 * Data Transfer Object (DTO) for updating an existing category.
 * Extends CreateCategoryDto as a partial type, making all fields optional.
 * 
 * @description
 * This DTO inherits all properties from CreateCategoryDto but makes them optional,
 * allowing for partial updates of category information. This means you can update
 * the name field without having to provide all other fields.
 * 
 * Properties inherited from CreateCategoryDto:
 * - name: string (optional)
 * 
 * All validation rules from CreateCategoryDto are preserved but made optional:
 * - @IsString() for string validation
 * - @IsNotEmpty() for non-empty validation
 * 
 * @example
 * // Partial update with just the name
 * {
 *   "name": "Updated Category Name"
 * }
 */
export class UpdateCategoryDto extends PartialType(CreateCategoryDto) {}
