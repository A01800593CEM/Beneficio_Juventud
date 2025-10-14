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

    // 1) Promos que vencen en <= 24h
    const promosExpiring = await this.promoRepo.find({
      where: { endDate: LessThanOrEqual(in24h) },
    });

    for (const promo of promosExpiring) {
      const recipientIdNum = Number(promo.collaboratorId); // <-- asegurar número

      const exists = await this.notifRepo.findOne({
        where: {
          title: 'Promoción por expirar',
          recipientId: recipientIdNum,
          // OJO: usa 'promotions' o 'promotion' según tu Notification entity
          promotions: { promotionId: promo.promotionId },
        },
      });
      if (exists) continue;

      await this.notifRepo.save(
        this.notifRepo.create({
          title: 'Promoción por expirar',
          message: `La promoción "${promo.title}" vence el ${promo.endDate.toISOString().slice(0, 10)}.`,
          type: NotificationType.ALERT,
          recipientType: RecipientType.COLLABORATOR,
          recipientId: recipientIdNum,
          status: NotificationStatus.PENDING,
          segmentCriteria: { kind: 'promotion-expiring', id: promo.promotionId },
          // OJO: usa 'promotions' o 'promotion' según tu Notification entity
          promotions: promo,
        }),
      );
    }

    // 2) Bookings que vencen en <= 24h
    const bookingsExpiring = await this.bookingRepo.find({
      where: { limitUseDate: LessThanOrEqual(in24h) },
      relations: ['promotion'], // sin 'user'
    });

    for (const book of bookingsExpiring) {
      const recipientIdNum = Number(book.userId); // <-- asegurar número
      if (!recipientIdNum) continue;

      // Asegura que venga la promo (por si alguna fila quedó sin FK)
      if (!book.promotion) continue;

      const exists = await this.notifRepo.findOne({
        where: {
          title: 'Cupón por expirar',
          recipientId: recipientIdNum,
          // OJO: usa 'promotions' o 'promotion' según tu Notification entity
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
          recipientId: recipientIdNum,
          status: NotificationStatus.PENDING,
          segmentCriteria: { kind: 'booking-expiring', id: book.bookingId },
          // OJO: usa 'promotions' o 'promotion' según tu Notification entity
          promotions: book.promotion,
        }),
      );
    }

    this.logger.log('✅ Revisión completada');
  }

  // Endpoint manual de prueba
  async runManualCheck() {
    await this.checkExpirations();
    return { ok: true, message: 'Manual expiration check executed.' };
  }
}
