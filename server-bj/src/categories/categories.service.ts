import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Category } from './entities/category.entity';
import { Repository } from 'typeorm';
import { Collaborator } from 'src/collaborators/entities/collaborator.entity';

/**
 * Service responsible for managing category operations in the application.
 * Handles all business logic related to category creation, retrieval, update, and deletion.
 */
@Injectable()
export class CategoriesService {
  /**
   * Creates an instance of CategoriesService.
   * @param categoriesRepository - The TypeORM repository for Category entities
   */
  constructor(
    @InjectRepository(Category)
    private categoriesRepository: Repository<Category>
  ) {}

  /**
   * Creates a new category in the database.
   * @param createCategoryDto - Data transfer object containing the category details
   * @returns Promise<Category> - The newly created category entity
   */
  async create(createCategoryDto: CreateCategoryDto) {
    const category = this.categoriesRepository.create(createCategoryDto)
    return this.categoriesRepository.save(category);
  }

  /**
   * Retrieves all categories from the database.
   * @returns Promise<Category[]> - An array of all category entities
   */
  async findAll() {
    return this.categoriesRepository.find();
  }

  /**
   * Retrieves a specific category by its ID.
   * @param id - The unique identifier of the category
   * @returns Promise<Category> - The category entity if found
   */
  async findOne(id: number) {
    return this.categoriesRepository.findOne({ where: { id }});
  }

  /**
   * Updates an existing category in the database.
   * @param id - The unique identifier of the category to update
   * @param updateCategoryDto - Data transfer object containing the updated category details
   * @returns Promise<Category> - The updated category entity
   * @throws NotFoundException - When the category with the given ID is not found
   */
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

  /**
   * Removes a category from the database.
   * @param id - The unique identifier of the category to remove
   * @returns Promise<DeleteResult> - The result of the delete operation
   */
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
