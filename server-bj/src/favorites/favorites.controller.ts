import { Controller, Get, Post, Patch, Delete, Body, Param } from '@nestjs/common';
import { FavoritesService } from './favorites.service';
import { CreateFavoriteDto } from './dto/create-favorite.dto';
import { UpdateFavoriteDto } from './dto/update-favorite.dto';

/**
 * Controller responsible for handling favorite-related HTTP requests.
 * Manages user-collaborator favorite relationships.
 * @route /users/favorites
 */
@Controller('users/favorites')
export class FavoritesController {
  /**
   * Creates an instance of FavoritesController.
   * @param favoritesService - The service handling favorite business logic
   */
  constructor(private readonly favoritesService: FavoritesService) {}

  @Post(':userId/:collaboratorId')
  async create(@Param('userId') userId: string,
    @Param('collaboratorId') collaboratorId: string,) {
    let createFavoriteDto = new CreateFavoriteDto();
    createFavoriteDto.userId = userId;
    createFavoriteDto.collaboratorId = collaboratorId;
    return this.favoritesService.create(createFavoriteDto);
  }

  /**
   * Retrieves all favorite relationships.
   * @route GET /users/favorites
   * @returns Promise containing an array of all favorite relationships
   */
  @Get()
  async findAll() {
    return this.favoritesService.findAll();
  }

  /**
   * Retrieves a specific favorite relationship.
   * @route GET /users/favorites/:userId/:collaboratorId
   * @param userId - The ID of the user
   * @param collaboratorId - The ID of the collaborator
   * @returns Promise containing the favorite relationship if found
   */
  @Get(':userId/:collaboratorId')
  async findOne(
    @Param('userId') userId: string,
    @Param('collaboratorId') collaboratorId: string,
  ) {
    return this.favoritesService.findOne(userId, collaboratorId);
  }

  /**
   * Updates a specific favorite relationship.
   * @route PATCH /users/favorites/:userId/:collaboratorId
   * @param userId - The ID of the user
   * @param collaboratorId - The ID of the collaborator
   * @param updateFavoriteDto - The data transfer object containing updated favorite information
   * @returns Promise containing the updated favorite relationship
   */
  @Patch(':userId/:collaboratorId')
  async update(
    @Param('userId') userId: string,
    @Param('collaboratorId') collaboratorId: string,
    @Body() updateFavoriteDto: UpdateFavoriteDto,
  ) {
    return this.favoritesService.update(userId, collaboratorId, updateFavoriteDto);
  }

  /**
   * Removes a favorite relationship.
   * @route DELETE /users/favorites/:userId/:collaboratorId
   * @param userId - The ID of the user
   * @param collaboratorId - The ID of the collaborator
   * @returns Promise containing the result of the deletion
   */
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
