import { Injectable, NotFoundException, BadRequestException, ConflictException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Redeemedcoupon } from './entities/redeemedcoupon.entity';
import { CreateRedeemedcouponDto } from './dto/create-redeemedcoupon.dto';
import { UpdateRedeemedcouponDto } from './dto/update-redeemedcoupon.dto';
import { Promotion } from 'src/promotions/entities/promotion.entity';

/**
 * Service responsible for managing redeemed coupons in the system.
 *
 * @remarks
 * This class encapsulates all database operations related to the
 * {@link Redeemedcoupon} entity. It includes creation, retrieval,
 * update, and deletion methods, using dependency injection for the repository.
 */
@Injectable()
export class RedeemedcouponService {
  /**
   * Creates an instance of RedeemedcouponService.
   *
   * @param reedemedcouponsRepository - TypeORM repository for {@link Redeemedcoupon} entities.
   * @param promotionsRepository - TypeORM repository for {@link Promotion} entities.
   */
  constructor(
    @InjectRepository(Redeemedcoupon)
    private reedemedcouponsRepository: Repository<Redeemedcoupon>,
    @InjectRepository(Promotion)
    private promotionsRepository: Repository<Promotion>,
  ) {}
  /**
   * Creates and saves a new redeemed coupon in the database.
   *
   * @param createRedeemedcouponDto - Data Transfer Object containing redemption details.
   * @returns The created {@link Redeemedcoupon} record.
   * @throws {NotFoundException} If promotion doesn't exist
   * @throws {BadRequestException} If promotion is inactive, expired, or out of stock
   * @throws {ConflictException} If nonce was already used (replay attack)
   *
   * @example
   * await redeemedcouponService.create({
   *   userId: "a1fbe500-a091-70e3-5a7b-3b1f4537f10f",
   *   branchId: 3,
   *   promotionId: 7,
   *   nonce: "abc12345",
   *   qrTimestamp: 1729458361234
   * });
   */
  async create(createRedeemedcouponDto: CreateRedeemedcouponDto): Promise<Redeemedcoupon> {
    const { userId, promotionId, nonce } = createRedeemedcouponDto;

    // 1. Verificar que la promoción existe
    const promotion = await this.promotionsRepository.findOne({
      where: { promotionId }
    });

    if (!promotion) {
      throw new NotFoundException(`Promoción con ID ${promotionId} no encontrada`);
    }

    // 2. Verificar que la promoción está activa
    if (promotion.promotionState !== 'activa') {
      throw new BadRequestException(`La promoción no está activa. Estado actual: ${promotion.promotionState}`);
    }

    // 3. Verificar stock disponible (si la promoción tiene stock limitado)
    if (promotion.availableStock !== null && promotion.availableStock !== undefined) {
      if (promotion.availableStock <= 0) {
        throw new BadRequestException('No hay stock disponible para esta promoción');
      }
    }

    // 4. Validar que el nonce no haya sido usado (prevenir replay attacks)
    if (nonce) {
      const existingWithNonce = await this.reedemedcouponsRepository.findOne({
        where: { nonce, promotionId }
      });

      if (existingWithNonce) {
        throw new ConflictException('Este código QR ya fue utilizado');
      }
    }

    // 5. Verificar límite por usuario (si aplica)
    if (promotion.limitPerUser !== null && promotion.limitPerUser !== undefined && promotion.limitPerUser > 0) {
      const userRedemptions = await this.reedemedcouponsRepository.count({
        where: { userId, promotionId }
      });

      if (userRedemptions >= promotion.limitPerUser) {
        throw new BadRequestException(`Has alcanzado el límite de ${promotion.limitPerUser} usos para esta promoción`);
      }
    }

    // 6. Verificar límite diario por usuario (si aplica)
    if (promotion.dailyLimitPerUser !== null && promotion.dailyLimitPerUser !== undefined && promotion.dailyLimitPerUser > 0) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const todayRedemptions = await this.reedemedcouponsRepository
        .createQueryBuilder('rc')
        .where('rc.usuario_id = :userId', { userId })
        .andWhere('rc.promocion_id = :promotionId', { promotionId })
        .andWhere('rc.fecha_uso >= :today', { today })
        .getCount();

      if (todayRedemptions >= promotion.dailyLimitPerUser) {
        throw new BadRequestException(`Has alcanzado el límite diario de ${promotion.dailyLimitPerUser} usos para esta promoción`);
      }
    }

    // 7. Decrementar stock disponible (si aplica)
    if (promotion.availableStock !== null && promotion.availableStock !== undefined && promotion.availableStock > 0) {
      promotion.availableStock -= 1;
      await this.promotionsRepository.save(promotion);
    }

    // 8. Crear el cupón canjeado
    const redeemedcoupon = this.reedemedcouponsRepository.create(createRedeemedcouponDto);
    return this.reedemedcouponsRepository.save(redeemedcoupon);
  }

   /**
   * Retrieves all redeemed coupons, including related entities.
   *
   * @returns An array of {@link Redeemedcoupon} entities with their relationships.
   * @example await redeemedcouponService.findAll();
   */
  async findAll(): Promise<Redeemedcoupon[]> {
    return this.reedemedcouponsRepository.find({relations: ['user', 'promotion', 'branch']});
  }

  async findAllByUser(userId: string): Promise<Redeemedcoupon[]> {
    return await this.reedemedcouponsRepository.find({
      where: {userId},
      relations: ['promotion']
    })
  }

  /**
   * Finds a single redeemed coupon by its ID.
   *
   * @param id - The unique identifier of the redeemed coupon.
   * @returns The matching {@link Redeemedcoupon} record or `null` if not found.
   * @example await redeemedcouponService.findOne(10);
   */
  async findOne(id: number): Promise<Redeemedcoupon | null> {
    return this.reedemedcouponsRepository.findOne({
      where: {usedId: id},
      relations: ['user', 'promotion', 'branch'],
    });
  }

   /**
   * Updates a redeemed coupon record.
   *
   * @param id - The ID of the redeemed coupon to update.
   * @param updateRedeemedcouponDto - DTO with the updated properties.
   * @throws {NotFoundException} If the redeemed coupon with the given ID does not exist.
   * @returns The updated {@link Redeemedcoupon} record.
   *
   * @example
   * await redeemedcouponService.update(5, { branchId: 12 });
   */
  async update(id: number, updateRedeemedcouponDto: UpdateRedeemedcouponDto): Promise<Redeemedcoupon | null> {
    const redeemedcoupon = await this.reedemedcouponsRepository.preload({
      usedId: id,
      ...updateRedeemedcouponDto,
    })
    if (!redeemedcoupon){
      throw new NotFoundException(`Redeemed Coupon with id ${id} not found`)
    }
    return this.reedemedcouponsRepository.save(redeemedcoupon);
  }

  /**
   * Deletes a redeemed coupon by its ID.
   *
   * @param id - The ID of the redeemed coupon to delete.
   * @returns A void promise after successful deletion.
   * @example await redeemedcouponService.remove(7);
   */
  async remove(id: number): Promise<void> {
    await this.reedemedcouponsRepository.delete(id);
  }
}
