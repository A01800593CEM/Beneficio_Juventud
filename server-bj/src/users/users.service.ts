import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from './entities/user.entity';
import { Repository } from 'typeorm';
import { BookingsService } from 'src/bookings/bookings.service';
import { UserState } from './enums/user-state.enum';


@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>,
    private bookingsService: BookingsService,
  ) {}
  
  async create(createUserDto: CreateUserDto): Promise<User> {
    const user = this.usersRepository.create(createUserDto)
    return this.usersRepository.save(user)
  }

  async findAll(): Promise<User[]> {
    return this.usersRepository.find();
  }

  async trueFindOne(cognitoId: string): Promise<User | null> {
    return this.usersRepository.findOne({ where: { cognitoId }});
  }

  async findOne(cognitoId: string): Promise<User | null> {
    const user = await this.usersRepository.findOne({ where: { cognitoId, accountState: UserState.ACTIVE }});
    if (!user) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    return user
  }

  async update(cognitoId: string, updateUserDto: UpdateUserDto): Promise<User | null>  {
    const user = await this.usersRepository.preload({
      cognitoId,
      ...updateUserDto
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
