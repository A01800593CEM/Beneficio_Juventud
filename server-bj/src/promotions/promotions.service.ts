import { Injectable, NotFoundException } from '@nestjs/common';
import { CreatePromotionDto } from './dto/create-promotion.dto';
import { UpdatePromotionDto } from './dto/update-promotion.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { Promotion } from './entities/promotion.entity';
import { Category } from 'src/categories/entities/category.entity';
import { PromotionState } from './enums/promotion-state.enums';

@Injectable()
export class PromotionsService {
  constructor(
    @InjectRepository(Promotion)
    private promotionsRepository: Repository<Promotion>,

    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>
  ) {}
  async create(createPromotionDto: CreatePromotionDto): Promise<Promotion> {
      const { categoryIds, ...data } = createPromotionDto;
      // Ensure categoryIds is never undefined
      const safeCategoryIds = categoryIds ?? [];
      const categories = await this.categoriesRepository.findBy({
        id: In(safeCategoryIds),
      });
      const promotion = this.promotionsRepository.create({
        ...data,
        categories,
      });
      return this.promotionsRepository.save(promotion);
    }

    async findAll(): Promise<Promotion[]> {
        return this.promotionsRepository.find({ 
          relations: ['categories'] });
      }

    async findOne(id: number): Promise<Promotion | null> {
      const promotion = this.promotionsRepository.findOne({ 
        where: { promotionId: id,
                 promotionState: PromotionState.ACTIVE
         },
        relations: ['categories'] });
          
      if (!promotion) {
        throw new NotFoundException(`User with id ${id} not found`);
      }
      return promotion
    }
  
    // Finds in all the database
    async trueFindOne(id: number): Promise<Promotion | null> {
      return this.promotionsRepository.findOne({ 
        where: { promotionId: id,
                 promotionState: PromotionState.ACTIVE
         },
        relations: ['categories'] });
    }

  async update(id: number, updatePromotionDto: UpdatePromotionDto): Promise<Promotion> {
      const promotion = await this.promotionsRepository.findOne({
        where: { promotionId: id },
        relations: ['categories'],
      });
  
      if (!promotion) {
        throw new NotFoundException(`Promotion with ID ${id} not found`);
      }
  
      const { categoryIds, ...updateData } = updatePromotionDto;
  
      
      Object.assign(promotion, updateData);
  
      
      if (categoryIds && categoryIds.length > 0) {
        const categories = await this.categoriesRepository.findBy({ id: In(categoryIds) });
        promotion.categories = categories;
      }
  
      return this.promotionsRepository.save(promotion);
    }

  async remove(id: number): Promise<void> {
    await this.promotionsRepository.delete(id);
  }

  async promotionPerCategory(category: string): Promise<Promotion[]> {
  const qb = this.promotionsRepository
    .createQueryBuilder('promotion')
    .leftJoin('promotion.categories', 'category')
    .addSelect(['category.id', 'category.name']); // opcional, para devolverlas

  const asNumber = Number(category);
  if (!Number.isNaN(asNumber)) {
    qb.where('category.id = :id', { id: asNumber });
  } else {
    qb.where('category.name = :name', { name: category });
  }

  return qb.getMany();
}
}
