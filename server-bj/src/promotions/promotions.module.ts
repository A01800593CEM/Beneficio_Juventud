import { forwardRef, Module } from '@nestjs/common';
import { PromotionsService } from './promotions.service';
import { PromotionsController } from './promotions.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Promotion } from './entities/promotion.entity';
import { BookingsModule } from 'src/bookings/bookings.module';
import { CategoriesModule } from 'src/categories/categories.module';

@Module({
  controllers: [PromotionsController],
  providers: [PromotionsService],
  imports: [
    TypeOrmModule.forFeature([Promotion]),
    forwardRef(() => PromotionsModule),
    forwardRef(() => BookingsModule),
    forwardRef(() => CategoriesModule)
],
exports: [PromotionsService],
})
export class PromotionsModule {}
