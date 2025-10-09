import { Test, TestingModule } from '@nestjs/testing';
import { RedeemedcouponService } from './redeemedcoupon.service';

describe('RedeemedcouponService', () => {
  let service: RedeemedcouponService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [RedeemedcouponService],
    }).compile();

    service = module.get<RedeemedcouponService>(RedeemedcouponService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
