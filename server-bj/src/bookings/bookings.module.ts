import { forwardRef, Module } from '@nestjs/common';
import { BookingsService } from './bookings.service';
import { BookingsController } from './bookings.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Booking } from './entities/booking.entity';
import { UsersModule } from '../users/users.module';
import { PromotionsModule } from '../promotions/promotions.module';

/**
 * Module responsible for managing bookings in the application.
 * Handles the registration of controllers, services, and dependencies related to bookings.
 * 
 * @module BookingsModule
 * @description
 * This module includes:
 * - BookingsController for handling HTTP requests
 * - BookingsService for business logic
 * - TypeORM integration for Booking entity
 * - Circular dependencies with Users and Promotions modules
 */
@Module({
  controllers: [BookingsController],
  providers: [BookingsService],
  imports: [
    TypeOrmModule.forFeature([Booking]),
    forwardRef(() => UsersModule),      // Resolves circular dependency with Users
    forwardRef(() => PromotionsModule), // Resolves circular dependency with Promotions
  ],
  exports: [BookingsService], // Makes BookingsService available to other modules
})
export class BookingsModule {}
