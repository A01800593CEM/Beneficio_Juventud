import { IsString, IsEmail, IsEnum, MinLength, IsNotEmpty, IsDate, IsPhoneNumber, MaxLength, ArrayNotEmpty, IsArray } from 'class-validator';
import { Type } from "class-transformer";
import { UserState } from "../enums/user-state.enum";

/**
 * DTO representing the data required to create a new user record.
 *
 * @remarks
 * This class applies validation rules to incoming data before persisting a user.
 * It uses decorators from `class-validator` to enforce correct formats, and
 * `class-transformer` to handle date conversion.
 *
 * @example
 * ```json
 * {
 *   "name": "Iván",
 *   "lastNamePaternal": "Carrillo",
 *   "lastNameMaternal": "López",
 *   "birthDate": "2001-08-15T00:00:00.000Z",
 *   "phoneNumber": "+52 5512345678",
 *   "email": "ivan@example.com",
 *   "accountState": "activo"
 * }
 * ```
 */
export class CreateUserDto {
    @IsString()
    @IsNotEmpty()
    name: string;

    @IsString()
    @IsNotEmpty()
    @MaxLength(50)
    cognitoId: string;

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

    @IsArray()
    @IsString({ each: true})
    userPrefCategories: string[]
}
