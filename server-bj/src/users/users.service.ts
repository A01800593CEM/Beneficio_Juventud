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
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>,
    private bookingsService: BookingsService,
  ) {}
  
  async create(createUserDto: CreateUserDto): Promise<User> {
    const { categories: userPrefCategories, ...data } = createUserDto;

    const categoriesEntities = await this.categoriesRepository.findBy({
      name: In(userPrefCategories),
    });

    const user = this.usersRepository.create({
      ...data,
      categories: categoriesEntities,
    });
    
    return this.usersRepository.save(user)
  }

  async findAll(): Promise<User[]> {
    return this.usersRepository.find();
  }

  async trueFindOne(cognitoId: string): Promise<User | null> {
    return this.usersRepository.findOne({ 
      where: { cognitoId },
      relations: ['bookings',
                  'bookings.promotion',
                  'favorites',
                  'favorites.collaborator',
                  'favorites.collaborator.categories',
                  'redeemedcoupon',
                  'redeemedcoupon.coupon',
                  'userPrefCategories'
                ] });
  }

  async findOne(cognitoId: string): Promise<User | null> {
    const user = await this.usersRepository.findOne({ 
      where: { 
        cognitoId, 
        accountState: UserState.ACTIVE },
      relations: ['bookings',
                  'favorites',
                  'favorites.collaborator',
                  'favorites.collaborator.categories',
                  'redeemedcoupon',
                  'redeemedcoupon.promotion',
                  'categories'
                ] });
    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    return user
  }

  async update(cognitoId: string, updateUserDto: UpdateUserDto): Promise<User>  {
    const user = await this.usersRepository.findOne({
      where: { cognitoId }, 
      relations: ['userPrefCategories', 'favorites', 'bookings', 'redeemedcoupon']
    });

    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    
    return this.usersRepository.save(user)
  }

  async remove(cognitoId: string): Promise<void>{
    const user = await this.findOne(cognitoId);
    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    await this.usersRepository.update(cognitoId, { accountState: UserState.INACTIVE });
  }

  async reActivate(cognitoId: string): Promise<User> {
    const user = await this.trueFindOne(cognitoId);
    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    await this.usersRepository.update(cognitoId, { accountState: UserState.ACTIVE })
    return user;
  }

}
