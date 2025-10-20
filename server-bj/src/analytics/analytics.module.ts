import { Module, forwardRef } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AnalyticsService } from './analytics.service';
import { AnalyticsController } from './analytics.controller';
import { Promotion } from '../promotions/entities/promotion.entity';
import { Booking } from '../bookings/entities/booking.entity';
import { Redeemedcoupon } from '../redeemedcoupon/entities/redeemedcoupon.entity';
import { Collaborator } from '../collaborators/entities/collaborator.entity';
import { Favorite } from '../favorites/entities/favorite.entity';
import { User } from '../users/entities/user.entity';

/**
 * Analytics module for handling statistics and reporting.
 * Provides endpoints optimized for Vico (Android) and Recharts (Web).
 */
@Module({
  imports: [
    TypeOrmModule.forFeature([
      Promotion,
      Booking,
      Redeemedcoupon,
      Collaborator,
      Favorite,
      User,
    ]),
  ],
  controllers: [AnalyticsController],
  providers: [AnalyticsService],
  exports: [AnalyticsService],
})
export class AnalyticsModule {}