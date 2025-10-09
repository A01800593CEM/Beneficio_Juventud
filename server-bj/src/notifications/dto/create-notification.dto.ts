import { IsEnum, IsInt, IsNotEmpty, IsObject, IsOptional, IsString } from 'class-validator';
import { NotificationType } from '../enums/notification-type.enums';
import { NotificationStatus } from '../enums/notification-status.enum';
import { RecipientType } from '../enums/recipient-type.enums';

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
