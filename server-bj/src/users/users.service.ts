import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { User } from './entities/user.entity';
import { Repository } from 'typeorm';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>
  ) {}
  
  async create(createUserDto: CreateUserDto) {
    const user = this.usersRepository.create(createUserDto)
    return this.usersRepository.insert(user)
  }

  async findAll(): Promise<User[]> {
    return this.usersRepository.find();
  }

  async findOne(id: number): Promise<User | null> {
    return this.usersRepository.findOne({ where: { id }});
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
    await this.usersRepository.delete(id);
  }
}
