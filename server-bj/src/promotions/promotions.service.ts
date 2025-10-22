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
import { Branch } from 'src/branch/entities/branch.entity';
import {
  calculateDistance,
  parseLocationString,
  Coordinates,
} from 'src/common/location.utils';

@Injectable()
export class PromotionsService {
  constructor(
    @InjectRepository(Promotion)
    private promotionsRepository: Repository<Promotion>,
    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>,
    @InjectRepository(Branch)
    private branchRepository: Repository<Branch>,
    private readonly notificationsService: NotificationsService,
  ) {}

  async create(createPromotionDto: CreatePromotionDto): Promise<Promotion> {
    const { categoryIds, branchIds, ...data } = createPromotionDto;

    // Obtener categorías
    const safeIds = categoryIds ?? [];
    const categories = await this.categoriesRepository.findBy({ id: In(safeIds) });

    // Cambia a false si quieres ignorar categorías inexistentes en vez de fallar
    const strictMissingCategories = true;
    if (safeIds.length !== categories.length && strictMissingCategories) {
      const found = new Set(categories.map((c) => c.id));
      const missing = safeIds.filter((id) => !found.has(id));
      throw new NotFoundException(`Some categories were not found: [${missing.join(', ')}]`);
    }

    // Obtener sucursales
    let branches: Branch[] = [];
    if (branchIds && branchIds.length > 0) {
      // Sucursales específicas seleccionadas
      branches = await this.branchRepository.findBy({
        branchId: In(branchIds)
      });

      // Validar que todas las sucursales pertenecen al colaborador
      const allBelongToCollaborator = branches.every(
        branch => branch.collaboratorId === data.collaboratorId
      );

      if (!allBelongToCollaborator) {
        throw new NotFoundException(
          'Cannot assign promotion to branches of another collaborator'
        );
      }

      if (branches.length !== branchIds.length) {
        throw new NotFoundException('Some branches were not found');
      }
    } else {
      // Si no se especifican sucursales, aplicar a TODAS las del colaborador
      branches = await this.branchRepository.find({
        where: { collaboratorId: data.collaboratorId }
      });
    }

    const promotion = this.promotionsRepository.create({
      ...data,
      // aceptar ambos (theme y promotionTheme), priorizando "theme"
      theme: data.theme ?? data.promotionTheme ?? PromotionTheme.LIGHT,
      categories,
      branches,
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
      .leftJoinAndSelect('promotion.branches', 'branch')
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
      relations: ['categories', 'branches'],
    });
    if (!promotion) throw new NotFoundException(`Promotion with ID ${id} not found`);

    const { categoryIds, branchIds, ...updateData } = updatePromotionDto;

    Object.assign(promotion, updateData, {
      theme: updateData.theme ?? (updateData as any).promotionTheme ?? promotion.theme,
    });

    if (categoryIds && categoryIds.length > 0) {
      const categories = await this.categoriesRepository.findBy({ id: In(categoryIds) });
      promotion.categories = categories;
    }

    if (branchIds !== undefined) {
      if (branchIds && branchIds.length > 0) {
        // Actualizar con las sucursales especificadas
        const branches = await this.branchRepository.findBy({ branchId: In(branchIds) });

        // Verificar que todas las sucursales pertenezcan al colaborador
        const invalidBranches = branches.filter(b => b.collaboratorId !== promotion.collaboratorId);
        if (invalidBranches.length > 0) {
          throw new NotFoundException(
            'Cannot assign promotion to branches of another collaborator'
          );
        }

        if (branches.length !== branchIds.length) {
          throw new NotFoundException('Some branches were not found');
        }

        promotion.branches = branches;
      } else {
        // Si branchIds es un array vacío, aplicar a TODAS las sucursales del colaborador
        const allBranches = await this.branchRepository.find({
          where: { collaboratorId: promotion.collaboratorId }
        });
        promotion.branches = allBranches;
      }
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

  /**
   * Encuentra promociones cercanas a la ubicación del usuario
   * @param latitude Latitud del usuario
   * @param longitude Longitud del usuario
   * @param radiusKm Radio de búsqueda en kilómetros (por defecto 3km)
   * @returns Lista de promociones con distancia, ordenadas por proximidad
   */
  async findNearbyPromotions(
    latitude: number,
    longitude: number,
    radiusKm: number = 3,
  ): Promise<any[]> {
    try {
      const userLocation: Coordinates = { latitude, longitude };

      console.log('========== NEARBY PROMOTIONS DEBUG ==========');
      console.log('User Location:', userLocation);
      console.log('Search Radius:', radiusKm, 'km');

    // Obtener todas las promociones activas con sus sucursales asignadas
    // IMPORTANTE: Usamos promotion.branches (relación many-to-many) para obtener
    // solo las sucursales específicas asociadas a cada promoción, no todas las
    // sucursales del colaborador
    const promotions = await this.promotionsRepository
      .createQueryBuilder('promotion')
      .leftJoinAndSelect('promotion.categories', 'category')
      .leftJoinAndSelect('promotion.branches', 'branch')
      .leftJoin('promotion.collaborator', 'collaborator')
      .addSelect([
        'collaborator.businessName',
        'collaborator.cognitoId',
        'collaborator.logoUrl',
      ])
      .where('promotion.promotionState = :state', {
        state: PromotionState.ACTIVE,
      })
      .andWhere('promotion.endDate >= :now', { now: new Date() })
      .getMany();

    console.log('Total active promotions found:', promotions.length);

    // Calcular distancias y filtrar por proximidad
    const promotionsWithDistance: any[] = [];

    for (const promo of promotions as any[]) {
      const { collaborator, ...promoData} = promo;

      console.log('\n--- Processing Promotion:', promoData.title || promoData.promotionId);
      console.log('Collaborator:', collaborator?.businessName);

      // Obtener las sucursales asignadas a esta promoción específica
      const branches = promoData.branches || [];
      console.log('Total branches assigned to promotion:', branches.length);

      const branchesWithLocation = branches.filter(
        (b: any) => b.location !== null,
      );
      console.log('Branches with location:', branchesWithLocation.length);

      if (branchesWithLocation.length === 0) {
        console.log('⚠️  No branches with location, skipping...');
        continue;
      }

      // Encontrar la sucursal más cercana
      let closestDistance = Infinity;
      let closestBranch = null;

      for (const branch of branchesWithLocation) {
        console.log(`  Branch: "${branch.name}" - Location raw:`, branch.location);
        console.log(`  Location type: ${typeof branch.location}`);

        const branchCoords = parseLocationString(branch.location);
        if (!branchCoords) {
          console.log(`  ❌ Failed to parse location for branch "${branch.name}"`);
          continue;
        }

        console.log(`  ✓ Parsed coords: lat=${branchCoords.latitude}, lon=${branchCoords.longitude}`);
        const distance = calculateDistance(userLocation, branchCoords);
        console.log(`  Distance: ${distance} km`);

        if (distance < closestDistance) {
          closestDistance = distance;
          closestBranch = {
            branchId: branch.branchId,
            name: branch.name,
            address: branch.address,
            phone: branch.phone,
            zipCode: branch.zipCode,
            location: branch.location,
          };
        }
      }

      console.log(`Closest distance: ${closestDistance} km (limit: ${radiusKm} km)`);

      // Si la sucursal más cercana está dentro del radio, agregar la promoción
      if (closestDistance <= radiusKm && closestBranch) {
        console.log(`✅ ADDED to results (within ${radiusKm}km)`);
        promotionsWithDistance.push({
          ...promoData,
          businessName: collaborator?.businessName || null,
          logoUrl: collaborator?.logoUrl || null,
          distance: closestDistance,
          closestBranch,
        });
      } else {
        console.log(`❌ EXCLUDED (distance ${closestDistance} > ${radiusKm}km)`);
      }
    }

    console.log('\n========== RESULTS ==========');
    console.log('Total promotions within radius:', promotionsWithDistance.length);
    console.log('=====================================\n');

      // Ordenar por distancia (más cercano primero)
      promotionsWithDistance.sort((a, b) => a.distance - b.distance);

      return promotionsWithDistance;
    } catch (error) {
      console.error('❌ ERROR in findNearbyPromotions:', error);
      console.error('Stack trace:', error.stack);
      throw error;
    }
  }
}
