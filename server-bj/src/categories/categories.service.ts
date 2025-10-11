import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Category } from './entities/category.entity';
import { Repository } from 'typeorm';
import { Collaborator } from 'src/collaborators/entities/collaborator.entity';


@Injectable()
export class CategoriesService {
  constructor(
    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>
  ) {}

  async create(createCategoryDto: CreateCategoryDto) {
    const category = this.categoriesRepository.create(createCategoryDto)
    return this.categoriesRepository.save(category);
  }

  async findAll() {
    return this.categoriesRepository.find();
  }

  async findOne(id: number) {
    return this.categoriesRepository.findOne({ where: { id }});
  }

  async update(id: number, updateCategoryDto: UpdateCategoryDto) {
     const category = await this.categoriesRepository.preload({
          id,
          ...updateCategoryDto
        });
    
        if (!category) {
          throw new NotFoundException(`User with id ${id} not found`);
        }
        
        return this.categoriesRepository.save(category)
      }

  remove(id: number) {
    return this.categoriesRepository.delete(id);
  }

  async findByNames(names: string[]): Promise<Category[]> {
  return this.categoriesRepository
    .createQueryBuilder('category')
    .where('category.name IN (:...names)', { names })
    .getMany();
}

}
