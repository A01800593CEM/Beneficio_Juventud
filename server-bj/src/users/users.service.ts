import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from './entities/user.entity';
import { Repository, In } from 'typeorm';
import { BookingsService } from 'src/bookings/bookings.service';
import { UserState } from './enums/user-state.enum';
import { Category } from 'src/categories/entities/category.entity';
import { Promotion } from 'src/promotions/entities/promotion.entity';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>,
    @InjectRepository(Promotion)
    private promotionsRepository: Repository<Promotion>,
    private bookingsService: BookingsService,
  ) {}
  
  async create(createUserDto: CreateUserDto): Promise<User> {
    const { userPrefCategories, favoritePromos, ...data } = createUserDto;
    
    let validUserPrefCategories: Category[] = userPrefCategories ? 
    await this.categoriesRepository.findBy({
        name: In(userPrefCategories),
      }) : [];

    let validUserFavoritePromos: Promotion[] = favoritePromos ? 
    await this.promotionsRepository.findBy({
        promotionId: In(favoritePromos),
      }) : [];
    

    const user = this.usersRepository.create({
      ...data,
      categories: validUserPrefCategories,
      favoritePromos: validUserFavoritePromos
    });

    return this.usersRepository.save(user)
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
                  'redeemedcoupon.promotion',
                  'favoritePromos'
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
      relations: ['categories', 'favorites', 'bookings', 'redeemedcoupon', 'favoritePromos']
    });

    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    
    const { userPrefCategories, favoritePromos, ...updateData} = updateUserDto

    Object.assign(user, updateData)

    if (userPrefCategories && userPrefCategories.length > 0) {
      const categories = await this.categoriesRepository.findBy({ name: In(userPrefCategories) });
      user.categories = categories;
    }

    if (favoritePromos && favoritePromos.length > 0) {
      const promotions = await this.promotionsRepository.findBy({ promotionId: In( favoritePromos )});
      user.favoritePromos = promotions;
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

  async getFavoritePromos(cognitoId: string): Promise<Promotion[]> {
    const user = await this.trueFindOne(cognitoId);
    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    };
    return user.favoritePromos;
  }

  async addFavoritePromo(cognitoId: string, promotionId: number): Promise<void> {
    const user = await this.trueFindOne(cognitoId);

    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    };

    const promotion = await this.promotionsRepository.findBy({ promotionId: promotionId })
    user.favoritePromos.concat(promotion);
  }

  async remFavoritePromo(cognitoId: string, promotionId: number,): Promise<void> {
  await this.usersRepository
    .createQueryBuilder()
    .relation(User, 'favoritePromos')
    .of(cognitoId) // refers to user.cognitoId
    .remove(promotionId); // refers to promotion.promotionId
}


}
