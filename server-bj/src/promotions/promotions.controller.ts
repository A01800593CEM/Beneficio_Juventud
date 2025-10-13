import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { PromotionsService } from './promotions.service';
import { CreatePromotionDto } from './dto/create-promotion.dto';
import { UpdatePromotionDto } from './dto/update-promotion.dto';
import { CategoriesByNamePipe } from 'src/common/pipes/transform-to-id.pipe';
import { Category } from 'src/categories/entities/category.entity';
@Controller('promotions')
export class PromotionsController {
  constructor(private readonly promotionsService: PromotionsService) {}

  @Post()
  create(
    @Body('categories', CategoriesByNamePipe) categories: Category[],
    @Body() createPromotionDto: CreatePromotionDto) {
    return this.promotionsService.create({
      ...createPromotionDto,
      categoryIds: categories.map(category => category.id)});
  }

  @Get()
  findAll() {
    return this.promotionsService.findAll();
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.promotionsService.findOne(+id);
  }

  @Patch(':id')
  update(@Param('id') id: string, @Body() updatePromotionDto: UpdatePromotionDto) {
    return this.promotionsService.update(+id, updatePromotionDto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.promotionsService.remove(+id);
  }

  //Promotions per Category
  @Get('category/:category')
  promotionPerCategory(@Param('category') category: string) {
    return this.promotionsService.promotionPerCategory(category);
  }

  
}
