import { Controller, Get, Post, Patch, Delete, Body, Param } from '@nestjs/common';
import { FavoritesService } from './favorites.service';
import { CreateFavoriteDto } from './dto/create-favorite.dto';
import { UpdateFavoriteDto } from './dto/update-favorite.dto';

@Controller('users/collaborators/fav')
export class FavoritesController {
  constructor(private readonly favoritesService: FavoritesService) {}

  @Post(':userId/:collaboratorId')
  async create(@Param('userId') userId: string,
    @Param('collaboratorId') collaboratorId: string,) {
    let createFavoriteDto = new CreateFavoriteDto();
    createFavoriteDto.userId = userId;
    createFavoriteDto.collaboratorId = collaboratorId;
    return this.favoritesService.create(createFavoriteDto);
  }

  @Get()
  async findAll() {
    return this.favoritesService.findAll();
  }

  @Get(':userId/:collaboratorId')
  async findOne(
    @Param('userId') userId: string,
    @Param('collaboratorId') collaboratorId: string,
  ) {
    return this.favoritesService.findOne(userId, collaboratorId);
  }

  @Patch(':userId/:collaboratorId')
  async update(
    @Param('userId') userId: string,
    @Param('collaboratorId') collaboratorId: string,
    @Body() updateFavoriteDto: UpdateFavoriteDto,
  ) {
    return this.favoritesService.update(userId, collaboratorId, updateFavoriteDto);
  }

  @Delete(':userId/:collaboratorId')
  async remove(
    @Param('userId') userId: string,
    @Param('collaboratorId') collaboratorId: string,
  ) {
    return this.favoritesService.remove(userId, collaboratorId);
  }

    // Favorite Collaborators 
  @Get(':id')
  async getFavoriteCollaborators(@Param('id') id: string) {
    return this.favoritesService.findByUser(id)
  }

}
