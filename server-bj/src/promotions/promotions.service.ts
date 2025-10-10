import { Injectable, NotFoundException } from '@nestjs/common';
import { CreatePromotionDto } from './dto/create-promotion.dto';
import { UpdatePromotionDto } from './dto/update-promotion.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Promotion } from './entities/promotion.entity';

@Injectable()
export class PromotionsService {
  constructor(
    @InjectRepository(Promotion)
    private promotionsRepository: Repository<Promotion>,
  ) {}
  async create(createPromotionDto: CreatePromotionDto): Promise<Promotion> {
    const promotion = this.promotionsRepository.create(createPromotionDto);
    return this.promotionsRepository.save(promotion);
  }

  async findAll(): Promise<Promotion[]> {
    return this.promotionsRepository.find({relations: ['bookings', 'redeemedcoupon']});
  }

  async findOne(id: number):Promise<Promotion | null> {
    return this.promotionsRepository.findOne({
      where: {promotionId: id},
      relations: ['bookings', 'redeemedcoupon'],
    });
  }

  async update(id: number, UpdatePromotionDto: UpdatePromotionDto): Promise<Promotion | null> {
    const promotion = await this.promotionsRepository.preload({
      promotionId: id,
      ...UpdatePromotionDto
    })
    if (!promotion){
      throw new NotFoundException(`Promotion with id ${id} not found`)
    }
    return this.promotionsRepository.save(promotion);
  }

  async remove(id: number): Promise<void> {
    await this.promotionsRepository.delete(id);
  }

  async promotionPerCategory(category: string): Promise<Promotion[]> {
    return this.promotionsRepository
      .createQueryBuilder('promotion')
      .where('promotion.category = :category', { category })
      .getMany();
  }
}
