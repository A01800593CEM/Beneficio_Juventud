import { Module, forwardRef } from '@nestjs/common';
import { CollaboratorsService } from './collaborators.service';
import { CollaboratorsController } from './collaborators.controller';
import { Collaborator } from './entities/collaborator.entity';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CategoriesModule } from 'src/categories/categories.module';
import { FavoritesModule } from 'src/favorites/favorites.module';
import { BranchModule } from 'src/branch/branch.module';
import { PromotionsModule } from 'src/promotions/promotions.module';

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
