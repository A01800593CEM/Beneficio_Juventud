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
    const { userPrefCategories, ...data } = createUserDto;

    if (userPrefCategories) {
      const categoriesEntities = await this.categoriesRepository.findBy({
        name: In(userPrefCategories),
      });
    

    const user = this.usersRepository.create({
      ...data,
      categories: categoriesEntities,
    });

    return this.usersRepository.save(user)

  }
    else {
      const user = this.usersRepository.create({
      ...data
    });
    return this.usersRepository.save(user)
    }
    
  }

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
