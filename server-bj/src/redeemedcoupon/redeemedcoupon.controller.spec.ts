import { Test, TestingModule } from '@nestjs/testing';
import { RedeemedcouponController } from './redeemedcoupon.controller';
import { RedeemedcouponService } from './redeemedcoupon.service';

describe('RedeemedcouponController', () => {
  let controller: RedeemedcouponController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [RedeemedcouponController],
      providers: [RedeemedcouponService],
    }).compile();

    controller = module.get<RedeemedcouponController>(RedeemedcouponController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
