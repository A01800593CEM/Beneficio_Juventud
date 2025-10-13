import { forwardRef, Module } from '@nestjs/common';
import { NotificationsService } from './notifications.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PromotionsModule } from 'src/promotions/promotions.module';
import admin from "firebase-admin";

@Module({
  providers: [NotificationsService],
  imports: [
    TypeOrmModule.forFeature([Notification]),
    forwardRef(() => PromotionsModule),
  ],
  exports: [NotificationsService]
})
export class NotificacionsModule {}
