import { Module, forwardRef } from '@nestjs/common';
import { CollaboratorsService } from './collaborators.service';
import { CollaboratorsController } from './collaborators.controller';
import { Collaborator } from './entities/collaborator.entity';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CategoriesModule } from 'src/categories/categories.module';
import { FavoritesModule } from 'src/favorites/favorites.module';
import { BranchModule } from 'src/branch/branch.module';
import { PromotionsModule } from 'src/promotions/promotions.module';

/**
 * Module responsible for managing collaborators functionality in the application.
 * 
 * This module encapsulates all collaborator-related features including:
 * - Collaborator entity registration with TypeORM
 * - Collaborator service and controller
 * - Integration with related modules (Categories, Favorites, Branch)
 * 
 * @module CollaboratorsModule
 * 
 * @imports
 * - TypeOrmModule.forFeature([Collaborator]) - Registers the Collaborator entity with TypeORM
 * - CategoriesModule (forwardRef) - For circular dependency handling with categories
 * - FavoritesModule (forwardRef) - For circular dependency handling with favorites
 * - BranchModule (forwardRef) - For circular dependency handling with branches
 * 
 * @exports
 * - TypeOrmModule - Makes the Collaborator repository available to other modules
 * - CollaboratorsService - Provides collaborator-related functionality to other modules
 */
@Module({
  imports: [
    TypeOrmModule.forFeature([Collaborator]),
    forwardRef(() => CategoriesModule),
    forwardRef(() => FavoritesModule),
    forwardRef(() => BranchModule) ,
    forwardRef(() => PromotionsModule)
],
  controllers: [CollaboratorsController],
  providers: [CollaboratorsService],
  exports: [TypeOrmModule, CollaboratorsService]
})
export class CollaboratorsModule {}
