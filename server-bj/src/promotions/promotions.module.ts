import { Module } from '@nestjs/common';
import { PromotionsService } from './promotions.service';
import { PromotionsController } from './promotions.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Promotion } from './entities/promotion.entity';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';
import { BookingsModule } from 'src/bookings/bookings.module';

@Module({
  controllers: [PromotionsController],
  providers: [PromotionsService],
  imports: [TypeOrmModule.forFeature([Promotion]), CollaboratorsModule, BookingsModule],
  exports: [TypeOrmModule]
})
export class PromotionsModule {}
