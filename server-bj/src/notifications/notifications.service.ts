import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateNotificationDto } from './dto/create-notification.dto';
import { UpdateNotificationDto } from './dto/update-notification.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Notification } from './entities/notification.entity';
import admin from "firebase-admin";
import serviceAccount from "beneficio-joven-firebase-adminsdk-fbsvc-076ac563fc.json" with { type: "json"};

@Injectable()
export class NotificationsService {
  
}
