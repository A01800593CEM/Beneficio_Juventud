import { Injectable, NotFoundException } from '@nestjs/common';
import { CreatePromotionDto } from './dto/create-promotion.dto';
import { UpdatePromotionDto } from './dto/update-promotion.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { Promotion } from './entities/promotion.entity';
import { Category } from 'src/categories/entities/category.entity';
import { PromotionState } from './enums/promotion-state.enums';
import { NotificationsService } from 'src/notifications/notifications.service';
import { PromotionTheme } from './enums/promotion-theme.enum';

@Injectable()
export class PromotionsService {
  constructor(
    @InjectRepository(Promotion)
    private promotionsRepository: Repository<Promotion>,
    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>,
    private readonly notificationsService: NotificationsService,
  ) {}

  async create(createPromotionDto: CreatePromotionDto): Promise<Promotion> {
    const { categoryIds, ...data } = createPromotionDto;

    const safeIds = categoryIds ?? [];
    const categories = await this.categoriesRepository.findBy({ id: In(safeIds) });

    // Cambia a false si quieres ignorar categorías inexistentes en vez de fallar
    const strictMissingCategories = true;
    if (safeIds.length !== categories.length && strictMissingCategories) {
      const found = new Set(categories.map((c) => c.id));
      const missing = safeIds.filter((id) => !found.has(id));
      throw new NotFoundException(`Some categories were not found: [${missing.join(', ')}]`);
    }

    const promotion = this.promotionsRepository.create({
      ...data,
      // aceptar ambos (theme y promotionTheme), priorizando "theme"
      theme: data.theme ?? data.promotionTheme ?? PromotionTheme.LIGHT,
      categories,
    });

    this.notificationsService.newPromoNotif(promotion);
    return this.promotionsRepository.save(promotion);
  }

  async findAll(): Promise<any[]> {
    const promotions = await this.promotionsRepository.find({
      relations: ['categories', 'collaborator'],
    });

    return promotions.map((promo: any) => {
      const { collaborator, ...rest } = promo;
      return {
        ...rest,
        businessName: collaborator?.businessName ?? null,
      };
    });
  }

  async findOne(id: number): Promise<any> {
    const promo = await this.promotionsRepository
      .createQueryBuilder('promotion')
      .leftJoinAndSelect('promotion.categories', 'category')
      .leftJoin('promotion.collaborator', 'collaborator')
      .addSelect(['collaborator.businessName'])
      .addSelect('promotion.theme')
      .where('promotion.promotionId = :id', { id })
      .andWhere('promotion.promotionState = :state', { state: PromotionState.ACTIVE })
      .getOne();

    if (!promo) throw new NotFoundException(`Promotion with id ${id} not found`);

    const { collaborator, ...rest } = promo as any;
    return {
      ...rest,
      businessName: collaborator?.businessName ?? null,
    };
  }

  async trueFindOne(id: number): Promise<any> {
    const promo = await this.promotionsRepository
      .createQueryBuilder('promotion')
      .leftJoinAndSelect('promotion.categories', 'category')
      .leftJoin('promotion.collaborator', 'collaborator')
      .addSelect(['collaborator.businessName'])
      .addSelect('promotion.theme')
      .where('promotion.promotionId = :id', { id })
      .andWhere('promotion.promotionState = :state', { state: PromotionState.ACTIVE })
      .getOne();

    if (!promo) return null;

    const { collaborator, ...rest } = promo as any;
    return {
      ...rest,
      businessName: collaborator?.businessName ?? null,
    };
  }

  async update(id: number, updatePromotionDto: UpdatePromotionDto): Promise<Promotion> {
    const promotion = await this.promotionsRepository.findOne({
      where: { promotionId: id },
      relations: ['categories'],
    });
    if (!promotion) throw new NotFoundException(`Promotion with ID ${id} not found`);

    const { categoryIds, ...updateData } = updatePromotionDto;

    Object.assign(promotion, updateData, {
      theme: updateData.theme ?? (updateData as any).promotionTheme ?? promotion.theme,
    });

    if (categoryIds && categoryIds.length > 0) {
      const categories = await this.categoriesRepository.findBy({ id: In(categoryIds) });
      promotion.categories = categories;
    }

    return this.promotionsRepository.save(promotion);
  }

  async remove(id: number): Promise<void> {
    await this.promotionsRepository.delete(id);
  }

  async promotionPerCategory(category: string): Promise<any[]> {
    const qb = this.promotionsRepository
      .createQueryBuilder('promotion')
      .leftJoinAndSelect('promotion.categories', 'category')
      .leftJoin('promotion.collaborator', 'collaborator')
      .addSelect(['collaborator.businessName'])
      .addSelect('promotion.theme')
      .andWhere('promotion.promotionState = :state', { state: PromotionState.ACTIVE });

    const asNumber = Number(category);
    if (!Number.isNaN(asNumber)) {
      qb.andWhere('category.id = :id', { id: asNumber });
    } else {
      qb.andWhere('LOWER(category.name) = :name', { name: category.toLowerCase() });
    }

    const promos = await qb.getMany();

    return promos.map((p: any) => ({
      ...p,
      businessName: p?.collaborator?.businessName ?? null,
    }));
  }

  async promotionsByCollaborator(collaboratorId: string): Promise<Promotion[]> {
    return this.promotionsRepository.find({
      where: { collaboratorId },
    });
  }
}
