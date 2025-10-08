import { Module } from '@nestjs/common';
import { CollaboratorsService } from './collaborators.service';
import { CollaboratorsController } from './collaborators.controller';
import { Collaborator } from './entities/collaborator.entity';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CategoriesModule } from 'src/categories/categories.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([Collaborator]),
    CategoriesModule
],
  controllers: [CollaboratorsController],
  providers: [CollaboratorsService],
  exports: [TypeOrmModule]
})
export class CollaboratorsModule {}
