import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateCollaboratorDto } from './dto/create-collaborator.dto';
import { UpdateCollaboratorDto } from './dto/update-collaborator.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Collaborator } from './entities/collaborator.entity';
import { Repository, In } from 'typeorm';
import { Category } from 'src/categories/entities/category.entity';
import { CollaboratorState } from './enums/collaborator-state.enum';

/**
 * Service responsible for managing collaborator operations.
 * Handles business logic for collaborator creation, retrieval, updates, and management.
 */
@Injectable()
export class CollaboratorsService {
  /**
   * Creates an instance of CollaboratorsService.
   * @param collaboratorsRepository - The TypeORM repository for Collaborator entities
   * @param categoriesRepository - The TypeORM repository for Category entities
   */
  constructor(
    @InjectRepository(Collaborator)
    private collaboratorsRepository: Repository<Collaborator>,

    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>
  ) {}
  
  /**
   * Creates a new collaborator with associated categories.
   * @param createCollaboratorDto - The DTO containing collaborator information
   * @returns Promise<Collaborator> The newly created collaborator
   */
  async create(createCollaboratorDto: CreateCollaboratorDto): Promise<Collaborator> {
    const { categoryIds, ...data } = createCollaboratorDto;

    const categories = await this.categoriesRepository.findBy({
      id: In(categoryIds),
    });

    const collaborator = this.collaboratorsRepository.create({
      ...data,
      categories,
    });

    return this.collaboratorsRepository.save(collaborator);
  }

  /**
   * Retrieves all collaborators with their associated categories.
   * @returns Promise<Collaborator[]> Array of all collaborators
   */
  async findAll(): Promise<Collaborator[]> {
    return this.collaboratorsRepository.find({ 
      relations: ['categories'] });
  }
 
  // Only finds the active collaborators
  /**
   * Finds an active collaborator by ID.
   * @param id - The collaborator's unique identifier
   * @returns Promise<Collaborator> The found collaborator with favorites and categories
   * @throws NotFoundException if collaborator is not found
   */
  async findOne(cognitoId: string): Promise<Collaborator | null> {
    const collaborator = this.collaboratorsRepository.findOne({ 
      where: { cognitoId,
               state: CollaboratorState.ACTIVE
       }});
        
    if (!collaborator) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    return collaborator
  }

  // Finds in all the database
  /**
   * Finds a collaborator by ID regardless of state.
   * @param id - The collaborator's unique identifier
   * @returns Promise<Collaborator> The found collaborator with favorites and categories
   */
  async trueFindOne(cognitoId: string): Promise<Collaborator | null> {
    return this.collaboratorsRepository.findOne({ 
      where: { cognitoId,
               state: CollaboratorState.ACTIVE
       },
      relations: ['favorites',
         'favorites.user',
         'categories'] });
  }

  /**
   * Updates a collaborator's information.
   * @param id - The collaborator's unique identifier
   * @param updateCollaboratorDto - The DTO containing updated information
   * @returns Promise<Collaborator> The updated collaborator
   * @throws NotFoundException if collaborator is not found
   */
  async update(cognitoId: string, updateCollaboratorDto: UpdateCollaboratorDto): Promise<Collaborator> {
    const collaborator = await this.collaboratorsRepository.findOne({
      where: { cognitoId },
      relations: ['categories'],
    });

    if (!collaborator) {
      throw new NotFoundException(`Collaborator with ID ${cognitoId} not found`);
    }

    const { categoryIds, ...updateData } = updateCollaboratorDto;

    Object.assign(collaborator, updateData);

    if (categoryIds && categoryIds.length > 0) {
      const categories = await this.categoriesRepository.findBy({ id: In(categoryIds) });
      collaborator.categories = categories;
    }

    return this.collaboratorsRepository.save(collaborator);
  }

  /**
   * Soft deletes a collaborator by setting their state to INACTIVE.
   * @param id - The collaborator's unique identifier
   * @throws NotFoundException if collaborator is not found
   */
  async remove(cognitoId: string): Promise<void> {
    const collaborator = await this.findOne(cognitoId);
    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }
    await this.collaboratorsRepository.update(cognitoId, { state: CollaboratorState.INACTIVE})
  }

  /**
   * Retrieves an inactive collaborator for potential reactivation.
   * @param id - The collaborator's unique identifier
   * @returns Promise<Collaborator> The found collaborator
   * @throws NotFoundException if collaborator is not found
   */
  async reActivate(cognitoId: string): Promise<Collaborator> {
    const collaborator = await this.trueFindOne(cognitoId);
    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }
    return collaborator
  }

  /**
   * Adds additional categories to an existing collaborator.
   * @param collaboratorId - The collaborator's unique identifier
   * @param categoryIds - Array of category IDs to add
   * @returns Promise<Collaborator> The updated collaborator with new categories
   * @throws NotFoundException if collaborator is not found
   */
  async addCategories(collaboratorId: string, categoryIds: number[]) {
    const collaborator = await this.collaboratorsRepository.findOne({
      where: { cognitoId: collaboratorId },
      relations: ['categories'],
    });

    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }

    const categories = await this.categoriesRepository.findBy({ id: In([...categoryIds]) });

    collaborator.categories = [...collaborator.categories, ...categories];

    return this.collaboratorsRepository.save(collaborator);
  }

  /**
   * Finds all collaborators belonging to a specific category.
   * @param categoryName - The name of the category to filter by
   * @returns Promise<Collaborator[]> Array of collaborators in the specified category
   */
  async findByCategory(categoryName: string): Promise<Collaborator[]> {
    return this.collaboratorsRepository
      .createQueryBuilder('collaborator')
      .innerJoin('collaborator.categories', 'category')
      .where('category.name = :categoryName', { categoryName })
      .getMany();
}

}
