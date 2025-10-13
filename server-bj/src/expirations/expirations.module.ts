import { Module } from '@nestjs/common';
import { ExpirationsService } from './expirations.service';
import { ExpirationsController } from './expirations.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Promotion } from '../promotions/entities/promotion.entity';
import { Booking } from '../bookings/entities/booking.entity';
import { Notification } from '../notifications/entities/notification.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Promotion, Booking, Notification])],
  providers: [ExpirationsService],
  controllers: [ExpirationsController], // 👈 importante
})
export class ExpirationsModule {}
