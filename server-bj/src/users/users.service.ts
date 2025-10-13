import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from './entities/user.entity';
import { Repository, In } from 'typeorm';
import { BookingsService } from 'src/bookings/bookings.service';
import { UserState } from './enums/user-state.enum';
import { Category } from 'src/categories/entities/category.entity';

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
    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>,
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
    const { userPrefCategories, ...data } = createUserDto;

    const categoriesEntities = await this.categoriesRepository.findBy({
      name: In(userPrefCategories),
    });

    const user = this.usersRepository.create({
      ...data,
      categories: categoriesEntities,
    });
    
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

  async trueFindOne(cognitoId: string): Promise<User | null> {
    return this.usersRepository.findOne({ 
      where: { cognitoId },
      relations: ['categories',
                  'bookings',
                  'bookings.promotion',
                  'favorites',
                  'favorites.collaborator',
                  'redeemedcoupon',
                  'redeemedcoupon.promotion'
                ] });
  }

  /**
   * Retrieves an active user by ID.
   *
   * @param id - Unique identifier of the user.
   * @throws {NotFoundException} If no active user is found with the given ID.
   * @returns The matching active {@link User}.
   * @example await usersService.findOne(8);
   */
  async findOne(cognitoId: string): Promise<User | null> {
    const user = await this.usersRepository.findOne({ 
      where: { 
        cognitoId, 
        accountState: UserState.ACTIVE }});
    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    return user
  }

  async update(cognitoId: string, updateUserDto: UpdateUserDto): Promise<User>  {
    const user = await this.usersRepository.findOne({
      where: { cognitoId }, 
      relations: ['categories', 'favorites', 'bookings', 'redeemedcoupon']
    });

    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    
    const { userPrefCategories, ...updateData} = updateUserDto

    Object.assign(user, updateData)

    if (userPrefCategories && userPrefCategories.length > 0) {
      const categories = await this.categoriesRepository.findBy({ name: In(userPrefCategories) });
      user.categories = categories;
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
  async remove(cognitoId: string): Promise<void>{
    const user = await this.findOne(cognitoId);
    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    await this.usersRepository.update(cognitoId, { accountState: UserState.INACTIVE });
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
  async reActivate(cognitoId: string): Promise<User> {
    const user = await this.trueFindOne(cognitoId);
    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    await this.usersRepository.update(cognitoId, { accountState: UserState.ACTIVE })
    return user;
  }

}
