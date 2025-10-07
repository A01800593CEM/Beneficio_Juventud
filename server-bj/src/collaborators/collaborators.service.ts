import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateCollaboratorDto } from './dto/create-collaborator.dto';
import { UpdateCollaboratorDto } from './dto/update-collaborator.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Collaborator } from './entities/collaborator.entity';
import { Repository } from 'typeorm';
import { Category } from 'src/categories/entities/category.entity';

@Injectable()
export class CollaboratorsService {
  constructor(
    @InjectRepository(Collaborator)
    private collaboratorsRepository: Repository<Collaborator>,

    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>
  ) {}
  
  async create(createCollaboratorDto: CreateCollaboratorDto) {
    const collaborator = this.collaboratorsRepository.create(createCollaboratorDto)
    return this.collaboratorsRepository.insert(collaborator);
  }

  async findAll() {
    return this.collaboratorsRepository.find({ 
      relations: ['categories'] });
  }

  async findOne(id: number) {
    return this.collaboratorsRepository.findOne({ 
      where: { id },
      relations: ['favoritedBy',
         'favoritedBy.user',
         'categories'] });
  }

  async update(id: number, updateCollaboratorDto: UpdateCollaboratorDto) {
    const collaborator = await this.collaboratorsRepository.preload({
      id,
      ...updateCollaboratorDto
    });

    if (!collaborator) {
      throw new Error(`Collaborator with id ${id} not found`);
    }
    return this.collaboratorsRepository.save(collaborator);
  }

  async remove(id: number) {
    await this.collaboratorsRepository.delete(id);
  }

  async addCategories(collaboratorId: number, categoryIds: number[]) {
    const collaborator = await this.collaboratorsRepository.findOne({
      where: { id: collaboratorId },
      relations: ['categories'],
    });

    if (!collaborator) {
      throw new NotFoundException('Collaborator not found');
    }

    const categories = await this.categoriesRepository.findByIds(categoryIds);

    collaborator.categories = [...collaborator.categories, ...categories];

    return this.collaboratorsRepository.save(collaborator);
  }

}
