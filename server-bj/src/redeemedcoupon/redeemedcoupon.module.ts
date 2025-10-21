import { forwardRef, Module } from '@nestjs/common';
import { RedeemedcouponService } from './redeemedcoupon.service';
import { RedeemedcouponController } from './redeemedcoupon.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Redeemedcoupon } from './entities/redeemedcoupon.entity';
import { Promotion } from '../promotions/entities/promotion.entity';
import { UsersModule } from 'src/users/users.module';
import { PromotionsModule } from 'src/promotions/promotions.module';
import { BranchModule } from '../branch/branch.module';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';

/**
 * Redeemed Coupon module that encapsulates all logic for managing coupon redemption records.
 *
 * @remarks
 * This module integrates several domain entities:
 * - {@link Redeemedcoupon}: TypeORM entity representing a coupon redemption.
 * - {@link UsersModule}: Provides user context for redemptions.
 * - {@link PromotionsModule}: Links each redemption to a specific promotion.
 * - {@link BranchModule}: Associates redemptions with a branch.
 * - {@link CollaboratorsModule}: Connects redemptions to collaborators.
 *
 * @example
 * ```ts
 * @Module({
 *   imports: [RedeemedcouponModule],
 * })
 * export class AppModule {}
 * ```
 */
@Module({
  controllers: [RedeemedcouponController],
  providers: [RedeemedcouponService],
  imports: [
    TypeOrmModule.forFeature([Redeemedcoupon, Promotion]),
    forwardRef(() => UsersModule),
    forwardRef(() => PromotionsModule),
    forwardRef(() => BranchModule),
    forwardRef(() => CollaboratorsModule)
  ],
  exports: [RedeemedcouponService]
})
export class RedeemedcouponModule {}
