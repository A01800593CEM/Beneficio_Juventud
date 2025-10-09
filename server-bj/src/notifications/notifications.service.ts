import { Injectable } from '@nestjs/common';
import { CreateNotificationDto } from './dto/create-notification.dto';
import { UpdateNotificationDto } from './dto/update-notification.dto';

@Injectable()
export class NotificationsService {
  create(createNotificacionDto: CreateNotificationDto) {
    return 'This action adds a new notificacion';
  }

  findAll() {
    return `This action returns all notificacion`;
  }

  findOne(id: number) {
    return `This action returns a #${id} notificacion`;
  }

  update(id: number, updateNotificacionDto: UpdateNotificationDto) {
    return `This action updates a #${id} notificacion`;
  }

  remove(id: number) {
    return `This action removes a #${id} notificacion`;
  }
}
