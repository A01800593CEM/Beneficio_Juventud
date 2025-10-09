import { Module, forwardRef } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UsersService } from './users.service';
import { UsersController } from './users.controller';
import { User } from './entities/user.entity';
import { BookingsModule } from '../bookings/bookings.module';
import { PromotionsModule } from 'src/promotions/promotions.module';
import { FavoritesModule } from 'src/favorites/favorites.module';

@Module({
  imports: [TypeOrmModule.forFeature([User]),
  forwardRef(() => BookingsModule),
  forwardRef(() => PromotionsModule),
  forwardRef(() => FavoritesModule)], 
  controllers: [UsersController],
  providers: [UsersService],
  exports:[TypeOrmModule]
})
export class UsersModule {}
