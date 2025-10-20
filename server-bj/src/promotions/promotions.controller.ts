import {
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Query,
  ValidationPipe,
  BadRequestException,
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

  @Get('nearby/search')
  async findNearbyPromotions(
    @Query('latitude') latitude: string,
    @Query('longitude') longitude: string,
    @Query('radius') radius?: string,
  ) {
    // Validar que se proporcionen latitud y longitud
    if (!latitude || !longitude) {
      throw new BadRequestException(
        'Latitude and longitude are required query parameters',
      );
    }

    const lat = parseFloat(latitude);
    const lon = parseFloat(longitude);
    const radiusKm = radius ? parseFloat(radius) : 3;

    // Validar que sean números válidos
    if (isNaN(lat) || isNaN(lon) || isNaN(radiusKm)) {
      throw new BadRequestException('Invalid coordinates or radius format');
    }

    // Validar rangos válidos
    if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
      throw new BadRequestException('Coordinates out of valid range');
    }

    if (radiusKm <= 0 || radiusKm > 50) {
      throw new BadRequestException('Radius must be between 0 and 50 km');
    }

    return this.promotionsService.findNearbyPromotions(lat, lon, radiusKm);
  }
}
