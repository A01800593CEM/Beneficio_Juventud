import { Module } from '@nestjs/common';
import { ExpirationsService } from './expirations.service';
import { ExpirationsController } from './expirations.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Promotion } from '../promotions/entities/promotion.entity';
import { Booking } from '../bookings/entities/booking.entity';
import { Notification } from '../notifications/entities/notification.entity';
import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([Promotion, Booking, Notification]),
    NotificationsModule
  ],
  providers: [ExpirationsService],
  controllers: [ExpirationsController],
})
export class ExpirationsModule {}
