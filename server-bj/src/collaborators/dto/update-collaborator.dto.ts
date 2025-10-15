import { PartialType } from '@nestjs/mapped-types';
import { CreateCollaboratorDto } from './create-collaborator.dto';

/**
 * Data Transfer Object for updating an existing collaborator.
 * Extends CreateCollaboratorDto but makes all properties optional using NestJS's PartialType.
 * 
 * @description This DTO inherits all properties and validation rules from CreateCollaboratorDto
 * but makes them optional for partial updates. This allows updating only specific fields
 * of a collaborator without requiring all fields to be present in the request.
 * 
 * Available fields for update (all optional):
 * - businessName: string
 * - rfc: string
 * - representativeName: string
 * - phone: string
 * - email: string
 * - address: string
 * - postalCode: string
 * - categoryIds: number[]
 * - logoUrl: string
 * - description: string
 * - state: CollaboratorState
 */
export class UpdateCollaboratorDto extends PartialType(CreateCollaboratorDto) {}
