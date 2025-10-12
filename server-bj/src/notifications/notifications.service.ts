import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateNotificationDto } from './dto/create-notification.dto';
import { UpdateNotificationDto } from './dto/update-notification.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Notification } from './entities/notification.entity';

/**
 * Service responsible for managing `Notification` entities.
 *
 * @remarks
 * This class uses dependency injection to access the `Notification` repository,
 * allowing database operations such as create, read, update, and delete.
 */
@Injectable()
export class NotificationsService {
  /**
   * Creates an instance of NotificationsService.
   *
   * @param notificationRepository - TypeORM repository for managing {@link Notification} entities.
   */
  constructor(
    @InjectRepository(Notification)
    private notificationRepository: Repository<Notification>,
  ) {}

  /**
   * Creates and saves a new notification in the database.
   *
   * @param createNotificationDto - Data Transfer Object containing the notification data.
   * @returns A Promise resolving to the created {@link Notification}.
   */

  async create(createNotificacionDto: CreateNotificationDto): Promise<Notification> {
    const notification = this.notificationRepository.create(createNotificacionDto);
    return this.notificationRepository.save(notification);
  }

  /**
   * Retrieves all notifications, including their related promotions.
   *
   * @returns A Promise resolving to an array of {@link Notification} entities.
   */

  async findAll(): Promise<Notification[]> {
    return this.notificationRepository.find({relations: ['promotion']});
  }

  /**
   * Finds a specific notification by its ID.
   *
   * @param id - The unique identifier of the notification.
   * @returns A Promise resolving to the found {@link Notification} or `null` if not found.
   */

  async findOne(id: number): Promise<Notification | null> {
    return this.notificationRepository.findOne({
      where: {notificationId: id},
      relations: ['promotion']
    });
  }

  /**
   * Updates an existing notification with the provided data.
   *
   * @param id - The ID of the notification to update.
   * @param updateNotificationDto - DTO containing the updated properties.
   * @throws {NotFoundException} If the notification with the given ID does not exist.
   * @returns A Promise resolving to the updated {@link Notification}.
   */

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

  /**
   * Deletes a notification by its ID.
   *
   * @param id - The ID of the notification to delete.
   * @returns A Promise resolving when the deletion is complete.
   */

  async remove(id: number): Promise<void> {
    await this.notificationRepository.delete(id);
  }
}
