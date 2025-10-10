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
