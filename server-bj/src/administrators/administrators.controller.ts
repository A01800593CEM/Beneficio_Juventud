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

/**
 * Controller responsible for handling administrative operations.
 * Provides endpoints for managing administrators, users, promotions, and collaborators.
 * @route /admin
 */
@Controller('admin')
export class AdministratorsController {
  /**
   * Creates an instance of AdministratorsController.
   * @param administratorsService - Service for managing administrators
   * @param usersService - Service for managing users
   * @param promotionsService - Service for managing promotions
   * @param collaboratorsService - Service for managing collaborators
   */
  constructor(
    private readonly administratorsService: AdministratorsService,
    private readonly usersService: UsersService,
    private readonly promotionsService: PromotionsService,
    private readonly collaboratorsService: CollaboratorsService,
  ) {}

  // ----------------------------
  // ADMINISTRATORS CRUD
  // ----------------------------
  
  /**
   * Creates a new administrator.
   * @route POST /admin/administrators
   * @param dto - Data transfer object containing administrator details
   * @returns The newly created administrator
   */
  @Post('administrators')
  createAdministrator(@Body() dto: CreateAdministratorDto) {
    return this.administratorsService.create(dto);
  }

  /**
   * Retrieves all administrators.
   * @route GET /admin/administrators
   * @returns Array of all administrators
   */
  @Get('administrators')
  findAllAdministrators() {
    return this.administratorsService.findAll();
  }

  /**
   * Retrieves a specific administrator by ID.
   * @route GET /admin/administrators/:id
   * @param id - The unique identifier of the administrator
   * @returns The administrator with the specified ID
   */
  @Get('administrators/:id')
  findOneAdministrator(@Param('id', ParseIntPipe) id: string) {
    return this.administratorsService.findOne(id);
  }

  /**
   * Updates an existing administrator.
   * @route PATCH /admin/administrators/:id
   * @param id - The unique identifier of the administrator to update
   * @param dto - Data transfer object containing updated administrator details
   * @returns The updated administrator
   */
  @Patch('administrators/:id')
  updateAdministrator(
    @Param('id', ParseIntPipe) id: string,
    @Body() dto: UpdateAdministratorDto,
  ) {
    return this.administratorsService.update(id, dto);
  }

  /**
   * Removes an administrator.
   * @route DELETE /admin/administrators/:id
   * @param id - The unique identifier of the administrator to remove
   * @returns void on successful deletion
   */
  @Delete('administrators/:id')
  removeAdministrator(@Param('id', ParseIntPipe) id: string) {
    return this.administratorsService.remove(id);
  }

  // ----------------------------
  // USERS CRUD
  // ----------------------------

  /**
   * Creates a new user.
   * @route POST /admin/users
   * @param dto - Data transfer object containing user details
   * @returns The newly created user
   */
  @Post('users')
  createUser(@Body() dto: CreateUserDto) {
    return this.usersService.create(dto);
  }

  /**
   * Retrieves all users.
   * @route GET /admin/users
   * @returns Array of all users
   */
  @Get('users')
  findAllUsers() {
    return this.usersService.findAll();
  }

  /**
   * Retrieves a specific user by ID.
   * @route GET /admin/users/:id
   * @param id - The unique identifier of the user
   * @returns The user with the specified ID
   */
  @Get('users/:id')
  findOneUser(@Param('id', ParseIntPipe) id: string) {
    return this.usersService.trueFindOne(id);
  }

  /**
   * Updates an existing user.
   * @route PATCH /admin/users/:id
   * @param id - The unique identifier of the user to update
   * @param dto - Data transfer object containing updated user details
   * @returns The updated user
   */
  @Patch('users/:id')
  updateUser(@Param('id', ParseIntPipe) id: string, @Body() dto: UpdateUserDto) {
    return this.usersService.update(id, dto);
  }

  /**
   * Reactivates a previously deactivated user.
   * @route PATCH /admin/users/reactivate/:id
   * @param id - The unique identifier of the user to reactivate
   * @returns The reactivated user
   */
  @Patch('users/reactivate/:id')
  reactivateUser(@Param('id', ParseIntPipe) id: string) {
    return this.usersService.reActivate(id);
  }

  /**
   * Removes a user.
   * @route DELETE /admin/users/:id
   * @param id - The unique identifier of the user to remove
   * @returns void on successful deletion
   */
  @Delete('users/:id')
  removeUser(@Param('id', ParseIntPipe) id: string) {
    return this.usersService.remove(id);
  }


  // ----------------------------
  // PROMOTIONS CRUD
  // ----------------------------

  /**
   * Creates a new promotion.
   * @route POST /admin/promotions
   * @param dto - Data transfer object containing promotion details
   * @returns The newly created promotion
   */
  @Post('promotions')
  createPromotion(@Body() dto: CreatePromotionDto) {
    return this.promotionsService.create(dto);
  }

  /**
   * Retrieves all promotions.
   * @route GET /admin/promotions
   * @returns Array of all promotions
   */
  @Get('promotions')
  findAllPromotions() {
    return this.promotionsService.findAll();
  }

  /**
   * Retrieves a specific promotion by ID.
   * @route GET /admin/promotions/:id
   * @param id - The unique identifier of the promotion
   * @returns The promotion with the specified ID
   */
  @Get('promotions/:id')
  findOnePromotion(@Param('id', ParseIntPipe) id: number) {
    return this.promotionsService.findOne(id);
  }

  /**
   * Updates an existing promotion.
   * @route PATCH /admin/promotions/:id
   * @param id - The unique identifier of the promotion to update
   * @param dto - Data transfer object containing updated promotion details
   * @returns The updated promotion
   */
  @Patch('promotions/:id')
  updatePromotion(
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: UpdatePromotionDto,
  ) {
    return this.promotionsService.update(id, dto);
  }

  /**
   * Removes a promotion.
   * @route DELETE /admin/promotions/:id
   * @param id - The unique identifier of the promotion to remove
   * @returns void on successful deletion
   */
  @Delete('promotions/:id')
  removePromotion(@Param('id', ParseIntPipe) id: number) {
    return this.promotionsService.remove(id);
  }



  // ----------------------------
  // COLLABORATORS CRUD
  // ----------------------------

  /**
   * Creates a new collaborator.
   * @route POST /admin/collaborators
   * @param dto - Data transfer object containing collaborator details
   * @returns The newly created collaborator
   */
  @Post('collaborators')
  createCollaborator(@Body() dto: CreateCollaboratorDto) {
    return this.collaboratorsService.create(dto);
  }

  /**
   * Retrieves all collaborators.
   * @route GET /admin/collaborators
   * @returns Array of all collaborators
   */
  @Get('collaborators')
  findAllCollaborators() {
    return this.collaboratorsService.findAll();
  }

  /**
   * Retrieves a specific collaborator by ID.
   * @route GET /admin/collaborators/:id
   * @param id - The unique identifier of the collaborator
   * @returns The collaborator with the specified ID
   */
  @Get('collaborators/:id')
  findOneCollaborator(@Param('id', ParseIntPipe) id: string) {
    return this.collaboratorsService.trueFindOne(id);
  }

  /**
   * Updates an existing collaborator.
   * @route PATCH /admin/collaborators/:id
   * @param id - The unique identifier of the collaborator to update
   * @param dto - Data transfer object containing updated collaborator details
   * @returns The updated collaborator
   */
  @Patch('collaborators/:id')
  updateCollaborator(
    @Param('id', ParseIntPipe) id: string,
    @Body() dto: UpdateCollaboratorDto,
  ) {
    return this.collaboratorsService.update(id, dto);
  }

  /**
   * Reactivates a previously deactivated collaborator.
   * @route PATCH /admin/collaborators/reactivate/:id
   * @param id - The unique identifier of the collaborator to reactivate
   * @returns The reactivated collaborator
   */
  @Patch('collaborators/reactivate/:id')
  reactivateCollaborator(@Param('id', ParseIntPipe) id: string) {
    return this.collaboratorsService.reActivate(id)
  }

  /**
   * Removes a collaborator.
   * @route DELETE /admin/collaborators/:id
   * @param id - The unique identifier of the collaborator to remove
   * @returns void on successful deletion
   */
  @Delete('collaborators/:id')
  removeCollaborator(@Param('id', ParseIntPipe) id: string) {
    return this.collaboratorsService.remove(id);
  }
}
