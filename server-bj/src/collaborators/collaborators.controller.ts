import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { CollaboratorsService } from './collaborators.service';
import { CreateCollaboratorDto } from './dto/create-collaborator.dto';
import { UpdateCollaboratorDto } from './dto/update-collaborator.dto';
import { CategoriesByNamePipe } from 'src/common/pipes/transform-to-id.pipe';
import { Category } from 'src/categories/entities/category.entity';

/**
 * Controller responsible for handling collaborator-related HTTP requests.
 * Provides endpoints for managing collaborators in the system.
 * @route /collaborators
 */
@Controller('collaborators')
export class CollaboratorsController {
  /**
   * Creates an instance of CollaboratorsController.
   * @param collaboratorsService - The service handling collaborator business logic
   */
  constructor(private readonly collaboratorsService: CollaboratorsService) {}

  /**
   * Creates a new collaborator.
   * @route POST /collaborators
   * @param createCollaboratorDto - The data transfer object containing collaborator details
   * @returns The newly created collaborator entity
   * @example
   * // Request body
   * {
   *   "businessName": "Tech Store",
   *   "rfc": "TECH123456ABC",
   *   "representativeName": "John Doe",
   *   "phone": "1234567890",
   *   "email": "contact@techstore.com",
   *   "address": "123 Tech Street",
   *   "postalCode": "12345",
   *   "categoryIds": [1, 2],
   *   "state": "activo"
   * }
   */
  @Post()
  create(
    @Body('categories', CategoriesByNamePipe) categories: Category[],
    @Body() createCollaboratorDto: CreateCollaboratorDto) {
    return this.collaboratorsService.create({
      ...createCollaboratorDto,
      categoryIds: categories.map(category => category.id),
    });
  }

  /**
   * Retrieves all collaborators.
   * @route GET /collaborators
   * @returns An array of all collaborator entities
   */
  @Get()
  findAll() {
    return this.collaboratorsService.findAll();
  }

  /**
   * Retrieves a specific collaborator by ID.
   * @route GET /collaborators/:id
   * @param id - The unique identifier of the collaborator
   * @returns The collaborator entity with the specified ID
   */
  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.collaboratorsService.findOne(id);
  }

  /**
   * Retrieves collaborators by category name.
   * @route GET /collaborators/category/:categoryName
   * @param categoryName - The name of the category to filter by
   * @returns An array of collaborator entities in the specified category
   */
  @Get('category/:categoryName')
  findByCategory(@Param('categoryName') categoryName: string) {
    return this.collaboratorsService.findByCategory(categoryName)
  }

  /**
   * Updates an existing collaborator.
   * @route PATCH /collaborators/:id
   * @param id - The unique identifier of the collaborator to update
   * @param updateCollaboratorDto - The data transfer object containing updated collaborator details
   * @returns The updated collaborator entity
   * @example
   * // Request body
   * {
   *   "businessName": "Updated Tech Store",
   *   "phone": "9876543210",
   *   "state": "inactivo"
   * }
   */
  @Patch(':id')
  update(@Param('id') id: string, @Body() updateCollaboratorDto: UpdateCollaboratorDto) {
    return this.collaboratorsService.update(id, updateCollaboratorDto);
  }

  /**
   * Removes a collaborator.
   * @route DELETE /collaborators/:id
   * @param id - The unique identifier of the collaborator to remove
   * @returns void on successful deletion
   */
  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.collaboratorsService.remove(id);
  }

}
