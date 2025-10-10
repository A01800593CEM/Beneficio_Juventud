import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateNotificationDto } from './dto/create-notification.dto';
import { UpdateNotificationDto } from './dto/update-notification.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Notification } from './entities/notification.entity';

@Injectable()
export class NotificationsService {
  constructor(
    @InjectRepository(Notification)
    private notificationRepository: Repository<Notification>,
  ) {}

  async create(createNotificacionDto: CreateNotificationDto): Promise<Notification> {
    const notification = this.notificationRepository.create(createNotificacionDto);
    return this.notificationRepository.save(notification);
  }

  async findAll(): Promise<Notification[]> {
    return this.notificationRepository.find({relations: ['promotion']});
  }

  async findOne(id: number): Promise<Notification | null> {
    return this.notificationRepository.findOne({
      where: {notificationId: id},
      relations: ['promotion']
    });
  }

  async update(id: number, updateNotificacionDto: UpdateNotificationDto): Promise<Notification | null> {
    const notificacion = await this.notificationRepository.preload({
      notificationId: id,
      ...UpdateNotificationDto,
    });
    if(!notificacion){
      throw new NotFoundException(`Notification with id ${id} not found`)
    }
    return this.notificationRepository.save(notificacion);
  }

  async remove(id: number): Promise<void> {
    await this.notificationRepository.delete(id);
  }
}
