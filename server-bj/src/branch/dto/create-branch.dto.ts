import { IsInt, IsNotEmpty, IsObject, IsOptional, IsPhoneNumber, IsString, Matches} from 'class-validator';
export class CreateBranchDto {
    @IsInt()
    collaboratorId: number;
    
    @IsString() @IsNotEmpty()
    name: string;

    @IsPhoneNumber('MX')
    phone: string;

    @IsString() @IsNotEmpty()
    direction: string;

    @IsString() @IsNotEmpty()
    zipCode: string;

    // Acepta formato "(lon,lat)" o "(x,y)" como string
    @IsOptional()
    @IsString()
    @Matches(/^\(\s*-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?\s*\)$/)
    location?: string;

    @IsOptional() @IsObject()
    jsonSchedule?: Record<string, any>;
}
