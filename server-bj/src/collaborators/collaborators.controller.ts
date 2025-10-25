import { Controller, Get, Post, Body, Patch, Param, Delete, Query, BadRequestException } from '@nestjs/common';
import { CollaboratorsService } from './collaborators.service';
import { CreateCollaboratorDto } from './dto/create-collaborator.dto';
import { UpdateCollaboratorDto } from './dto/update-collaborator.dto';
import { PromotionsService } from 'src/promotions/promotions.service';

@Controller('collaborators')
export class CollaboratorsController {
  constructor(
    private readonly collaboratorsService: CollaboratorsService,
    private readonly promotionsService: PromotionsService) {}

  @Post()
  create(@Body() createCollaboratorDto: CreateCollaboratorDto) {
    return this.collaboratorsService.create(createCollaboratorDto);
  }

  @Get()
  findAll() {
    return this.collaboratorsService.findAll();
  }

  @Get('active/all')
  findAllActive() {
    return this.collaboratorsService.findAllActive();
  }

  @Get('active/newest')
  findAllActiveByNewest() {
    return this.collaboratorsService.findAllActiveByNewest();
  }

  @Get('active/by-latest-promotion')
  findAllActiveWithLatestPromotion() {
    return this.collaboratorsService.findAllActiveWithLatestPromotion();
  }

  @Get('nearby/search')
  async findNearbyCollaborators(
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

    return this.collaboratorsService.findNearbyCollaborators(lat, lon, radiusKm);
  }

  @Get('promotions/:id')
  getPromotions(@Param('id') id: string) {
    console.log(id)
    return this.promotionsService.promotionsByCollaborator(id)
  }

  @Get('category/:categoryName')
  findByCategory(@Param('categoryName') categoryName: string) {
    return this.collaboratorsService.findByCategory(categoryName)
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.collaboratorsService.findOne(id);
  }

  @Patch(':id')
  update(@Param('id') id: string, @Body() updateCollaboratorDto: UpdateCollaboratorDto) {
    return this.collaboratorsService.update(id, updateCollaboratorDto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.collaboratorsService.remove(id);
  }

  

}
