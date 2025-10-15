import { forwardRef, Module } from '@nestjs/common';
import { AdministratorsService } from './administrators.service';
import { AdministratorsController } from './administrators.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Administrator } from './entities/administrator.entity';
import { UsersModule } from 'src/users/users.module';
import { PromotionsModule } from 'src/promotions/promotions.module';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';

/**
 * Module responsible for managing administrator functionality.
 * Handles the registration of controllers, services, and dependencies related to administrators.
 * 
 * @module AdministratorsModule
 * @description
 * This module includes:
 * - AdministratorsController for handling HTTP requests
 * - AdministratorsService for business logic
 * - TypeORM integration for Administrator entity
 * - Integration with Users, Promotions, and Collaborators modules
 * 
 * Features:
 * - Administrator management
 * - User management through admin interface
 * - Promotion management through admin interface
 * - Collaborator management through admin interface
 * 
 * Note: Uses forwardRef to handle circular dependencies with related modules
 */
@Module({
  imports: [
    TypeOrmModule.forFeature([Administrator]), // Database integration for Administrator entity
    forwardRef(() => UsersModule),            // Circular dependency with Users
    forwardRef(() => PromotionsModule),       // Circular dependency with Promotions
    forwardRef(() => CollaboratorsModule)     // Circular dependency with Collaborators
  ],
  controllers: [AdministratorsController],
  providers: [AdministratorsService],
})
export class AdministratorsModule {}
