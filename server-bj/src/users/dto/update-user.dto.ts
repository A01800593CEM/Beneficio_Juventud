import { PartialType } from '@nestjs/mapped-types';
import { CreateUserDto } from './create-user.dto';

/**
 * DTO representing the payload for updating a user.
 *
 * @remarks
 * Uses `PartialType` so all properties from {@link CreateUserDto} become optional,
 * making it ideal for `PATCH` endpoints. The `registrationDate?: never` member is
 * declared to **forbid** clients from sending a `registrationDate` field during updates
 * (it should be managed by the system, not the caller).
 *
 * @example
 * ```json
 * {
 *   "phoneNumber": "+52 5512345678"
 * }
 * ```
 *
 * @example
 * // ❌ This will be rejected at compile-time due to `never`:
 * {
 *   "registrationDate": "2025-10-07T02:47:57.541Z"
 * }
 * ```
 */
export class UpdateUserDto extends PartialType(CreateUserDto) {
    registrationDate?: never;
}
