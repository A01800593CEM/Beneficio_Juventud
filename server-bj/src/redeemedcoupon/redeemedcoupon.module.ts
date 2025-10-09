import { forwardRef, Module } from '@nestjs/common';
import { RedeemedcouponService } from './redeemedcoupon.service';
import { RedeemedcouponController } from './redeemedcoupon.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Redeemedcoupon } from './entities/redeemedcoupon.entity';
import { UsersModule } from 'src/users/users.module';
import { PromotionsModule } from 'src/promotions/promotions.module';
import { BranchModule } from '../branch/branch.module';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';

@Module({
  controllers: [RedeemedcouponController],
  providers: [RedeemedcouponService],
  imports: [
    TypeOrmModule.forFeature([Redeemedcoupon]),
    forwardRef(() => UsersModule),
    forwardRef(() => PromotionsModule),
    forwardRef(() => BranchModule),
    forwardRef(() => CollaboratorsModule)
  ],
  exports: [RedeemedcouponService]
})
export class RedeemedcouponModule {}
