import { Module, forwardRef } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UsersService } from './users.service';
import { UsersController } from './users.controller';
import { User } from './entities/user.entity';
import { BookingsModule } from '../bookings/bookings.module';
import { PromotionsModule } from 'src/promotions/promotions.module';
import { FavoritesModule } from 'src/favorites/favorites.module';

/**
 * Users module that encapsulates all logic related to user management.
 *
 * @remarks
 * This module handles creation, retrieval, and modification of users,
 * and exposes REST endpoints through the {@link UsersController}.
 * 
 * It also defines dependencies with other modules that interact with users:
 * - {@link BookingsModule}: to access user reservations.
 * - {@link PromotionsModule}: to link users with promotions and redemptions.
 * - {@link FavoritesModule}: to manage favorite promotions or collaborators.
 *
 * @example
 * ```ts
 * @Module({
 *   imports: [UsersModule],
 * })
 * export class AppModule {}
 * ```
 */
@Module({
  imports: [TypeOrmModule.forFeature([User]),
  forwardRef(() => BookingsModule),
  forwardRef(() => PromotionsModule),
  forwardRef(() => FavoritesModule)], 
  controllers: [UsersController],
  providers: [UsersService],
  exports:[TypeOrmModule, UsersService]
})
export class UsersModule {}
