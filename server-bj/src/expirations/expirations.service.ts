import { Injectable, Logger } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, LessThanOrEqual } from 'typeorm';
import { Promotion } from '../promotions/entities/promotion.entity';
import { Booking } from '../bookings/entities/booking.entity';
import { Notification } from '../notifications/entities/notification.entity';
import { NotificationType } from '../notifications/enums/notification-type.enums';
import { NotificationStatus } from '../notifications/enums/notification-status.enum';
import { RecipientType } from '../notifications/enums/recipient-type.enums';

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
  ) {}

  // ⚠️ Para pruebas: cada minuto. En producción vuelve a: CronExpression.EVERY_HOUR
  @Cron(CronExpression.EVERY_HOUR)
  async checkExpirations() {
    return this._check({ collectResults: false });
  }

  // Endpoint manual de prueba
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

    // 1) Promos que vencen <= 24h
    const promosExpiring = await this.promoRepo.find({
      where: { endDate: LessThanOrEqual(in24h) },
    });
    this.logger.log(`🔹 Promociones encontradas por expirar: ${promosExpiring.length}`);

    for (const promo of promosExpiring) {
      // recipientId para colaborador: tu esquema dice INT,
      // pero en tus datos aparece string (ej: "us-east-1:...").
      // Si no es numérico, lo dejamos en null para no romper el INSERT.
      const recipientIdNum =
        typeof promo.collaboratorId === 'number' ? promo.collaboratorId : null;

      const exists = await this.notifRepo.findOne({
        where: {
          title: 'Promoción por expirar',
          recipientId: recipientIdNum ?? undefined,
          promotions: { promotionId: promo.promotionId },
        },
      });
      if (exists) {
        if (opts.collectResults) {
          skipped.push({ reason: 'duplicate-promotion', kind: 'promotion' });
        }
        continue;
      }

      const body = `La promoción "${promo.title}" vence el ${promo.endDate
        .toISOString()
        .slice(0, 10)}.`;

      const saved = await this.notifRepo.save(
        this.notifRepo.create({
          title: 'Promoción por expirar',
          message: body,
          type: NotificationType.ALERT,
          recipientType: RecipientType.COLLABORATOR,
          recipientId: recipientIdNum, // puede ser null si no es numérico
          status: NotificationStatus.PENDING,
          segmentCriteria: { kind: 'promotion-expiring', id: promo.promotionId },
          promotions: promo, // relación
        }),
      );

      this.logger.log(`✅ Notificación creada para promo ID ${promo.promotionId}`);
      if (opts.collectResults) {
        created.push({ id: saved.notificationId, body, kind: 'promotion' });
      }
    }

    // 2) Bookings que vencen <= 24h
    // Ya puedes traer la relación 'user' porque tu FK va a cognitoId (varchar)
    const bookingsExpiring = await this.bookingRepo.find({
      where: { limitUseDate: LessThanOrEqual(in24h) },
      relations: ['promotion', 'user'],
    });
    this.logger.log(`🔹 Cupones encontrados por expirar: ${bookingsExpiring.length}`);

    for (const book of bookingsExpiring) {
      // personalizar con el nombre del usuario (si existe)
      const userName = book.user?.name ?? 'Tu';
      const recipientIdNum = book.user?.id ?? null; // INT de la tabla usuario

      // si no hay promoción o recipient numérico, saltamos de forma segura
      if (!book.promotion || recipientIdNum === null) {
        if (opts.collectResults) {
          skipped.push({ reason: 'missing-promo-or-recipient', kind: 'booking' });
        }
        continue;
      }

      const exists = await this.notifRepo.findOne({
        where: {
          title: 'Cupón por expirar',
          recipientId: recipientIdNum,
          promotions: { promotionId: book.promotion.promotionId },
        },
      });
      if (exists) {
        if (opts.collectResults) {
          skipped.push({ reason: 'duplicate-booking', kind: 'booking' });
        }
        continue;
      }

      const body = `${userName}, tu cupón de "${book.promotion.title}" está por expirar. ¡Canjéalo ahora! ${
        book.limitUseDate
          ? `(vence el ${book.limitUseDate.toISOString().slice(0, 10)})`
          : ''
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

      this.logger.log(`✅ Notificación creada para booking ID ${book.bookingId}`);
      if (opts.collectResults) {
        created.push({ id: saved.notificationId, body, kind: 'booking' });
      }
    }

    this.logger.log('🎯 Revisión completada.');

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
