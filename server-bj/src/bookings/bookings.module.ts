import { forwardRef, Module } from '@nestjs/common';
import { BookingsService } from './bookings.service';
import { BookingsController } from './bookings.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Booking } from './entities/booking.entity';
import { UsersModule } from '../users/users.module';
import { PromotionsModule } from '../promotions/promotions.module';

@Module({
  controllers: [BookingsController],
  providers: [BookingsService],
  imports: [
    TypeOrmModule.forFeature([Booking]),
    forwardRef(() => UsersModule),
    forwardRef(() => PromotionsModule),
  ],

  exports: [BookingsService],
})
export class BookingsModule {}
