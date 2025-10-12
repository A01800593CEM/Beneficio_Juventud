import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Favorite } from './entities/favorite.entity';
import { CreateFavoriteDto } from './dto/create-favorite.dto';
import { UpdateFavoriteDto } from './dto/update-favorite.dto';
import { Collaborator } from 'src/collaborators/entities/collaborator.entity';

/**
 * Service responsible for managing favorite relationships between users and collaborators.
 * Handles business logic for creating, retrieving, updating, and removing favorites.
 */
@Injectable()
export class FavoritesService {
  /**
   * Creates an instance of FavoritesService.
   * @param favoritesRepository - The TypeORM repository for Favorite entities
   */
  constructor(
    @InjectRepository(Favorite)
    private favoritesRepository: Repository<Favorite>,
  ) {}

  /**
   * Creates a new favorite relationship.
   * @param createFavoriteDto - Data transfer object containing user and collaborator IDs
   * @returns Promise<Favorite> The newly created favorite relationship
   */
  async create(createFavoriteDto: CreateFavoriteDto): Promise<Favorite> {
    const favorite = this.favoritesRepository.create(createFavoriteDto);
    return this.favoritesRepository.save(favorite);
  }

  /**
   * Retrieves all favorite relationships with their associated users and collaborators.
   * @returns Promise<Favorite[]> Array of all favorite relationships
   */
  async findAll(): Promise<Favorite[]> {
    return this.favoritesRepository.find({
      relations: ['user', 'collaborator'],
    });
  }

  /**
   * Finds a specific favorite relationship by user and collaborator IDs.
   * @param userId - The ID of the user
   * @param collaboratorId - The ID of the collaborator
   * @returns Promise<Favorite> The found favorite relationship
   * @throws NotFoundException if the favorite relationship doesn't exist
   */
  async findOne(userId: number, collaboratorId: number): Promise<Favorite | null> {
    const favorite = await this.favoritesRepository.findOne({
      where: { userId, collaboratorId },
      relations: ['user', 'collaborator'],
    });

    if (!favorite) {
      throw new NotFoundException(
        `Favorite not found for userId ${userId} and collaboratorId ${collaboratorId}`,
      );
    }

    return favorite;
  }

  /**
   * Updates a favorite relationship.
   * @param userId - The ID of the user
   * @param collaboratorId - The ID of the collaborator
   * @param updateFavoriteDto - Data transfer object containing updated information
   * @returns Promise<Favorite> The updated favorite relationship
   * @throws NotFoundException if the favorite relationship doesn't exist
   */
  async update(
    userId: number,
    collaboratorId: number,
    updateFavoriteDto: UpdateFavoriteDto,
  ): Promise<Favorite> {
    const favorite = await this.favoritesRepository.preload({
      userId,
      collaboratorId,
      ...updateFavoriteDto,
    });

    if (!favorite) {
      throw new NotFoundException(
        `Favorite not found for userId ${userId} and collaboratorId ${collaboratorId}`,
      );
    }

    return this.favoritesRepository.save(favorite);
  }

  /**
   * Removes a favorite relationship.
   * @param userId - The ID of the user
   * @param collaboratorId - The ID of the collaborator
   * @throws NotFoundException if the favorite relationship doesn't exist
   */
  async remove(userId: number, collaboratorId: number): Promise<void> {
    const result = await this.favoritesRepository.delete({ userId, collaboratorId });

    if (result.affected === 0) {
      throw new NotFoundException(
        `Favorite not found for userId ${userId} and collaboratorId ${collaboratorId}`,
      );
    }
  }

  // Optional convenience method: get all favorites by user
  /**
   * Retrieves all favorite collaborators for a specific user.
   * @param userId - The ID of the user
   * @returns Promise<Collaborator[]> Array of collaborators favorited by the user
   */
  async findByUser(userId: number): Promise<Collaborator[]> {
    const favorites = await this.favoritesRepository.find({
      where: { userId },
      relations: ['collaborator'],
    });
    return favorites.map(favorite => favorite.collaborator)
  }
}