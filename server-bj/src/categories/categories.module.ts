import { Module } from '@nestjs/common';
import { CategoriesService } from './categories.service';
import { CategoriesController } from './categories.controller';
import { Category } from './entities/category.entity';
import { TypeOrmModule } from '@nestjs/typeorm';

/**
 * Categories Module - Handles the category management functionality in the application.
 * 
 * This module encapsulates all category-related features including:
 * - Category entity registration with TypeORM
 * - Category controller for handling HTTP requests
 * - Category service for business logic
 * 
 * @module CategoriesModule
 * 
 * @imports
 * - TypeOrmModule.forFeature([Category]) - Registers the Category entity with TypeORM
 * 
 * @exports
 * - TypeOrmModule - Makes the Category repository available to other modules
 */
@Module({
  imports: [TypeOrmModule.forFeature([Category])],
  controllers: [CategoriesController],
  providers: [CategoriesService],
  exports: [TypeOrmModule, CategoriesService]
})
export class CategoriesModule {}
