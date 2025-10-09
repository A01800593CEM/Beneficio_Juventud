import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Redeemedcoupon } from './entities/redeemedcoupon.entity';
import { CreateRedeemedcouponDto } from './dto/create-redeemedcoupon.dto';
import { UpdateRedeemedcouponDto } from './dto/update-redeemedcoupon.dto';

@Injectable()
export class RedeemedcouponService {
  constructor(
    @InjectRepository(Redeemedcoupon)
    private reedemedcouponsRepository: Repository<Redeemedcoupon>,
  ) {}
  async create(createRedeemedcouponDto: CreateRedeemedcouponDto): Promise<Redeemedcoupon> {
    const redeemedcoupon = this.reedemedcouponsRepository.create(createRedeemedcouponDto);
    return this.reedemedcouponsRepository.save(redeemedcoupon);
  }

  async findAll(): Promise<Redeemedcoupon[]> {
    return this.reedemedcouponsRepository.find({relations: ['user', 'promotion', 'branch', 'collaborators']});
  }

  async findOne(id: number): Promise<Redeemedcoupon | null> {
    return this.reedemedcouponsRepository.findOne({
      where: {usedId: id},
      relations: ['user', 'promotion', 'branch', 'collaborators'],
    });
  }

  async update(id: number, updateRedeemedcouponDto: UpdateRedeemedcouponDto): Promise<Redeemedcoupon | null> {
    const redeemedcoupon = await this.reedemedcouponsRepository.preload({
      usedId: id,
      ...UpdateRedeemedcouponDto,
    })
    if (!redeemedcoupon){
      throw new NotFoundException(`Redeemed Coupon with id ${id} not found`)
    }
    return this.reedemedcouponsRepository.save(redeemedcoupon);
  }

  async remove(id: number): Promise<void> {
    await this.reedemedcouponsRepository.delete(id);
  }
}
