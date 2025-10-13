import { Injectable, Logger } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { Repository, LessThanOrEqual } from 'typeorm';
import { InjectRepository } from '@nestjs/typeorm';
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

  // 🕒 Ejecuta cada hora
  @Cron(CronExpression.EVERY_HOUR)
  async checkExpirations() {
    this.logger.log('🔍 Revisando promociones y cupones próximos a expirar...');

    const now = new Date();
    const in24h = new Date(now.getTime() + 24 * 60 * 60 * 1000);

    // 1️⃣ Promociones que vencen en menos de 24h
    const promosExpiring = await this.promoRepo.find({
      where: { endDate: LessThanOrEqual(in24h) },
    });

    for (const promo of promosExpiring) {
      const exists = await this.notifRepo.findOne({
        where: {
          title: 'Promoción por expirar',
          recipientId: promo.collaboratorId,
          promotions: { promotionId: promo.promotionId },
        },
      });
      if (exists) continue; // evita duplicados

      await this.notifRepo.save(
        this.notifRepo.create({
          title: 'Promoción por expirar',
          message: `La promoción "${promo.title}" vence el ${promo.endDate.toISOString().slice(0, 10)}.`,
          type: NotificationType.ALERT,
          recipientType: RecipientType.COLLABORATOR,
          recipientId: promo.collaboratorId,
          status: NotificationStatus.PENDING,
          segmentCriteria: { kind: 'promotion-expiring', id: promo.promotionId },
          promotions: promo,
        }),
      );
    }

    // 2️⃣ Cupones (Bookings) que vencen en menos de 24h
    const bookingsExpiring = await this.bookingRepo.find({
      where: { limitUseDate: LessThanOrEqual(in24h) },
      relations: ['promotion'], // 👈 sin 'user' para evitar el join problemático
    });

    for (const book of bookingsExpiring) {
      const recipientId = book.userId; // 👈 usamos la FK cruda
      if (!recipientId) continue;

      const exists = await this.notifRepo.findOne({
        where: {
          title: 'Cupón por expirar',
          recipientId,
          promotions: { promotionId: book.promotion.promotionId },
        },
      });
      if (exists) continue;

      await this.notifRepo.save(
        this.notifRepo.create({
          title: 'Cupón por expirar',
          message: `Tu cupón de "${book.promotion.title}" vence el ${
            book.limitUseDate ? book.limitUseDate.toISOString().slice(0, 10) : 'pronto'
          }.`,
          type: NotificationType.ALERT,
          recipientType: RecipientType.USER,
          recipientId,
          status: NotificationStatus.PENDING,
          segmentCriteria: { kind: 'booking-expiring', id: book.bookingId },
          promotions: book.promotion,
        }),
      );
    }

    this.logger.log('✅ Revisión completada');
  }

  //Tests
  async runManualCheck() {
    await this.checkExpirations();
    return { ok: true, message: 'Manual expiration check executed.' };
  }
}
