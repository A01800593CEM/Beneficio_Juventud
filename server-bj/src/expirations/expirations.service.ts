import { Injectable, Logger } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, LessThanOrEqual, Between } from 'typeorm';

import { Promotion } from '../promotions/entities/promotion.entity';
import { Booking } from '../bookings/entities/booking.entity';
import { Notification } from '../notifications/entities/notification.entity';

import { NotificationType } from '../notifications/enums/notification-type.enums';
import { NotificationStatus } from '../notifications/enums/notification-status.enum';
import { RecipientType } from '../notifications/enums/recipient-type.enums';

import { NotificationsService } from '../notifications/notifications.service';
import { BookStatus } from '../bookings/enums/book-status.enum';

@Injectable()
export class ExpirationsService {
  private readonly logger = new Logger(ExpirationsService.name);

  constructor(
    @InjectRepository(Promotion)
    private readonly promoRepo: Repository<Promotion>,
    @InjectRepository(Booking)
    private readonly bookingRepo: Repository<Booking>,
    @InjectRepository(Notification)
    private readonly notifRepo: Repository<Notification>,
    private readonly notificationsService: NotificationsService,
  ) {}

  // En producción puedes dejarlo cada hora. Para pruebas podrías cambiarlo a EVERY_MINUTE.
  @Cron(CronExpression.EVERY_HOUR)
  async checkExpirations() {
    return this._check({ collectResults: false });
  }

  // Endpoint manual de prueba (/expirations/check)
  async runManualCheck() {
    const result = await this._check({ collectResults: true });
    return result;
  }

  /**
   * Hace la revisión y opcionalmente devuelve detalles de lo creado / saltado.
   */
  private async _check(opts: { collectResults: boolean }) {
    const created: Array<{ id: number; body: string; kind: 'promotion' | 'booking' }> = [];
    const skipped: Array<{ reason: string; kind: 'promotion' | 'booking' }> = [];

    const now = new Date();
    const in24h = new Date(now.getTime() + 24 * 60 * 60 * 1000);

    this.logger.log(
      `🔎 Revisando expiraciones entre ${now.toISOString()} y ${in24h.toISOString()}`,
    );

    // =========================================================
    // 1) Promociones que vencen <= 24h
    //    → Notificar a usuarios con BOOKING PENDING de esa promo
    // =========================================================
    const promosExpiring = await this.promoRepo.find({
      where: { endDate: LessThanOrEqual(in24h) },
    });
    this.logger.log(`🔹 Promociones por expirar: ${promosExpiring.length}`);

    for (const promo of promosExpiring) {
      // Buscar bookings PENDING de esta promo (usuarios relacionados)
      const userBookings = await this.bookingRepo.find({
        where: {
          promotionId: promo.promotionId,
          status: BookStatus.PENDING,
          limitUseDate: Between(now, in24h), // también dentro de 24h si quieres limitar
        },
        relations: ['user'],
      });

      // Set único por usuario.id (podría haber varias reservas de la misma promo)
      const uniqueUsers = new Map<number, { userId: number; name?: string }>();
      for (const b of userBookings) {
        const uid = b.user?.id;
        if (uid != null && !uniqueUsers.has(uid)) {
          uniqueUsers.set(uid, { userId: uid, name: b.user?.name });
        }
      }

      for (const { userId, name } of uniqueUsers.values()) {
        // Deduplicación por recipient + kind + promoId
        const exists = await this.notifRepo
          .createQueryBuilder('n')
          .where(`n."recipientType" = :rt`, { rt: RecipientType.USER })
          .andWhere(`n."recipientId" = :rid`, { rid: userId })
          .andWhere(`n."segmentCriteria"->>'kind' = :kind`, { kind: 'promotion-expiring' })
          .andWhere(`n."segmentCriteria"->>'id' = :pid`, { pid: String(promo.promotionId) })
          .getExists();

        if (exists) {
          if (opts.collectResults) skipped.push({ reason: 'duplicate-promotion', kind: 'promotion' });
          continue;
        }

        const body = `${name ?? 'Tu'}, la promoción "${promo.title}" vence el ${promo.endDate
          .toISOString()
          .slice(0, 10)}.`;

        const saved = await this.notifRepo.save(
          this.notifRepo.create({
            title: 'Promoción por expirar',
            message: body,
            type: NotificationType.ALERT,
            recipientType: RecipientType.USER, // 👈 ahora va a usuario (sí lo envía tu NotificationsService)
            recipientId: userId,
            status: NotificationStatus.PENDING,
            segmentCriteria: { kind: 'promotion-expiring', id: promo.promotionId },
            promotions: promo, // relación
          }),
        );

        this.logger.log(`✅ Notificación creada (promo expiring) para user ${userId} - promo ${promo.promotionId}`);
        if (opts.collectResults) {
          created.push({ id: saved.notificationId, body, kind: 'promotion' });
        }
      }
    }

    // =========================================================
    // 2) Auto-cancelar bookings ya expirados (limitUseDate < now)
    // =========================================================
    const expiredBookings = await this.bookingRepo.find({
      where: {
        status: BookStatus.PENDING,
        limitUseDate: LessThanOrEqual(now),
      },
    });

    if (expiredBookings.length > 0) {
      this.logger.log(`🔴 Auto-cancelando ${expiredBookings.length} reservas expiradas`);
      for (const booking of expiredBookings) {
        booking.status = BookStatus.CANCELLED;
        booking.cancelledDate = now;
        await this.bookingRepo.save(booking);
      }
    }

    // =========================================================
    // 3) Bookings que vencen en <= 24h (SOLO PENDING)
    // =========================================================
    const bookingsExpiring = await this.bookingRepo.find({
      where: {
        status: BookStatus.PENDING,              // 👈 evitar usadas/canceladas
        limitUseDate: Between(now, in24h),       // 👈 evitar ya vencidas
      },
      relations: ['promotion', 'user'],
    });
    this.logger.log(`🔹 Cupones por expirar: ${bookingsExpiring.length}`);

    for (const book of bookingsExpiring) {
      const userName = book.user?.name ?? 'Tu';
      const recipientIdNum = book.user?.id ?? null;

      if (!book.promotion || recipientIdNum === null) {
        if (opts.collectResults) {
          skipped.push({ reason: 'missing-promo-or-recipient', kind: 'booking' });
        }
        continue;
      }

      // Deduplicación por recipient + kind + bookingId
      const exists = await this.notifRepo
        .createQueryBuilder('n')
        .where(`n."recipientType" = :rt`, { rt: RecipientType.USER })
        .andWhere(`n."recipientId" = :rid`, { rid: recipientIdNum })
        .andWhere(`n."segmentCriteria"->>'kind' = :kind`, { kind: 'booking-expiring' })
        .andWhere(`n."segmentCriteria"->>'id' = :bid`, { bid: String(book.bookingId) })
        .getExists();
      if (exists) {
        if (opts.collectResults) skipped.push({ reason: 'duplicate-booking', kind: 'booking' });
        continue;
      }

      const body = `${userName}, tu cupón de "${book.promotion.title}" está por expirar. ¡Canjéalo ahora! ${
        book.limitUseDate ? `(vence el ${book.limitUseDate.toISOString().slice(0, 10)})` : ''
      }`;

      const saved = await this.notifRepo.save(
        this.notifRepo.create({
          title: 'Cupón por expirar',
          message: body,
          type: NotificationType.ALERT,
          recipientType: RecipientType.USER,
          recipientId: recipientIdNum,
          status: NotificationStatus.PENDING,
          segmentCriteria: { kind: 'booking-expiring', id: book.bookingId },
          promotions: book.promotion,
        }),
      );

      this.logger.log(`✅ Notificación creada (booking expiring) para user ${recipientIdNum} - booking ${book.bookingId}`);
      if (opts.collectResults) {
        created.push({ id: saved.notificationId, body, kind: 'booking' });
      }
    }

    this.logger.log('🎯 Revisión completada.');

    // Enviar notificaciones push para todas las notificaciones pendientes
    try {
      await this.notificationsService.sendPendingNotifications();
    } catch (error) {
      this.logger.error('❌ Error sending pending notifications:', error);
    }

    // Respuesta detallada solo para el endpoint manual
    if (opts.collectResults) {
      return {
        ok: true,
        created, // [{ id, body, kind }]
        skipped, // [{ reason, kind }]
        totals: {
          promotionsFound: promosExpiring.length,
          bookingsFound: bookingsExpiring.length,
          created: created.length,
          skipped: skipped.length,
        },
        message: 'Manual expiration check executed.',
      };
    }

    // En el cron no necesitamos payload de salida
    return;
  }
}
