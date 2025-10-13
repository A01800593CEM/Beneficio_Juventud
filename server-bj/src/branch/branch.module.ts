import { forwardRef, Module } from '@nestjs/common';
import { BranchService } from './branch.service';
import { BranchController } from './branch.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Branch } from './entities/branch.entity';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';

/**
 * Module responsible for managing branch-related functionality.
 * Handles the registration of controllers, services, and dependencies related to branches.
 * 
 * @module BranchModule
 * @description
 * This module includes:
 * - BranchController for handling HTTP requests
 * - BranchService for business logic
 * - TypeORM integration for Branch entity
 * - Circular dependency handling with CollaboratorsModule
 */
@Module({
  controllers: [BranchController],
  providers: [BranchService],
  imports: [
    TypeOrmModule.forFeature([Branch]),
    forwardRef(() => CollaboratorsModule) // Resolves circular dependency with Collaborators
  ],
  exports: [BranchModule] // Makes BranchModule available for importing by other modules
})
export class BranchModule {}
