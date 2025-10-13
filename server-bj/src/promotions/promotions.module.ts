import { forwardRef, Module } from '@nestjs/common';
import { PromotionsService } from './promotions.service';
import { PromotionsController } from './promotions.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Promotion } from './entities/promotion.entity';
import { BookingsModule } from 'src/bookings/bookings.module';
import { CategoriesModule } from 'src/categories/categories.module';
import { NotificacionsModule } from 'src/notifications/notifications.module';

@Module({
  controllers: [PromotionsController],
  providers: [PromotionsService],
  imports: [
    TypeOrmModule.forFeature([Promotion]),
    forwardRef(() => PromotionsModule),
    forwardRef(() => BookingsModule),
    forwardRef(() => CategoriesModule),
    NotificacionsModule
],
exports: [PromotionsService],
})
export class PromotionsModule {}
