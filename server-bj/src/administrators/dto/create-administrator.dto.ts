import {
  IsEmail,
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsPhoneNumber,
  IsString,
  MaxLength,
} from 'class-validator';
import { AdminRole } from '../enums/admin-role.enums';
import { AdminState } from '../enums/admin-state.enum';

/**
 * Data Transfer Object (DTO) for creating a new administrator.
 * Contains all necessary information for administrator registration with validation rules.
 */
export class CreateAdministratorDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  firstName: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  lastNameFather: string;

  @IsString()
  @IsOptional()
  @MaxLength(255)
  lastNameMother?: string;

  @IsEmail()
  @IsNotEmpty()
  email: string;

  @IsPhoneNumber('MX')
  @IsOptional()
  phone?: string;

  @IsEnum(AdminRole)
  role: AdminRole;

  @IsEnum(AdminState)
  status: AdminState;
}
