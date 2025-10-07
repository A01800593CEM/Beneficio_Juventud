import { IsString, IsEmail, IsEnum, MinLength, IsNotEmpty, IsDate, IsPhoneNumber } from 'class-validator';
import { Type } from "class-transformer";
import { UserState } from "../enums/user-state.enum";

export class CreateUserDto {
    @IsString()
    @IsNotEmpty()
    name: string;

    @IsString()
    @IsNotEmpty()
    lastNamePaternal: string;

    @IsString()
    @IsNotEmpty()
    lastNameMaternal: string;

    @IsDate()
    @Type(() => Date)
    birthDate: Date

    @IsPhoneNumber("MX")
    phoneNumber: string

    @IsEmail()
    email: string

    @IsEnum(UserState)
    accountState: UserState
}
