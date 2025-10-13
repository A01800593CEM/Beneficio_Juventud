import { Injectable, NotFoundException } from '@nestjs/common';
import { CreatePromotionDto } from './dto/create-promotion.dto';
import { UpdatePromotionDto } from './dto/update-promotion.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, In } from 'typeorm';
import { Promotion } from './entities/promotion.entity';
import { Category } from 'src/categories/entities/category.entity';
import { PromotionState } from './enums/promotion-state.enums';
import { sendNotification } from 'src/enviar';
import { NotificationsService } from 'src/notifications/notifications.service';

/**
 * Servicio responsable de la lógica de negocio para promociones.
 *
 * @remarks
 * Este servicio encapsula la creación, lectura, actualización y eliminación
 * de promociones, además de consultas filtradas por categoría.
 */
@Injectable()
export class PromotionsService {
  /**
   * Crea una instancia del servicio, inyectando los repositorios necesarios.
   * @param promotionsRepository Repositorio TypeORM para {@link Promotion}.
   * @param categoriesRepository Repositorio TypeORM para {@link Category}.
   */
  constructor(
    @InjectRepository(Promotion)
    private promotionsRepository: Repository<Promotion>,

    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>,
    private readonly notificationsService: NotificationsService
  ) {}

  /**
   * Crea una nueva promoción y asocia categorías opcionales.
   *
   * @param createPromotionDto DTO con los datos de la promoción.
   * @returns La promoción creada.
   *
   * @example
   * await promotionsService.create({ title: '2x1', categoryIds: [1, 2], ... });
   */
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
      this.notificationsService.sendNotification();
      return this.promotionsRepository.save(promotion);
    }

    /**
   * Obtiene todas las promociones, incluyendo sus categorías.
   * @returns Arreglo de promociones.
   */
    async findAll(): Promise<Promotion[]> {
        return this.promotionsRepository.find({ 
          relations: ['categories'] });
      }

    /**
   * Obtiene una promoción activa por ID.
   *
   * @param id Identificador de la promoción.
   * @throws {NotFoundException} Si no existe una promoción activa con ese ID.
   * @returns La promoción encontrada.
   */
    async findOne(id: number): Promise<Promotion | null> {
      const promotion = this.promotionsRepository.findOne({ 
        where: { promotionId: id,
                 promotionState: PromotionState.ACTIVE
         },
        relations: ['categories'] });
          
      if (!promotion) {
        throw new NotFoundException(`Promotion with id ${id} not found`);
      }
      return promotion
    }
  
    // Finds in all the database
    /**
   * Obtiene una promoción (consulta "real") por ID.
   *
   * @remarks
   * Actualmente filtra por {@link PromotionState.ACTIVE} igual que `findOne`.
   * Útil si solo deben exponerse promociones activas en cualquier flujo.
   *
   * @param id Identificador de la promoción.
   * @returns La promoción encontrada o `null`.
   */
    async trueFindOne(id: number): Promise<Promotion | null> {
      return this.promotionsRepository.findOne({ 
        where: { promotionId: id,
                 promotionState: PromotionState.ACTIVE
         },
        relations: ['categories'] });
    }

  /**
   * Actualiza una promoción existente y, si se especifica, sus categorías asociadas.
   *
   * @param id ID de la promoción a actualizar.
   * @param updatePromotionDto DTO con las propiedades a actualizar.
   * @throws {NotFoundException} Si la promoción no existe.
   * @returns La promoción actualizada.
   */
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

  /**
   * Elimina una promoción por su ID.
   * @param id ID de la promoción a eliminar.
   * @returns Promesa vacía cuando la operación concluye.
   */
  async remove(id: number): Promise<void> {
    await this.promotionsRepository.delete(id);
  }

  //Promotions per Category
  /**
   * Obtiene promociones filtrando por categoría (por ID numérico o por nombre).
   *
   * @param category ID numérico (como string) o nombre de la categoría.
   * @returns Arreglo de promociones que pertenecen a la categoría indicada.
   *
   * @example
   * await promotionsService.promotionPerCategory('3');      // por ID
   * await promotionsService.promotionPerCategory('food');   // por nombre
   */
  
  async promotionPerCategory(category: string): Promise<Promotion[]> {
  const qb = this.promotionsRepository
    .createQueryBuilder('promotion')
    .leftJoin('promotion.categories', 'category')
    .addSelect(['category.id', 'category.name']);

  const asNumber = Number(category);
  if (!Number.isNaN(asNumber)) {
    qb.where('category.id = :id', { id: asNumber });
  } else {
    qb.where('category.name = :name', { name: category });
  }

  return qb.getMany();
}
}
