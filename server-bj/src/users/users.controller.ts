import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { UsersService } from './users.service';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { FavoritesService } from 'src/favorites/favorites.service';

/**
 * Controller providing REST endpoints for managing users.
 *
 * @remarks
 * This controller handles CRUD operations for the {@link User} entity.
 * It delegates business logic to the {@link UsersService} and may
 * also use {@link FavoritesService} to manage user-favorite relationships.
 *
 * Routes:
 * - `POST /users` — Create a new user.
 * - `GET /users` — Retrieve all users.
 * - `GET /users/:id` — Retrieve a specific user by ID.
 * - `PATCH /users/:id` — Update an existing user.
 * - `DELETE /users/:id` — Remove a user record.
 */
@Controller('users')
export class UsersController {
  /**
   * Creates an instance of the UsersController.
   *
   * @param usersService - Service handling user business logic.
   * @param favoritesService - Service managing user favorites.
   */
  constructor(
    private readonly usersService: UsersService,
    private readonly favoritesService: FavoritesService) {}

  // CRUD Endpoints
  /**
   * Creates a new user.
   *
   * @param createUserDto - DTO containing user registration data.
   * @returns The newly created user record.
   *
   * @example
   * POST /users
   * ```json
   * {
   *   "name": "Iván",
   *   "lastNamePaternal": "Carrillo",
   *   "lastNameMaternal": "López",
   *   "birthDate": "2001-08-15T00:00:00.000Z",
   *   "phoneNumber": "+52 5512345678",
   *   "email": "ivan@example.com",
   *   "accountState": "activo"
   * }
   * ```
   */
  @Post()
  create(@Body() createUserDto: CreateUserDto) {
    return this.usersService.create(createUserDto);
  }

  /**
   * Retrieves all users in the system.
   *
   * @returns A list of user records.
   * @example GET /users
   */
  @Get()
  findAll() {
    return this.usersService.findAll();
  }

  /**
   * Retrieves a specific user by ID.
   *
   * @param id - The unique identifier of the user.
   * @returns The user record if found.
   * @example GET /users/12
   */
  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.usersService.findOne(id);
  }

  /**
   * Updates user information.
   *
   * @param id - The ID of the user to update.
   * @param updateUserDto - DTO containing updated user fields.
   * @returns The updated user record.
   *
   * @example
   * PATCH /users/12
   * ```json
   * {
   *   "phoneNumber": "+52 5587654321"
   * }
   * ```
   */
  @Patch(':id')
  update(@Param('id') cognitoId: string, @Body() updateUserDto: UpdateUserDto) {
    return this.usersService.update(cognitoId, updateUserDto);
  }

  /**
   * Deletes a user by ID.
   *
   * @param id - The ID of the user to delete.
   * @returns A confirmation or void when deletion is successful.
   * @example DELETE /users/12
   */
  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.usersService.remove(id);
  }
}
