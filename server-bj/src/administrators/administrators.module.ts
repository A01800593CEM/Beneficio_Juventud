import { forwardRef, Module } from '@nestjs/common';
import { AdministratorsService } from './administrators.service';
import { AdministratorsController } from './administrators.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Administrator } from './entities/administrator.entity';
import { UsersModule } from 'src/users/users.module';
import { PromotionsModule } from 'src/promotions/promotions.module';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';

@Module({
  imports: [
    TypeOrmModule.forFeature([Administrator]), 
    forwardRef(() => UsersModule), 
    forwardRef(() => PromotionsModule), 
    forwardRef(() => CollaboratorsModule)],
  controllers: [AdministratorsController],
  providers: [AdministratorsService],
})
export class AdministratorsModule {}
