import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Favorite } from './entities/favorite.entity';
import { CreateFavoriteDto } from './dto/create-favorite.dto';
import { UpdateFavoriteDto } from './dto/update-favorite.dto';
import { Collaborator } from 'src/collaborators/entities/collaborator.entity';

@Injectable()
export class FavoritesService {
  constructor(
    @InjectRepository(Favorite)
    private favoritesRepository: Repository<Favorite>,
  ) {}

  async create(createFavoriteDto: CreateFavoriteDto): Promise<Favorite> {
    const favorite = this.favoritesRepository.create(createFavoriteDto);
    return this.favoritesRepository.save(favorite);
  }

  async findAll(): Promise<Favorite[]> {
    return this.favoritesRepository.find({
      relations: ['user', 'collaborator'],
    });
  }

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

  async remove(userId: number, collaboratorId: number): Promise<void> {
    const result = await this.favoritesRepository.delete({ userId, collaboratorId });

    if (result.affected === 0) {
      throw new NotFoundException(
        `Favorite not found for userId ${userId} and collaboratorId ${collaboratorId}`,
      );
    }
  }

  // Optional convenience method: get all favorites by user
  async findByUser(userId: number): Promise<Collaborator[]> {
    const favorites = await this.favoritesRepository.find({
      where: { userId },
      relations: ['collaborator'],
    });
    return favorites.map(favorite => favorite.collaborator)
  }
}