import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Patch,
  Delete,
  ParseIntPipe,
} from '@nestjs/common';
import { AdministratorsService } from '../administrators/administrators.service';
import { UsersService } from '../users/users.service';
import { PromotionsService } from '../promotions/promotions.service';
import { CollaboratorsService } from '../collaborators/collaborators.service';
import { CreateAdministratorDto } from '../administrators/dto/create-administrator.dto';
import { UpdateAdministratorDto } from '../administrators/dto/update-administrator.dto';
import { CreateUserDto } from '../users/dto/create-user.dto';
import { UpdateUserDto } from '../users/dto/update-user.dto';
import { CreatePromotionDto } from '../promotions/dto/create-promotion.dto';
import { UpdatePromotionDto } from '../promotions/dto/update-promotion.dto';
import { CreateCollaboratorDto } from '../collaborators/dto/create-collaborator.dto';
import { UpdateCollaboratorDto } from '../collaborators/dto/update-collaborator.dto';

@Controller('admin')
export class AdministratorsController {
  constructor(
    private readonly administratorsService: AdministratorsService,
    private readonly usersService: UsersService,
    private readonly promotionsService: PromotionsService,
    private readonly collaboratorsService: CollaboratorsService,
  ) {}

  // ----------------------------
  // ADMINISTRATORS CRUD
  // ----------------------------
  @Post('administrators')
  createAdministrator(@Body() dto: CreateAdministratorDto) {
    return this.administratorsService.create(dto);
  }

  @Get('administrators')
  findAllAdministrators() {
    return this.administratorsService.findAll();
  }

  @Get('administrators/:id')
  findOneAdministrator(@Param('id', ParseIntPipe) id: number) {
    return this.administratorsService.findOne(id);
  }

  @Patch('administrators/:id')
  updateAdministrator(
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: UpdateAdministratorDto,
  ) {
    return this.administratorsService.update(id, dto);
  }

  @Delete('administrators/:id')
  removeAdministrator(@Param('id', ParseIntPipe) id: number) {
    return this.administratorsService.remove(id);
  }

  // ----------------------------
  // USERS CRUD
  // ----------------------------
  @Post('users')
  createUser(@Body() dto: CreateUserDto) {
    return this.usersService.create(dto);
  }

  @Get('users')
  findAllUsers() {
    return this.usersService.findAll();
  }

  @Get('users/:id')
  findOneUser(@Param('id', ParseIntPipe) id: string) {
    return this.usersService.trueFindOne(id);
  }

  @Patch('users/:id')
  updateUser(@Param('id', ParseIntPipe) id: string, @Body() dto: UpdateUserDto) {
    return this.usersService.update(id, dto);
  }

  @Patch('users/reactivate/:id')
  reactivateUser(@Param('id', ParseIntPipe) id: string) {
    return this.usersService.reActivate(id);
  }

  @Delete('users/:id')
  removeUser(@Param('id', ParseIntPipe) id: string) {
    return this.usersService.remove(id);
  }


  // ----------------------------
  // PROMOTIONS CRUD
  // ----------------------------
  @Post('promotions')
  createPromotion(@Body() dto: CreatePromotionDto) {
    return this.promotionsService.create(dto);
  }

  @Get('promotions')
  findAllPromotions() {
    return this.promotionsService.findAll();
  }

  @Get('promotions/:id')
  findOnePromotion(@Param('id', ParseIntPipe) id: number) {
    return this.promotionsService.findOne(id);
  }

  @Patch('promotions/:id')
  updatePromotion(
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: UpdatePromotionDto,
  ) {
    return this.promotionsService.update(id, dto);
  }

  @Delete('promotions/:id')
  removePromotion(@Param('id', ParseIntPipe) id: number) {
    return this.promotionsService.remove(id);
  }



  // ----------------------------
  // COLLABORATORS CRUD
  // ----------------------------
  @Post('collaborators')
  createCollaborator(@Body() dto: CreateCollaboratorDto) {
    return this.collaboratorsService.create(dto);
  }

  @Get('collaborators')
  findAllCollaborators() {
    return this.collaboratorsService.findAll();
  }

  @Get('collaborators/:id')
  findOneCollaborator(@Param('id', ParseIntPipe) id: string) {
    return this.collaboratorsService.trueFindOne(id);
  }

  @Patch('collaborators/:id')
  updateCollaborator(
    @Param('id', ParseIntPipe) id: string,
    @Body() dto: UpdateCollaboratorDto,
  ) {
    return this.collaboratorsService.update(id, dto);
  }

  @Patch('collaborators/reactivate/:id')
  reactivateCollaborator(@Param('id', ParseIntPipe) id: string) {
    return this.collaboratorsService.reActivate(id)
  }

  @Delete('collaborators/:id')
  removeCollaborator(@Param('id', ParseIntPipe) id: string) {
    return this.collaboratorsService.remove(id);
  }
}
