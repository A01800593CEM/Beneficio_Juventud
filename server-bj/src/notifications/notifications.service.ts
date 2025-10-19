import { Inject, Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, Between } from 'typeorm';
import admin from 'firebase-admin';

import { Promotion } from 'src/promotions/entities/promotion.entity';
import { User } from 'src/users/entities/user.entity';
import { Favorite } from 'src/favorites/entities/favorite.entity';
import { Notification } from './entities/notification.entity';

import { NotificationType } from './enums/notification-type.enums';
import { NotificationStatus } from './enums/notification-status.enum';
import { RecipientType } from './enums/recipient-type.enums';

import { Booking } from 'src/bookings/entities/booking.entity';
import { BookStatus } from 'src/bookings/enums/book-status.enum';

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

    @InjectRepository(Booking)
    private readonly bookingRepository: Repository<Booking>,
  ) {}

  // =========================
  // 🔔 Promoción nueva
  // =========================
  async newPromoNotif(newPromo: Promotion) {
    try {
      this.logger.log(`🔔 Sending notifications for new promotion: ${newPromo.title}`);

      // 1. Enviar al topic general del colaborador
      await admin.messaging().send({
        topic: newPromo.collaboratorId,
        notification: {
          title: newPromo.title,
          body: newPromo.description,
        },
        data: {
          promotion: String(newPromo.promotionId),
          action: 'openPromoDetail',
        },
      });

      // 2. Buscar usuarios que tienen este colaborador como favorito
      const usersWithFavorite = await this.favoriteRepository.find({
        where: { collaboratorId: newPromo.collaboratorId },
        relations: ['user'],
      });

      this.logger.log(`👥 Found ${usersWithFavorite.length} users with this collaborator as favorite`);

      // 3. Enviar notificaciones personalizadas a cada usuario
      for (const favorite of usersWithFavorite) {
        if (favorite.user?.notificationToken) {
          try {
            await admin.messaging().send({
              token: favorite.user.notificationToken,
              notification: {
                title: `🎉 Nueva promoción de tu favorito`,
                body: `${newPromo.title} - ${newPromo.description}`,
              },
              data: {
                promotion: String(newPromo.promotionId),
                collaborator: newPromo.collaboratorId,
                action: 'openPromoDetail',
              },
            });

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
              }),
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

  // =========================
  // 🧩 Función de prueba basada en tu código original
  // =========================
  async sendNotification() {
    this.logger.log('🔍 Buscando primer usuario con token_notificacion...');

    // 1️⃣ Buscar el primer usuario con token_notificacion no nulo
    const user = await this.userRepository
      .createQueryBuilder('u')
      .where('u.token_notificacion IS NOT NULL')
      .orderBy('u.usuario_id', 'ASC')
      .getOne();

    if (!user?.notificationToken) {
      this.logger.warn('⚠️ No se encontró ningún usuario con token_notificacion');
      return;
    }

    this.logger.log(`📱 Se usará el token del usuario ID=${user.id}`);

    // 2️⃣ Envío directo a su token (igual que tu versión original)
    await admin.messaging().send({
      token: user.notificationToken,
      notification: {
        title: '¡Hola!',
        body: 'Esta es una notificación de prueba desde el servidor NestJS.',
      },
      data: {
        key1: 'value1',
        key2: 'value2',
      },
    });

    this.logger.log('✅ Notificación enviada por token');
    console.log('Termina el envío');

    // 3️⃣ Envío a un tópico (manteniendo tu comportamiento original)
    await admin.messaging().send({
      topic: 'TarjetaJoven',
      notification: {
        title: '¡Noticia del día!',
        body: '¡Mira lo nuevo en nuestras promociones!',
      },
      data: {
        key1: 'Ropa y accesorios para la familia',
        key2: 'value2',
      },
    });

    this.logger.log('✅ Notificación enviada al tópico TarjetaJoven');
  }

  // =========================
  // 📨 Envío de pendientes (BD → FCM)
  // =========================
  async sendPendingNotifications() {
    try {
      this.logger.log('🔍 Looking for pending notifications to send...');

      const pendingNotifications = await this.notificationRepository.find({
        where: { status: NotificationStatus.PENDING },
        relations: ['promotions'],
      });

      this.logger.log(`📋 Found ${pendingNotifications.length} pending notifications`);

      for (const notification of pendingNotifications) {
        try {
          if (!notification.recipientId) continue;

          const user = await this.userRepository.findOne({
            where: { id: notification.recipientId },
          });

          if (user?.notificationToken) {
            await admin.messaging().send({
              token: user.notificationToken,
              notification: {
                title: notification.title,
                body: notification.message,
              },
              data: {
                notificationId: String(notification.notificationId),
                promotion: notification.promotions ? String(notification.promotions.promotionId) : '',
                action: 'openNotification',
              },
            });

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

  // =========================
  // ⏳ Escaneo de reservas por expirar (crea PENDING)
  // =========================
  async checkExpiringBookings(hoursBefore = 24) {
    try {
      this.logger.log(`⏰ Buscando reservas que expiran en las próximas ${hoursBefore} h...`);

      const now = new Date();
      const limit = new Date(now.getTime() + hoursBefore * 60 * 60 * 1000);

      const expiring = await this.bookingRepository.find({
        where: {
          status: BookStatus.PENDING,
          limitUseDate: Between(now, limit),
        },
        relations: ['user', 'promotion'],
      });

      this.logger.log(`📋 Encontradas ${expiring.length} reservas próximas a expirar`);

      for (const booking of expiring) {
        const user = booking.user;
        if (!user?.notificationToken) continue;

        const title = '⏳ Tu reservación está por expirar';
        const body = booking.promotion
          ? `La promoción "${booking.promotion.title}" vence el ${new Date(booking.limitUseDate!).toLocaleString()}`
          : `Tu reservación vence el ${new Date(booking.limitUseDate!).toLocaleString()}`;

        await this.notificationRepository.save(
          this.notificationRepository.create({
            title,
            message: body,
            type: NotificationType.ALERT,
            recipientType: RecipientType.USER,
            recipientId: user.id,
            status: NotificationStatus.PENDING,
            segmentCriteria: {
              kind: 'booking-expiration',
              bookingId: booking.bookingId,
              limitUseDate: booking.limitUseDate?.toISOString() ?? '',
            },
          }),
        );

        this.logger.log(`📝 Notificación creada para reserva ${booking.bookingId}`);
      }

      this.logger.log('✅ checkExpiringBookings() completado');
    } catch (error) {
      this.logger.error('❌ Error en checkExpiringBookings():', error);
      throw error;
    }
  }
}
