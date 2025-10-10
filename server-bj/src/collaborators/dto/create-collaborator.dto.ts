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

export class CreateCollaboratorDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(255)
  businessName: string;

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

  // 👇 Multiple category IDs
  @IsArray()
  @ArrayNotEmpty()
  @IsInt({ each: true })s
  categoryIds: number[];

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
