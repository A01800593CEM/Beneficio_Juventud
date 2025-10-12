import { PartialType } from '@nestjs/mapped-types';
import { CreateNotificationDto } from './create-notification.dto';

/**
 * Data Transfer Object for updating an existing notification.
 * Extends CreateNotificationDto but makes all properties optional using NestJS's PartialType.
 * 
 * @description This DTO inherits all properties and validation rules from CreateNotificationDto
 * but makes them optional for partial updates. This allows updating specific fields of a
 * notification without requiring all fields to be present in the request.
 * 
 * Available fields for update (all optional):
 * - title: string
 * - description: string
 * - userId: number
 * - type: NotificationType
 * - state: NotificationState
 * - dateTime: Date
 * - isRead: boolean
 */
export class UpdateNotificationDto extends PartialType(CreateNotificationDto) {}
