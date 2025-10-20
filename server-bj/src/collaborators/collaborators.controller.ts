import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
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

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.collaboratorsService.findOne(id);
  }

  @Get('category/:categoryName')
  findByCategory(@Param('categoryName') categoryName: string) {
    return this.collaboratorsService.findByCategory(categoryName)
  }

  @Patch(':id')
  update(@Param('id') id: string, @Body() updateCollaboratorDto: UpdateCollaboratorDto) {
    return this.collaboratorsService.update(id, updateCollaboratorDto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.collaboratorsService.remove(id);
  }

  @Get('promotions/:id')
  getPromotions(@Param('id') id: string) {
    console.log(id)
    return this.promotionsService.promotionsByCollaborator(id)
  }

}
