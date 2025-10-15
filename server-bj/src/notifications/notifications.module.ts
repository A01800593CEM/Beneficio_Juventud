import { forwardRef, Global, Module } from '@nestjs/common';
import { NotificationsService } from './notifications.service';
import { TypeOrmModule } from '@nestjs/typeorm';
import { PromotionsModule } from 'src/promotions/promotions.module';
import admin from "firebase-admin";
import { Promotion } from 'src/promotions/entities/promotion.entity';
const serviceAccount = require("../../beneficio-joven-firebase-adminsdk-fbsvc-0852e25dec.json")

const firebaseApp = admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://beneficio-joven-default-rtdb.firebaseio.com"
  });

@Global()
@Module({
  providers: [
    {
      provide: 'FIREBASE_ADMIN',
      useValue: firebaseApp,
    }, 
  NotificationsService],
  exports: [NotificationsService, 'FIREBASE_ADMIN'],
  imports: [TypeOrmModule.forFeature([Promotion]), forwardRef(() => PromotionsModule)],
})
export class NotificationsModule {}
