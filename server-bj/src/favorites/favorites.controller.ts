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

  /**
   * Creates a new favorite relationship between a user and a collaborator.
   * @route POST /users/favorites
   * @param createFavoriteDto - The data transfer object containing user and collaborator IDs
   * @returns Promise containing the newly created favorite relationship
   * @example
   * // Request body
   * {
   *   "userId": 1,
   *   "collaboratorId": 2
   * }
   */
  @Post()
  async create(@Body() createFavoriteDto: CreateFavoriteDto) {
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
    @Param('userId') userId: number,
    @Param('collaboratorId') collaboratorId: number,
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
    @Param('userId') userId: number,
    @Param('collaboratorId') collaboratorId: number,
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
    @Param('userId') userId: number,
    @Param('collaboratorId') collaboratorId: number,
  ) {
    return this.favoritesService.remove(userId, collaboratorId);
  }

    // Favorite Collaborators 
    /**
   * Retrieves all favorite collaborators for a specific user.
   * @route GET /users/favorites/user_favorites/:id
   * @param id - The ID of the user
   * @returns Promise containing an array of the user's favorite collaborators
   */
  @Get('user_favorites/:id')
  async getFavoriteCollaborators(@Param('id') id: string) {
    return this.favoritesService.findByUser(+id)
  }

}
