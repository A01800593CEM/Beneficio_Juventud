import { forwardRef, Module } from '@nestjs/common';
import { NotificationsService } from './notifications.service';
import { NotificationsController } from './notifications.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PromotionsModule } from 'src/promotions/promotions.module';
import { Notification } from './entities/notification.entity';

@Module({
  controllers: [NotificationsController],
  providers: [NotificationsService],
  imports: [
    TypeOrmModule.forFeature([Notification]),
    forwardRef(() => PromotionsModule),
  ],
  exports: [NotificationsService]
})
export class NotificacionsModule {}
