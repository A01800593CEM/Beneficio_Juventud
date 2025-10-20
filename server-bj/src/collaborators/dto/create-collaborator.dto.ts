import { 
  IsString, 
  IsEmail, 
  IsOptional, 
  IsNotEmpty, 
  IsNumber, 
  IsEnum,  
  MaxLength, 
  IsArray,
  ArrayNotEmpty,
  IsInt
} from 'class-validator';
import { CollaboratorState } from '../enums/collaborator-state.enum';

/**
 * Data Transfer Object for creating a new collaborator.
 * Contains all the necessary fields and validation rules for collaborator creation.
 */
export class CreateCollaboratorDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  businessName: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(50)
  cognitoId: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(20)
  rfc: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  representativeName: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(20)
  phone: string;

  @IsEmail()
  @IsNotEmpty()
  @MaxLength(255)
  email: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  address: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(10)
  postalCode: string;

  // 👇 Multiple category IDs (opcional durante registro, se puede actualizar después)
  @IsOptional()
  @IsArray()
  @IsInt({ each: true })
  categoryIds?: number[];

  @IsOptional()
  @IsString()
  @MaxLength(255)
  logoUrl?: string;

  @IsOptional()
  @IsString()
  description?: string;

  @IsEnum(CollaboratorState)
  state: CollaboratorState;
}
