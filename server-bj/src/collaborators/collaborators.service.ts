import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateCollaboratorDto } from './dto/create-collaborator.dto';
import { UpdateCollaboratorDto } from './dto/update-collaborator.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Collaborator } from './entities/collaborator.entity';
import { Repository, In } from 'typeorm';
import { Category } from 'src/categories/entities/category.entity';
import { CollaboratorState } from './enums/collaborator-state.enum';

@Injectable()
export class CollaboratorsService {
  constructor(
    @InjectRepository(Collaborator)
    private collaboratorsRepository: Repository<Collaborator>,

    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>
  ) {}
  
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


  async findAll(): Promise<Collaborator[]> {
    return this.collaboratorsRepository.find({ 
      relations: ['categories'] });
  }
 
  // Only finds the active collaborators
  async findOne(cognitoId: string): Promise<Collaborator | null> {
    const collaborator = this.collaboratorsRepository.findOne({ 
      where: { cognitoId,
               state: CollaboratorState.ACTIVE
       },
      relations: ['favorites',
         'favorites.user',
         'categories'] });
        
    if (!collaborator) {
      throw new NotFoundException(`User with id ${cognitoId} not found`);
    }
    return collaborator
  }

  // Finds in all the database
  async trueFindOne(cognitoId: string): Promise<Collaborator | null> {
    return this.collaboratorsRepository.findOne({ 
      where: { cognitoId,
               state: CollaboratorState.ACTIVE
       },
      relations: ['favorites',
         'favorites.user',
         'categories'] });
  }

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

  async remove(cognitoId: string): Promise<void> {
    const collaborator = await this.findOne(cognitoId);
    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }
    await this.collaboratorsRepository.update(cognitoId, { state: CollaboratorState.INACTIVE})
  }

  async reActivate(cognitoId: string): Promise<Collaborator> {
    const collaborator = await this.trueFindOne(cognitoId);
    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }
    return collaborator
    
  }

  async addCategories(collaboratorId: string, categoryIds: number[]) {
    const collaborator = await this.collaboratorsRepository.findOne({
      where: { cognitoId: collaboratorId },
      relations: ['categories'],
    });

    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }

    const categories = await this.categoriesRepository.findBy({ id: In([...categoryIds]) }); //.findBy({ id: In([1, 2, 3]) })

    collaborator.categories = [...collaborator.categories, ...categories];

    return this.collaboratorsRepository.save(collaborator);
  }
  async findByCategory(categoryName: string): Promise<Collaborator[]> {
  return this.collaboratorsRepository
    .createQueryBuilder('collaborator')
    .innerJoin('collaborator.categories', 'category')
    .where('category.name = :categoryName', { categoryName })
    .getMany();
}

 




}
