import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { NotificationsService } from './notifications.service';
import { CreateNotificationDto } from './dto/create-notification.dto';
import { UpdateNotificationDto } from './dto/update-notification.dto';

/**
 * Controller responsible for handling notification-related HTTP requests.
 * Provides endpoints for managing notifications in the system.
 * @route /notification
 */
@Controller('notification')
export class NotificationsController {
  /**
   * Creates an instance of NotificationsController.
   * @param notificacionService - The service handling notification business logic
   */
  constructor(private readonly notificacionService: NotificationsService) {}

  /**
   * Creates a new notification.
   * @route POST /notification
   * @param createNotificacionDto - The data transfer object containing notification details
   * @returns Promise containing the newly created notification
   * @example
   * // Request body
   * {
   *   "title": "New Promotion",
   *   "message": "Check out our latest offers!",
   *   "type": "PROMOTION",
   *   "recipientType": "USER",
   *   "status": "PENDING"
   * }
   */
  @Post()
  create(@Body() createNotificacionDto: CreateNotificationDto) {
    return this.notificacionService.create(createNotificacionDto);
  }

  /**
   * Retrieves all notifications.
   * @route GET /notification
   * @returns Promise containing an array of all notifications
   */
  @Get()
  findAll() {
    return this.notificacionService.findAll();
  }

  /**
   * Retrieves a specific notification by ID.
   * @route GET /notification/:id
   * @param id - The unique identifier of the notification
   * @returns Promise containing the notification if found
   */
  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.notificacionService.findOne(+id);
  }

  /**
   * Updates an existing notification.
   * @route PATCH /notification/:id
   * @param id - The unique identifier of the notification to update
   * @param updateNotificacionDto - The data transfer object containing updated notification details
   * @returns Promise containing the updated notification
   * @example
   * // Request body
   * {
   *   "status": "READ",
   *   "message": "Updated notification message"
   * }
   */
  @Patch(':id')
  update(@Param('id') id: string, @Body() updateNotificacionDto: UpdateNotificationDto) {
    return this.notificacionService.update(+id, updateNotificacionDto);
  }

  /**
   * Removes a notification.
   * @route DELETE /notification/:id
   * @param id - The unique identifier of the notification to remove
   * @returns Promise containing the deletion result
   */
  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.notificacionService.remove(+id);
  }

  /**
   * Manually sends all pending notifications.
   * This endpoint is useful for testing and debugging.
   * @route POST /notification/send-pending
   * @returns Promise containing the result of sending pending notifications
   */
  @Post('send-pending')
  async sendPendingNotifications() {
    try {
      await this.notificacionService.sendPendingNotifications();
      return { success: true, message: 'Pending notifications sent successfully' };
    } catch (error) {
      return { success: false, message: 'Failed to send pending notifications', error: error.message };
    }
  }

  /**
   * Sends the test notification (existing functionality).
   * @route POST /notification/test
   * @returns Promise containing the result of sending test notification
   */
  @Post('test')
  async sendTestNotification() {
    try {
      await this.notificacionService.sendNotification();
      return { success: true, message: 'Test notification sent successfully' };
    } catch (error) {
      return { success: false, message: 'Failed to send test notification', error: error.message };
    }
  }
}
