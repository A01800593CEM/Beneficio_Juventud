import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { PromotionsService } from './promotions.service';
import { CreatePromotionDto } from './dto/create-promotion.dto';
import { UpdatePromotionDto } from './dto/update-promotion.dto';
import { CategoriesByNamePipe } from 'src/common/pipes/transform-to-id.pipe';
import { Category } from 'src/categories/entities/category.entity';
@Controller('promotions')
export class PromotionsController {
  /**
   * Creates an instance of PromotionsController.
   *
   * @param promotionsService - Service that contains the business logic for promotions.
   */
  constructor(private readonly promotionsService: PromotionsService) {}

  /**
   * Creates a new promotion.
   *
   * @param createPromotionDto - Data Transfer Object containing promotion details.
   * @returns A Promise resolving to the created promotion.
   *
   * @example
   * POST /promotions
   * ```json
   * {
   *   "collaboratorId": 1,
   *   "title": "Summer Sale",
   *   "description": "50% off selected items",
   *   "promotionType": "descuento",
   *   "promotionState": "activa"
   * }
   * ```
   */
  @Post()
  create(
    @Body('categories', CategoriesByNamePipe) categories: Category[],
    @Body() createPromotionDto: CreatePromotionDto) {
    return this.promotionsService.create({
      ...createPromotionDto,
      categoryIds: categories.map(category => category.id)});
  }

  /**
   * Retrieves all promotions.
   *
   * @returns A Promise resolving to an array of promotions.
   * @example GET /promotions
   */
  @Get()
  findAll() {
    return this.promotionsService.findAll();
  }

  /**
   * Retrieves a specific promotion by its ID.
   *
   * @param id - Promotion identifier.
   * @returns A Promise resolving to the found promotion.
   * @example GET /promotions/5
   */
  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.promotionsService.findOne(+id);
  }

  /**
   * Updates an existing promotion.
   *
   * @param id - The ID of the promotion to update.
   * @param updatePromotionDto - DTO containing updated fields.
   * @returns A Promise resolving to the updated promotion.
   * @example
   * PATCH /promotions/3
   * ```json
   * {
   *   "title": "Extended Summer Sale",
   *   "promotionState": "activa"
   * }
   * ```
   */
  @Patch(':id')
  update(@Param('id') id: string, @Body() updatePromotionDto: UpdatePromotionDto) {
    return this.promotionsService.update(+id, updatePromotionDto);
  }

  /**
   * Deletes a promotion by its ID.
   *
   * @param id - The ID of the promotion to remove.
   * @returns A Promise resolving when the deletion is complete.
   * @example DELETE /promotions/7
   */
  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.promotionsService.remove(+id);
  }

  
  //Promotions per Category

  /**
   * Retrieves all promotions that belong to a specific category.
   *
   * @param category - The category name or identifier.
   * @returns A Promise resolving to an array of promotions filtered by category.
   * @example GET /promotions/category/food
   */
  @Get('category/:category')
  promotionPerCategory(@Param('category') category: string) {
    return this.promotionsService.promotionPerCategory(category);
  }

  
}
