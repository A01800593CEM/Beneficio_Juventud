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
    const collaborator = this.collaboratorsRepository.create(createCollaboratorDto)
    return this.collaboratorsRepository.save(collaborator);
  }

  async findAll(): Promise<Collaborator[]> {
    return this.collaboratorsRepository.find({ 
      relations: ['categories'] });
  }
 
  // Only finds the active collaborators
  async findOne(id: number): Promise<Collaborator | null> {
    return this.collaboratorsRepository.findOne({ 
      where: { id,
               state: CollaboratorState.ACTIVE
       },
      relations: ['favorites',
         'favorites.user',
         'categories'] });
  }

  // Finds in all the database
  async trueFindOne(id: number): Promise<Collaborator | null> {
    return this.collaboratorsRepository.findOne({ 
      where: { id,
               state: CollaboratorState.ACTIVE
       },
      relations: ['favorites',
         'favorites.user',
         'categories'] });
  }

  async update(id: number, updateCollaboratorDto: UpdateCollaboratorDto): Promise<Collaborator | null> {
    const collaborator = await this.collaboratorsRepository.preload({
      id,
      ...updateCollaboratorDto
    });

    if (!collaborator) {
      throw new Error(`Collaborator with id ${id} not found`);
    }
    return this.collaboratorsRepository.save(collaborator);
  }

  async remove(id: number): Promise<void> {
    const collaborator = await this.findOne(id);
    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }
    await this.collaboratorsRepository.update(id, { state: CollaboratorState.INACTIVE})
  }

  async reActivate(id: number): Promise<Collaborator> {
    const collaborator = await this.trueFindOne(id);
    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }
    return collaborator
    
  }

  async addCategories(collaboratorId: number, categoryIds: number[]) {
    const collaborator = await this.collaboratorsRepository.findOne({
      where: { id: collaboratorId },
      relations: ['categories'],
    });

    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }

    const categories = await this.categoriesRepository.findBy({ id: In([...categoryIds]) }); //.findBy({ id: In([1, 2, 3]) })

    collaborator.categories = [...collaborator.categories, ...categories];

    return this.collaboratorsRepository.save(collaborator);
  }

}
