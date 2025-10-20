import { Injectable, NotFoundException } from '@nestjs/common';
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
   */
  constructor(
    @InjectRepository(Redeemedcoupon)
    private reedemedcouponsRepository: Repository<Redeemedcoupon>,
  ) {}
  /**
   * Creates and saves a new redeemed coupon in the database.
   *
   * @param createRedeemedcouponDto - Data Transfer Object containing redemption details.
   * @returns The created {@link Redeemedcoupon} record.
   *
   * @example
   * await redeemedcouponService.create({
   *   userId: 5,
   *   branchId: 3,
   *   promotionId: 7
   * });
   */
  async create(createRedeemedcouponDto: CreateRedeemedcouponDto): Promise<Redeemedcoupon> {
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
