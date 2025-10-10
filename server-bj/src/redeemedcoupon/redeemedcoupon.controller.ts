import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { RedeemedcouponService } from './redeemedcoupon.service';
import { CreateRedeemedcouponDto } from './dto/create-redeemedcoupon.dto';
import { UpdateRedeemedcouponDto } from './dto/update-redeemedcoupon.dto';

@Controller('redeemedcoupon')
export class RedeemedcouponController {
  constructor(private readonly redeemedcouponService: RedeemedcouponService) {}

  @Post()
  create(@Body() createRedeemedcouponDto: CreateRedeemedcouponDto) {
    return this.redeemedcouponService.create(createRedeemedcouponDto);
  }

  @Get()
  findAll() {
    return this.redeemedcouponService.findAll();
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.redeemedcouponService.findOne(+id);
  }

  @Patch(':id')
  update(@Param('id') id: string, @Body() updateRedeemedcouponDto: UpdateRedeemedcouponDto) {
    return this.redeemedcouponService.update(+id, updateRedeemedcouponDto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.redeemedcouponService.remove(+id);
  }
}
