import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Administrator } from './entities/administrator.entity';
import { CreateAdministratorDto } from './dto/create-administrator.dto';
import { UpdateAdministratorDto } from './dto/update-administrator.dto';

/**
 * Service responsible for managing administrator-related operations.
 * Provides methods for CRUD operations and business logic related to administrators.
 */
@Injectable()
export class AdministratorsService {
  /**
   * Creates an instance of AdministratorsService.
   * @param administratorsRepository - The TypeORM repository for Administrator entities
   */
  constructor(
    @InjectRepository(Administrator)
    private administratorsRepository: Repository<Administrator>,
  ) {}

  /**
   * Creates a new administrator in the system.
   * @param createAdministratorDto - The data transfer object containing administrator details
   * @returns Promise resolving to the created administrator entity
   */
  async create(createAdministratorDto: CreateAdministratorDto): Promise<Administrator> {
    const administrator = this.administratorsRepository.create(createAdministratorDto);
    return this.administratorsRepository.save(administrator);
  }

  /**
   * Retrieves all administrators from the system.
   * @returns Promise resolving to an array of all administrator entities
   */
  async findAll(): Promise<Administrator[]> {
    return this.administratorsRepository.find();
  }

  /**
   * Retrieves a specific administrator by their ID.
   * @param id - The unique identifier of the administrator
   * @returns Promise resolving to the found administrator entity
   * @throws NotFoundException if the administrator doesn't exist
   */
  async findOne(id: number): Promise<Administrator | null> {
    const administrator = await this.administratorsRepository.findOne({ where: { id } });

    if (!administrator) {
      throw new NotFoundException(`Administrator with id ${id} not found`);
    }

    return administrator;
  }

  /**
   * Updates an existing administrator's information.
   * @param id - The unique identifier of the administrator to update
   * @param updateAdministratorDto - The data transfer object containing updated administrator details
   * @returns Promise resolving to the updated administrator entity
   * @throws NotFoundException if the administrator doesn't exist
   */
  async update(id: number, updateAdministratorDto: UpdateAdministratorDto): Promise<Administrator> {
    const administrator = await this.administratorsRepository.preload({
      id,
      ...updateAdministratorDto,
    });

    if (!administrator) {
      throw new NotFoundException(`Administrator with id ${id} not found`);
    }

    return this.administratorsRepository.save(administrator);
  }

  /**
   * Removes an administrator from the system.
   * @param id - The unique identifier of the administrator to remove
   * @throws NotFoundException if the administrator doesn't exist
   * @returns Promise resolving when the administrator is deleted
   */
  async remove(id: number): Promise<void> {
    const result = await this.administratorsRepository.delete(id);

    if (result.affected === 0) {
      throw new NotFoundException(`Administrator with id ${id} not found`);
    }
  }

}
