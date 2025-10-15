import { PartialType } from '@nestjs/mapped-types';
import { CreateAdministratorDto } from './create-administrator.dto';

/**
 * Data Transfer Object (DTO) for updating an existing administrator.
 * Extends CreateAdministratorDto as a partial type, making all fields optional.
 * 
 * @description
 * This DTO inherits all properties from CreateAdministratorDto but makes them optional,
 * allowing for partial updates of administrator information. This means you can update
 * one or more fields without having to provide values for all fields.
 * 
 * Properties inherited from CreateAdministratorDto:
 * - firstName: string (optional)
 * - lastNameFather: string (optional)
 * - lastNameMother: string (optional)
 * - email: string (optional)
 * - phone: string (optional)
 * - role: AdminRole (optional)
 * - status: AdminState (optional)
 * 
 * All validation rules from CreateAdministratorDto are preserved but made optional.
 */
export class UpdateAdministratorDto extends PartialType(CreateAdministratorDto) {}
