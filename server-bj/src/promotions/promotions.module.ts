import { forwardRef, Module } from '@nestjs/common';
import { PromotionsService } from './promotions.service';
import { PromotionsController } from './promotions.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Promotion } from './entities/promotion.entity';
import { BookingsModule } from 'src/bookings/bookings.module';
import { CategoriesModule } from 'src/categories/categories.module';
import { NotificationsModule } from 'src/notifications/notifications.module';

/**
 * Promotions module responsible for handling all promotion-related functionality.
 *
 * @remarks
 * This module encapsulates all business logic, data access, and REST endpoints
 * related to the `Promotion` entity.
 *
 * It also integrates with other modules through `forwardRef()` to resolve
 * circular dependencies, enabling mutual injection (e.g., between promotions and bookings).
 *
 * @example
 * Import this module into the application’s root module:
 * ```ts
 * @Module({
 *   imports: [PromotionsModule],
 * })
 * export class AppModule {}
 * ```
 */
@Module({
  controllers: [PromotionsController],
  providers: [PromotionsService],
  imports: [
    TypeOrmModule.forFeature([Promotion]),
    forwardRef(() => PromotionsModule),
    forwardRef(() => BookingsModule),
    forwardRef(() => CategoriesModule),
    NotificationsModule
],
exports: [PromotionsService],
})
export class PromotionsModule {}
