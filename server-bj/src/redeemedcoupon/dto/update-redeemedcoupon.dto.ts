import { PartialType } from '@nestjs/mapped-types';
import { CreateRedeemedcouponDto } from './create-redeemedcoupon.dto';

export class UpdateRedeemedcouponDto extends PartialType(CreateRedeemedcouponDto) {}
