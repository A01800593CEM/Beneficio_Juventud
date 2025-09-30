import { IsString, IsEmail, IsEnum, MinLength, IsNotEmpty, IsDate, IsPhoneNumber } from 'class-validator';
import { UserState } from "../enums/user-state.enum";

export class CreateUsuarioDto {
    @IsString()
    @IsNotEmpty()
    firstName: string;

    @IsString()
    @IsNotEmpty()
    lastNamePaternal: string;

    @IsString()
    @IsNotEmpty()
    lastNameMaternal: string;

    @IsDate()
    birthDate: Date

    @IsPhoneNumber("MX")
    phoneNumber: String

    @IsEmail()
    email: String

    @IsDate()
    registrationDate: Date

    @IsDate()
    updatedAt: Date

    @IsEnum(UserState)
    accountState: UserState


}
