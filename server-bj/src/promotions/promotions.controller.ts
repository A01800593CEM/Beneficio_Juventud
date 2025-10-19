import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  ValidationPipe,
} from '@nestjs/common';
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
    @Body(new ValidationPipe({ whitelist: true, transform: true }))
    dto: CreatePromotionDto,
  ) {
    return this.promotionsService.create({
      ...dto,
      categoryIds: categories.map(category => category.id)});
  }

  @Get()
  findAll() {
    return this.promotionsService.findAll();
  }

  @Get(':id')
  findOne(@Param('id', ParseIntPipe) id: number) {
    return this.promotionsService.findOne(id);
  }

  @Patch(':id')
  update(
    @Param('id', ParseIntPipe) id: number,
    @Body(new ValidationPipe({ whitelist: true, transform: true }))
    dto: UpdatePromotionDto,
  ) {
    return this.promotionsService.update(id, dto);
  }
  @Get('category/:category')
  async promotionPerCategory(@Param('category') category: string) {
    return this.promotionsService.promotionPerCategory(category);
  }
}
