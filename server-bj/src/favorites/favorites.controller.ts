import { Controller, Get, Post, Patch, Delete, Body, Param } from '@nestjs/common';
import { FavoritesService } from './favorites.service';
import { CreateFavoriteDto } from './dto/create-favorite.dto';
import { UpdateFavoriteDto } from './dto/update-favorite.dto';

@Controller('users/favorites')
export class FavoritesController {
  constructor(private readonly favoritesService: FavoritesService) {}

  @Post()
  async create(@Body() createFavoriteDto: CreateFavoriteDto) {
    return this.favoritesService.create(createFavoriteDto);
  }

  @Get()
  async findAll() {
    return this.favoritesService.findAll();
  }

  @Get(':userId/:collaboratorId')
  async findOne(
    @Param('userId') userId: number,
    @Param('collaboratorId') collaboratorId: number,
  ) {
    return this.favoritesService.findOne(userId, collaboratorId);
  }

  @Patch(':userId/:collaboratorId')
  async update(
    @Param('userId') userId: number,
    @Param('collaboratorId') collaboratorId: number,
    @Body() updateFavoriteDto: UpdateFavoriteDto,
  ) {
    return this.favoritesService.update(userId, collaboratorId, updateFavoriteDto);
  }

  @Delete(':userId/:collaboratorId')
  async remove(
    @Param('userId') userId: number,
    @Param('collaboratorId') collaboratorId: number,
  ) {
    return this.favoritesService.remove(userId, collaboratorId);
  }

    // Favorite Collaborators 
  @Get('user_favorites/:id')
  async getFavoriteCollaborators(@Param('id') id: string) {
    return this.favoritesService.findByUser(+id)
  }

}
