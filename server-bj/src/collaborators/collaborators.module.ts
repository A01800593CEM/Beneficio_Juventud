import { Module, forwardRef } from '@nestjs/common';
import { CollaboratorsService } from './collaborators.service';
import { CollaboratorsController } from './collaborators.controller';
import { Collaborator } from './entities/collaborator.entity';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CategoriesModule } from 'src/categories/categories.module';
import { FavoritesModule } from 'src/favorites/favorites.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([Collaborator]),
    forwardRef(() => CategoriesModule),
    forwardRef(() => FavoritesModule)
],
  controllers: [CollaboratorsController],
  providers: [CollaboratorsService],
  exports: [TypeOrmModule]
})
export class CollaboratorsModule {}
