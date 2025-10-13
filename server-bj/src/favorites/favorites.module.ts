import { Module, forwardRef } from '@nestjs/common';
import { FavoritesService } from './favorites.service';
import { FavoritesController } from './favorites.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Favorite } from './entities/favorite.entity';
import { UsersModule } from 'src/users/users.module';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';

/**
 * Module responsible for managing favorite relationships between users and collaborators.
 * 
 * This module encapsulates all favorite-related features including:
 * - Favorite entity registration with TypeORM
 * - Favorite service and controller implementation
 * - Integration with Users and Collaborators modules
 * 
 * @module FavoritesModule
 * 
 * @imports
 * - TypeOrmModule.forFeature([Favorite]) - Registers the Favorite entity with TypeORM
 * - UsersModule (forwardRef) - For circular dependency handling with users
 * - CollaboratorsModule (forwardRef) - For circular dependency handling with collaborators
 * 
 * @exports
 * - TypeOrmModule - Makes the Favorite repository available to other modules
 * - FavoritesService - Provides favorite management functionality to other modules
 * 
 * @description
 * Uses forwardRef() to handle circular dependencies with Users and Collaborators modules,
 * as favorites create relationships between these entities.
 */
@Module({
  controllers: [FavoritesController],
  providers: [FavoritesService],
  imports: [
    TypeOrmModule.forFeature([Favorite]),
    forwardRef(() => UsersModule),
    forwardRef(() => CollaboratorsModule)],
  exports: [TypeOrmModule, FavoritesService]
})
export class FavoritesModule {}
