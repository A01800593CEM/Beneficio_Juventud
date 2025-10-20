import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { BranchService } from './branch.service';
import { CreateBranchDto } from './dto/create-branch.dto';
import { UpdateBranchDto } from './dto/update-branch.dto';

/**
 * Controller responsible for handling branch-related HTTP requests.
 * Provides endpoints for CRUD operations on branch entities.
 * @route /branch
 */
@Controller('branch')
export class BranchController {
  /**
   * Creates an instance of BranchController.
   * @param branchService - The service handling branch business logic
   */
  constructor(private readonly branchService: BranchService) {}

  /**
   * Creates a new branch.
   * @route POST /branch
   * @param createBranchDto - The data transfer object containing branch details
   * @returns The newly created branch entity
   */
  @Post()
  create(@Body() createBranchDto: CreateBranchDto) {
    return this.branchService.create(createBranchDto);
  }

  /**
   * Retrieves all branches.
   * @route GET /branch
   * @returns An array of all branch entities
   */
  @Get()
  findAll() {
    return this.branchService.findAll();
  }

  /**
   * Retrieves all branches for a specific collaborator.
   * @route GET /branch/collaborator/:collaboratorId
   * @param collaboratorId - The cognito ID of the collaborator
   * @returns An array of branch entities belonging to the collaborator
   */
  @Get('collaborator/:collaboratorId')
  findByCollaborator(@Param('collaboratorId') collaboratorId: string) {
    return this.branchService.findByCollaborator(collaboratorId);
  }

  /**
   * Retrieves a specific branch by ID.
   * @route GET /branch/:id
   * @param id - The unique identifier of the branch
   * @returns The branch entity with the specified ID
   */
  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.branchService.findOne(+id);
  }

  /**
   * Updates an existing branch.
   * @route PATCH /branch/:id
   * @param id - The unique identifier of the branch to update
   * @param updateBranchDto - The data transfer object containing updated branch details
   * @returns The updated branch entity
   */
  @Patch(':id')
  update(@Param('id') id: string, @Body() updateBranchDto: UpdateBranchDto) {
    return this.branchService.update(+id, updateBranchDto);
  }

  /**
   * Removes a branch.
   * @route DELETE /branch/:id
   * @param id - The unique identifier of the branch to remove
   * @returns void on successful deletion
   */
  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.branchService.remove(+id);
  }
}
