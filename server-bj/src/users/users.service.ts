import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from './entities/user.entity';
import { Repository } from 'typeorm';
import { BookingsService } from 'src/bookings/bookings.service';
import { UserState } from './enums/user-state.enum';

/**
 * Service that encapsulates all user management operations.
 *
 * @remarks
 * This service is responsible for creating, retrieving, updating, and logically deleting users.
 * It interacts directly with the {@link User} repository and handles account state transitions
 * such as activation and deactivation.
 */
@Injectable()
export class UsersService {
  /**
   * Creates an instance of the UsersService.
   *
   * @param usersRepository - TypeORM repository for managing {@link User} entities.
   * @param bookingsService - Service used to manage related user bookings.
   */
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    private bookingsService: BookingsService,
  ) {}

  /**
   * Creates a new user and persists it to the database.
   *
   * @param createUserDto - DTO containing the new user's information.
   * @returns The created {@link User}.
   *
   * @example
   * await usersService.create({
   *   name: 'Iván',
   *   lastNamePaternal: 'Carrillo',
   *   lastNameMaternal: 'López',
   *   birthDate: new Date('2001-08-15'),
   *   phoneNumber: '+52 5512345678',
   *   email: 'ivan@example.com',
   *   accountState: UserState.ACTIVE,
   * });
   */
  async create(createUserDto: CreateUserDto): Promise<User> {
    const user = this.usersRepository.create(createUserDto)
    return this.usersRepository.save(user)
  }

   /**
   * Retrieves all users in the system.
   *
   * @returns A Promise resolving to an array of {@link User} entities.
   * @example await usersService.findAll();
   */
  async findAll(): Promise<User[]> {
    return this.usersRepository.find();
  }

  /**
   * Retrieves a user by ID (without filtering by account state).
   *
   * @param id - Unique identifier of the user.
   * @returns The matching {@link User} or `null` if not found.
   * @example await usersService.trueFindOne(15);
   */
  async trueFindOne(id: number): Promise<User | null> {
    return this.usersRepository.findOne({ where: { id }});
  }

  /**
   * Retrieves an active user by ID.
   *
   * @param id - Unique identifier of the user.
   * @throws {NotFoundException} If no active user is found with the given ID.
   * @returns The matching active {@link User}.
   * @example await usersService.findOne(8);
   */
  async findOne(id: number): Promise<User | null> {
    const user = await this.usersRepository.findOne({ where: { id, accountState: UserState.ACTIVE }});
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    return user
  }

   /**
   * Updates a user's details.
   *
   * @param id - The ID of the user to update.
   * @param updateUserDto - DTO containing the updated data.
   * @throws {NotFoundException} If the user does not exist.
   * @returns The updated {@link User}.
   *
   * @example
   * await usersService.update(5, { phoneNumber: '+52 5587654321' });
   */
  async update(id: number, updateUserDto: UpdateUserDto): Promise<User | null>  {
    const user = await this.usersRepository.preload({
      id,
      ...updateUserDto
    });

    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    
    return this.usersRepository.save(user)
  }

   /**
   * Deactivates a user account (logical deletion).
   *
   * @param id - ID of the user to deactivate.
   * @throws {NotFoundException} If the user does not exist.
   * @returns A void Promise after deactivation.
   *
   * @example
   * await usersService.remove(7);
   */
  async remove(id: number): Promise<void>{
    const user = await this.findOne(id);
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    await this.usersRepository.update(id, { accountState: UserState.INACTIVE });
  }

  /**
   * Reactivates a previously deactivated user account.
   *
   * @param id - ID of the user to reactivate.
   * @throws {NotFoundException} If the user does not exist.
   * @returns The reactivated {@link User}.
   *
   * @example
   * await usersService.reActivate(7);
   */
  async reActivate(id: number): Promise<User> {
    const user = await this.trueFindOne(id);
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    await this.usersRepository.update(id, { accountState: UserState.ACTIVE })
    return user;
  }

}
