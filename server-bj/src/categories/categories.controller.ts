import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { CategoriesService } from './categories.service';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';

/**
 * Controller responsible for handling category-related HTTP requests.
 * Provides endpoints for CRUD operations on categories.
 * @route /categories
 */
@Controller('categories')
export class CategoriesController {
  /**
   * Creates an instance of CategoriesController.
   * @param categoriesService - The service handling category business logic
   */
  constructor(private readonly categoriesService: CategoriesService) {}

  /**
   * Creates a new category.
   * @route POST /categories
   * @param createCategoryDto - The data transfer object containing category details
   * @returns The newly created category entity
   * @example
   * // Request body
   * {
   *   "name": "Electronics"
   * }
   */
  @Post()
  create(@Body() createCategoryDto: CreateCategoryDto) {
    return this.categoriesService.create(createCategoryDto);
  }

  /**
   * Retrieves all categories.
   * @route GET /categories
   * @returns An array of all category entities
   */
  @Get()
  findAll() {
    return this.categoriesService.findAll();
  }

  /**
   * Retrieves a specific category by ID.
   * @route GET /categories/:id
   * @param id - The unique identifier of the category
   * @returns The category entity with the specified ID
   */
  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.categoriesService.findOne(+id);
  }

  /**
   * Updates an existing category.
   * @route PATCH /categories/:id
   * @param id - The unique identifier of the category to update
   * @param updateCategoryDto - The data transfer object containing updated category details
   * @returns The updated category entity
   * @example
   * // Request body
   * {
   *   "name": "Updated Category Name"
   * }
   */
  @Patch(':id')
  update(@Param('id') id: string, @Body() updateCategoryDto: UpdateCategoryDto) {
    return this.categoriesService.update(+id, updateCategoryDto);
  }

  /**
   * Removes a category.
   * @route DELETE /categories/:id
   * @param id - The unique identifier of the category to remove
   * @returns void on successful deletion
   */
  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.categoriesService.remove(+id);
  }
}
