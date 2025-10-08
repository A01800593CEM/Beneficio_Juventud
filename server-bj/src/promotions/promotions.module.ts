import { forwardRef, Module } from '@nestjs/common';
import { PromotionsService } from './promotions.service';
import { PromotionsController } from './promotions.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Promotion } from './entities/promotion.entity';
import { BookingsModule } from 'src/bookings/bookings.module';

@Module({
  controllers: [PromotionsController],
  providers: [PromotionsService],
  exports: [PromotionsService],
  imports: [TypeOrmModule.forFeature([Promotion]),
  forwardRef(() => PromotionsModule),
  forwardRef(() => BookingsModule)],
})
export class PromotionsModule {}
