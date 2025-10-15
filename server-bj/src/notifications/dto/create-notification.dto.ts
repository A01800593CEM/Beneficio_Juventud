import { IsEnum, IsInt, IsNotEmpty, IsObject, IsOptional, IsString } from 'class-validator';
import { NotificationType } from '../enums/notification-type.enums';
import { NotificationStatus } from '../enums/notification-status.enum';
import { RecipientType } from '../enums/recipient-type.enums';

/**
 * Data Transfer Object for creating a new notification.
 * Contains all the necessary fields and validation rules for notification creation.
 */
export class CreateNotificationDto {
  @IsString() @IsNotEmpty()
  title: string;

  @IsString() @IsNotEmpty()
  message: string;

  @IsEnum(NotificationType)
  type: NotificationType;

  @IsEnum(RecipientType)
  recipientType: RecipientType;

  @IsOptional() @IsInt()
  recipientId?: number;

  @IsEnum(NotificationStatus)
  status: NotificationStatus;

  @IsOptional() @IsObject()
  segmentCriteria?: Record<string, any>;
  
  @IsOptional() @IsInt()
  promotionId?: number;
}
