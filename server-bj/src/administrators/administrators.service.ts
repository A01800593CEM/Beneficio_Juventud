import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Administrator } from './entities/administrator.entity';
import { CreateAdministratorDto } from './dto/create-administrator.dto';
import { UpdateAdministratorDto } from './dto/update-administrator.dto';

@Injectable()
export class AdministratorsService {
  constructor(
    @InjectRepository(Administrator)
    private administratorsRepository: Repository<Administrator>,
  ) {}

  async create(createAdministratorDto: CreateAdministratorDto): Promise<Administrator> {
    const administrator = this.administratorsRepository.create(createAdministratorDto);
    return this.administratorsRepository.save(administrator);
  }

  async findAll(): Promise<Administrator[]> {
    return this.administratorsRepository.find();
  }

  async findOne(cognitoId: string): Promise<Administrator | null> {
    const administrator = await this.administratorsRepository.findOne({ where: { cognitoId } });

    if (!administrator) {
      throw new NotFoundException(`Administrator with cognitoId ${cognitoId} not found`);
    }

    return administrator;
  }

  async update(cognitoId: string, updateAdministratorDto: UpdateAdministratorDto): Promise<Administrator> {
    const administrator = await this.administratorsRepository.preload({
      cognitoId,
      ...updateAdministratorDto,
    });

    if (!administrator) {
      throw new NotFoundException(`Administrator with cognitoId ${cognitoId} not found`);
    }

    return this.administratorsRepository.save(administrator);
  }

  async remove(cognitoId: string): Promise<void> {
    const result = await this.administratorsRepository.delete(cognitoId);

    if (result.affected === 0) {
      throw new NotFoundException(`Administrator with cognitoId ${cognitoId} not found`);
    }
  }

}
