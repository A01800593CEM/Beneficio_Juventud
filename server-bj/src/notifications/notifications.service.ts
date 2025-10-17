import { Inject, Injectable, NotFoundException, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import admin from "firebase-admin";
import { Promotion } from 'src/promotions/entities/promotion.entity';
import { User } from 'src/users/entities/user.entity';
import { Favorite } from 'src/favorites/entities/favorite.entity';
import { Notification } from './entities/notification.entity';
import { NotificationType } from './enums/notification-type.enums';
import { NotificationStatus } from './enums/notification-status.enum';
import { RecipientType } from './enums/recipient-type.enums';


const token = "d6pkRCOgTVCX4CezhHXNvh:APA91bFtNzHwDJOhmWdU6ZaTiyC5bce3H00knep7lcGgn5qTprmBtsGJamSGwmQmLWuq4utjMaEwFyNmimJuEKMCfoy36FIaRbJE7kEx4z-GZhMgndcJVxg";

@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name);

  constructor(
    @Inject('FIREBASE_ADMIN') private admin: admin.app.App,
    @InjectRepository(User)
    private readonly userRepository: Repository<User>,
    @InjectRepository(Favorite)
    private readonly favoriteRepository: Repository<Favorite>,
    @InjectRepository(Notification)
    private readonly notificationRepository: Repository<Notification>,
  ) {}

  async newPromoNotif(newPromo: Promotion) {
    try {
      this.logger.log(`🔔 Sending notifications for new promotion: ${newPromo.title}`);

      // 1. Enviar al topic general del colaborador (mantener funcionalidad existente)
      await admin.messaging().send({
        topic: newPromo.collaboratorId,
        notification: {
          title: newPromo.title,
          body: newPromo.description
        },
        data: {
          promotion: String(newPromo.promotionId),
          action: 'openPromoDetail'
        }
      });

      // 2. Buscar usuarios que tienen este colaborador como favorito
      const usersWithFavorite = await this.favoriteRepository.find({
        where: { collaboratorId: newPromo.collaboratorId },
        relations: ['user']
      });

      this.logger.log(`👥 Found ${usersWithFavorite.length} users with this collaborator as favorite`);

      // 3. Enviar notificaciones personalizadas a cada usuario
      for (const favorite of usersWithFavorite) {
        if (favorite.user?.notificationToken) {
          try {
            // Enviar push notification
            await admin.messaging().send({
              token: favorite.user.notificationToken,
              notification: {
                title: `🎉 Nueva promoción de tu favorito`,
                body: `${newPromo.title} - ${newPromo.description}`
              },
              data: {
                promotion: String(newPromo.promotionId),
                collaborator: newPromo.collaboratorId,
                action: 'openPromoDetail'
              }
            });

            // Guardar registro en BD
            await this.notificationRepository.save(
              this.notificationRepository.create({
                title: '🎉 Nueva promoción de tu favorito',
                message: `${newPromo.title} - ${newPromo.description}`,
                type: NotificationType.PROMOTION,
                recipientType: RecipientType.USER,
                recipientId: favorite.user.id,
                status: NotificationStatus.PENDING,
                segmentCriteria: { kind: 'favorite-promotion', collaboratorId: newPromo.collaboratorId },
                promotions: newPromo,
              })
            );

            this.logger.log(`✅ Notification sent to user ${favorite.user.id}`);
          } catch (error) {
            this.logger.error(`❌ Failed to send notification to user ${favorite.user.id}:`, error);
          }
        }
      }

      this.logger.log(`🎯 Promotion notifications completed`);
    } catch (error) {
      this.logger.error('❌ Error in newPromoNotif:', error);
      throw error;
    }
  }

  async sendNotification() {
    await admin.messaging().send({
        token: token,
        notification: {
            title: '¡Hola!',
            body: 'Esta es una notificación de prueba desde Javascript.'
        },
        data: { // Opcional: datos personalizados
            key1: 'value1',
            key2: 'value2'
        }
    });
    console.log("Termina el envío");

    // Tema
    await admin.messaging().send({
      topic: 'TarjetaJoven',
      notification: {
        title: '¡Noticia del día!',
        body: '¡Mira lo nuevo en nuestras promociones!'
      },
      data: { // Opcional: datos personalizados
        key1: 'Ropa y accesorios para la familia',
        key2: 'value2'
      }
    });
}


  /**
   * Envía notificaciones push basadas en registros de notificación pendientes
   * Usado principalmente por el sistema de expiraciones
   */
  async sendPendingNotifications() {
    try {
      this.logger.log('🔍 Looking for pending notifications to send...');

      const pendingNotifications = await this.notificationRepository.find({
        where: { status: NotificationStatus.PENDING },
        relations: ['promotions']
      });

      this.logger.log(`📋 Found ${pendingNotifications.length} pending notifications`);

      for (const notification of pendingNotifications) {
        try {
          // Buscar el usuario destinatario (solo si recipientId existe)
          if (!notification.recipientId) continue;

          const user = await this.userRepository.findOne({
            where: { id: notification.recipientId }
          });

          if (user?.notificationToken) {
            // Enviar push notification
            await admin.messaging().send({
              token: user.notificationToken,
              notification: {
                title: notification.title,
                body: notification.message
              },
              data: {
                notificationId: String(notification.notificationId),
                promotion: notification.promotions ? String(notification.promotions.promotionId) : '',
                action: 'openNotification'
              }
            });

            // Marcar como enviada
            notification.status = NotificationStatus.SENT;
            await this.notificationRepository.save(notification);

            this.logger.log(`✅ Push notification sent for notification ID ${notification.notificationId}`);
          } else {
            this.logger.warn(`⚠️ User ${notification.recipientId} has no notification token`);
          }
        } catch (error) {
          this.logger.error(`❌ Failed to send notification ID ${notification.notificationId}:`, error);
        }
      }

      this.logger.log('🎯 Pending notifications processing completed');
    } catch (error) {
      this.logger.error('❌ Error in sendPendingNotifications:', error);
      throw error;
    }
  }

  /**
   * Envía una notificación push específica a un usuario
   */
  async sendPushNotification(
    userToken: string,
    title: string,
    body: string,
    data: Record<string, string> = {}
  ) {
    try {
      await admin.messaging().send({
        token: userToken,
        notification: { title, body },
        data
      });

      this.logger.log(`✅ Push notification sent: ${title}`);
    } catch (error) {
      this.logger.error(`❌ Failed to send push notification: ${title}`, error);
      throw error;
    }
  }
}

//sendNotification().catch(console.error);
