import { Module, forwardRef } from '@nestjs/common';
import { FavoritesService } from './favorites.service';
import { FavoritesController } from './favorites.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Favorite } from './entities/favorite.entity';
import { UsersModule } from 'src/users/users.module';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';

@Module({
  controllers: [FavoritesController],
  providers: [FavoritesService],
  imports: [
    TypeOrmModule.forFeature([Favorite]),
    forwardRef(() => UsersModule),
    forwardRef(() => CollaboratorsModule)],
  exports: [TypeOrmModule]
})
export class FavoritesModule {}
