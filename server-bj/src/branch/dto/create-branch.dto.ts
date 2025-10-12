import { IsInt, 
    IsNotEmpty, 
    IsObject, 
    IsOptional, 
    IsPhoneNumber, 
    IsString, 
    Matches} 
from 'class-validator';

/**
 * Data Transfer Object (DTO) for creating a new branch.
 * Contains all necessary information for branch registration with validation rules.
 */
export class CreateBranchDto {
    @IsInt()
    collaboratorId: number;
    
    @IsString() @IsNotEmpty()
    name: string;

    @IsPhoneNumber('MX')
    phone: string;

    @IsString() @IsNotEmpty()
    address: string;

    @IsString() @IsNotEmpty()
    zipCode: string;

    // Accepts format “(lon,lat)” or “(x,y)” as string
    @IsOptional()
    @IsString()
    @Matches(/^\(\s*-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?\s*\)$/)
    location?: string;

    @IsOptional() @IsObject()
    jsonSchedule?: Record<string, any>;
}
