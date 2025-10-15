import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { RedeemedcouponService } from './redeemedcoupon.service';
import { CreateRedeemedcouponDto } from './dto/create-redeemedcoupon.dto';
import { UpdateRedeemedcouponDto } from './dto/update-redeemedcoupon.dto';

/**
 * Controller exposing RESTful endpoints for managing redeemed coupons.
 *
 * @remarks
 * This controller handles CRUD operations for the `Redeemedcoupon` entity.
 * It delegates business logic to the {@link RedeemedcouponService}.
 *
 * Routes:
 * - `POST /redeemedcoupon` — Create a new redeemed coupon.
 * - `GET /redeemedcoupon` — Retrieve all redeemed coupons.
 * - `GET /redeemedcoupon/:id` — Retrieve a redeemed coupon by ID.
 * - `PATCH /redeemedcoupon/:id` — Update a redeemed coupon record.
 * - `DELETE /redeemedcoupon/:id` — Remove a redeemed coupon.
 */
@Controller('redeemedcoupon')
export class RedeemedcouponController {
  /**
   * Creates an instance of RedeemedcouponController.
   *
   * @param redeemedcouponService - Service handling redeemed coupon logic.
   */
  constructor(private readonly redeemedcouponService: RedeemedcouponService) {}

  /**
   * Creates a new redeemed coupon record.
   *
   * @param createRedeemedcouponDto - DTO containing the redemption data.
   * @returns The newly created redeemed coupon.
   *
   * @example
   * POST /redeemedcoupon
   * ```json
   * {
   *   "userId": 5,
   *   "collaboratorId": 3,
   *   "branchId": 2,
   *   "promotionId": 17
   * }
   * ```
   */
  @Post()
  create(@Body() createRedeemedcouponDto: CreateRedeemedcouponDto) {
    return this.redeemedcouponService.create(createRedeemedcouponDto);
  }

  /**
   * Retrieves all redeemed coupons.
   *
   * @returns A list of all redeemed coupons in the system.
   * @example GET /redeemedcoupon
   */
  @Get()
  findAll() {
    return this.redeemedcouponService.findAll();
  }

  /**
   * Retrieves a single redeemed coupon by its ID.
   *
   * @param id - The unique identifier of the redeemed coupon.
   * @returns The corresponding redeemed coupon record.
   * @example GET /redeemedcoupon/12
   */
  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.redeemedcouponService.findOne(+id);
  }

  /**
   * Updates an existing redeemed coupon record.
   *
   * @param id - The ID of the record to update.
   * @param updateRedeemedcouponDto - DTO containing updated values.
   * @returns The updated redeemed coupon record.
   *
   * @example
   * PATCH /redeemedcoupon/12
   * ```json
   * {
   *   "branchId": 10
   * }
   * ```
   */
  @Patch(':id') 
  update(@Param('id') id: string, @Body() updateRedeemedcouponDto: UpdateRedeemedcouponDto) {
    return this.redeemedcouponService.update(+id, updateRedeemedcouponDto);
  }

  /**
   * Deletes a redeemed coupon record by its ID.
   *
   * @param id - The ID of the redeemed coupon to delete.
   * @returns A confirmation of deletion or void.
   * @example DELETE /redeemedcoupon/12
   */
  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.redeemedcouponService.remove(+id);
  }
}
