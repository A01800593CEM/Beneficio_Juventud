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

  async trueFindOne(id: number): Promise<User | null> {
    return this.usersRepository.findOne({ where: { id }});
  }

  async findOne(id: number): Promise<User | null> {
    const user = await this.usersRepository.findOne({ where: { id, accountState: UserState.ACTIVE }});
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    return user
  }

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

  async remove(id: number): Promise<void>{
    const user = await this.findOne(id);
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    await this.usersRepository.update(id, { accountState: UserState.INACTIVE });
  }

  async reActivate(id: number): Promise<User> {
    const user = await this.trueFindOne(id);
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    await this.usersRepository.update(id, { accountState: UserState.ACTIVE })
    return user;
  }

}
