import {
    IsString,
    IsNotEmpty
} from 'class-validator'

/**
 * Data Transfer Object (DTO) for creating a new category.
 * Contains all necessary information for category creation with validation rules.
 */
export class CreateCategoryDto {
    @IsString()
    @IsNotEmpty()
    name: string;
}
