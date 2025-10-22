import { Injectable, NotFoundException, Logger } from '@nestjs/common';
import { CreateBranchDto } from './dto/create-branch.dto';
import { UpdateBranchDto } from './dto/update-branch.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Branch } from './entities/branch.entity';
import { Repository } from 'typeorm';
import { GeocodingService } from 'src/common/geocoding.service';

/**
 * Service responsible for managing branch-related operations.
 * Provides methods for CRUD operations and business logic related to branches.
 * Supports automatic geocoding of addresses to coordinates.
 */
@Injectable()
export class BranchService {
  private readonly logger = new Logger(BranchService.name);

  /**
   * Creates an instance of BranchService.
   * @param branchRepository - The TypeORM repository for Branch entities
   * @param geocodingService - The service for geocoding addresses to coordinates
   */
  constructor(
    @InjectRepository(Branch)
    private branchRepository: Repository<Branch>,
    private geocodingService: GeocodingService,
  ) {}

  /**
   * Creates a new branch in the system.
   * @param createBranchDto - The data transfer object containing branch details
   * @returns Promise resolving to the created branch entity
   */
  async create(createBranchDto: CreateBranchDto): Promise<Branch> {
    const branch = this.branchRepository.create(createBranchDto)
    return this.branchRepository.save(branch);
  }

  /**
   * Retrieves all branches from the system.
   * Includes related collaborator data.
   * @returns Promise resolving to an array of all branch entities
   */
  async findAll(): Promise<Branch[]> {
    return this.branchRepository.find({relations: ['collaborators']});
  }

  /**
   * Retrieves a specific branch by its ID.
   * Includes related collaborator data.
   * @param id - The unique identifier of the branch
   * @returns Promise resolving to the found branch or null if not found
   */
  async findOne(id: number): Promise<Branch | null> {
    return this.branchRepository.findOne({
      where: {branchId: id},
      relations: ['collaborators'],
    });
  }

  /**
   * Updates an existing branch with new data.
   * @param id - The unique identifier of the branch to update
   * @param updateBranchDto - The data transfer object containing updated branch details
   * @returns Promise resolving to the updated branch
   * @throws NotFoundException if the branch doesn't exist
   */
  async update(id: number, updateBranchDto: UpdateBranchDto): Promise<Branch | null> {
    const branch = await this.branchRepository.preload({
      branchId: id,
      ...updateBranchDto
    })
    if (!branch) {
      throw new NotFoundException(`Branch with id ${id} not found`)
    }
    return this.branchRepository.save(branch);
  }

  /**
   * Retrieves all branches belonging to a specific collaborator.
   * @param collaboratorId - The cognito ID of the collaborator
   * @returns Promise resolving to an array of branch entities
   */
  async findByCollaborator(collaboratorId: string): Promise<Branch[]> {
    return this.branchRepository.find({
      where: { collaboratorId },
      order: { name: 'ASC' }
    });
  }

  /**
   * Removes a branch from the system.
   * @param id - The unique identifier of the branch to remove
   * @returns Promise resolving when the branch is deleted
   */
  async remove(id: number): Promise<void> {
    await this.branchRepository.delete(id);
  }

  /**
   * Geocodes an address and updates the branch location with the coordinates
   * @param branchId - The ID of the branch to update
   * @param address - The address to geocode
   * @param country - Optional country code for geocoding (e.g., "MX" for Mexico)
   * @returns Promise resolving to the updated branch
   * @throws NotFoundException if the branch doesn't exist
   * @throws Error if geocoding fails
   */
  async geocodeAndUpdateLocation(
    branchId: number,
    address: string,
    country?: string,
  ): Promise<Branch> {
    const branch = await this.branchRepository.findOne({
      where: { branchId },
    });

    if (!branch) {
      throw new NotFoundException(`Branch with id ${branchId} not found`);
    }

    try {
      this.logger.debug(`Geocoding address for branch ${branchId}: "${address}"`);

      const components = country ? `country:${country}` : undefined;
      const geocodingResult = await this.geocodingService.geocodeAddress(
        address,
        components,
      );

      // Format coordinates as "(longitude,latitude)" for PostgreSQL Point type
      const locationString = this.geocodingService.formatCoordinatesForDatabase(
        geocodingResult.coordinates,
      );

      // Update branch with new location
      branch.location = locationString;

      const updatedBranch = await this.branchRepository.save(branch);

      this.logger.debug(
        `Successfully geocoded and updated branch ${branchId} with location: ${locationString}`,
      );

      return updatedBranch;
    } catch (error) {
      this.logger.error(
        `Failed to geocode address for branch ${branchId}: ${error.message}`,
      );
      throw error;
    }
  }
}
