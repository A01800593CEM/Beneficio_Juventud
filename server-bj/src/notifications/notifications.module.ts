import { forwardRef, Module } from '@nestjs/common';
import { NotificationsService } from './notifications.service';
import { NotificationsController } from './notifications.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PromotionsModule } from 'src/promotions/promotions.module';
import { Notification } from './entities/notification.entity';

/**
 * NestJS module that encapsulates notifications domain logic.
 *
 * @remarks
 * - **Controllers**: Exposes REST endpoints via {@link NotificationsController}.
 * - **Providers**: Business logic in {@link NotificationsService}.
 * - **Imports**:
 *   - `TypeOrmModule.forFeature([Notification])` registers the repository for the `Notification` entity
 *     in this module’s DI scope.
 *   - `forwardRef(() => PromotionsModule)` resolves a circular dependency (when both modules
 *     inject each other’s services).
 * - **Exports**: Re-exports {@link NotificationsService} for consumption by other modules.
 *
 * If there is no actual circular dependency with `PromotionsModule`, you can replace `forwardRef`
 * with a direct import.
 */

@Module({
  controllers: [NotificationsController],
  providers: [NotificationsService],
  imports: [
    TypeOrmModule.forFeature([Notification]),
    forwardRef(() => PromotionsModule),
  ],
  exports: [NotificationsService]
})
export class NotificationsModule {}
