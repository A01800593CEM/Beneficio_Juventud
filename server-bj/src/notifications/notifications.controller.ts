import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { NotificationsService } from './notifications.service';

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
