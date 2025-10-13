import { PartialType } from '@nestjs/mapped-types';
import { CreateBranchDto } from './create-branch.dto';

/**
 * Data Transfer Object (DTO) for updating an existing branch.
 * Extends CreateBranchDto as a partial type, making all fields optional.
 * 
 * @description
 * This DTO inherits all properties from CreateBranchDto but makes them optional,
 * allowing for partial updates of branch information. This means you can update
 * one or more fields without having to provide values for all fields.
 * 
 * Properties inherited from CreateBranchDto:
 * - collaboratorId: number (optional)
 * - name: string (optional)
 * - phone: string (optional)
 * - address: string (optional)
 * - zipCode: string (optional)
 * - location: string (optional)
 * - jsonSchedule: Record<string, any> (optional)
 * 
 * All validation rules from CreateBranchDto are preserved but made optional:
 * - @IsInt() for collaboratorId
 * - @IsString() and @IsNotEmpty() for strings
 * - @IsPhoneNumber('MX') for phone
 * - @Matches() for location format "(lat,lon)"
 * - @IsObject() for jsonSchedule
 */
export class UpdateBranchDto extends PartialType(CreateBranchDto) {}
