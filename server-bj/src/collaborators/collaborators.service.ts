import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateCollaboratorDto } from './dto/create-collaborator.dto';
import { UpdateCollaboratorDto } from './dto/update-collaborator.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Collaborator } from './entities/collaborator.entity';
import { Repository, In } from 'typeorm';
import { Category } from 'src/categories/entities/category.entity';
import { CollaboratorState } from './enums/collaborator-state.enum';
import { Branch } from 'src/branch/entities/branch.entity';
import {
  calculateDistance,
  parseLocationString,
  Coordinates,
} from 'src/common/location.utils';

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
    private categoriesRepository: Repository<Category>,

    @InjectRepository(Branch)
    private branchRepository: Repository<Branch>,
  ) {}
  
  /**
   * Creates a new collaborator with associated categories.
   * Categories are optional during registration and can be updated later.
   * @param createCollaboratorDto - The DTO containing collaborator information
   * @returns Promise<Collaborator> The newly created collaborator
   */
  async create(createCollaboratorDto: CreateCollaboratorDto): Promise<Collaborator> {
    const { categoryIds, ...data } = createCollaboratorDto;

    // Solo buscar categorías si se proporcionaron IDs
    let categories = [];
    if (categoryIds && categoryIds.length > 0) {
      categories = await this.categoriesRepository.findBy({
        id: In(categoryIds),
      });
    }

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

  /**
   * Encuentra colaboradores cercanos a la ubicación del usuario
   * @param latitude Latitud del usuario
   * @param longitude Longitud del usuario
   * @param radiusKm Radio de búsqueda en kilómetros (por defecto 3km)
   * @returns Lista de colaboradores con distancia a la sucursal más cercana
   */
  async findNearbyCollaborators(
    latitude: number,
    longitude: number,
    radiusKm: number = 3,
  ): Promise<any[]> {
    try {
      const userLocation: Coordinates = { latitude, longitude };

      console.log('========== NEARBY COLLABORATORS DEBUG ==========');
      console.log('User Location:', userLocation);
      console.log('Search Radius:', radiusKm, 'km');

    // Obtener todos los colaboradores activos con sus sucursales
    const collaborators = await this.collaboratorsRepository
      .createQueryBuilder('collaborator')
      .leftJoinAndSelect('collaborator.categories', 'category')
      .leftJoinAndSelect('collaborator.branch', 'branch')
      .where('collaborator.state = :state', {
        state: CollaboratorState.ACTIVE,
      })
      .getMany();

    console.log('Total active collaborators found:', collaborators.length);

    // Calcular distancias y filtrar por proximidad
    const collaboratorsWithDistance: any[] = [];

    for (const collaborator of collaborators) {
      console.log('\n--- Processing Collaborator:', collaborator.businessName);

      // Obtener todas las sucursales con ubicación
      const branches = collaborator.branch || [];
      console.log('Total branches:', branches.length);

      const branchesWithLocation = branches.filter(
        (b: any) => b.location !== null,
      );
      console.log('Branches with location:', branchesWithLocation.length);

      if (branchesWithLocation.length === 0) {
        console.log('⚠️  No branches with location, skipping...');
        continue;
      }

      // Encontrar la sucursal más cercana
      let closestDistance = Infinity;
      let closestBranch = null;

      for (const branch of branchesWithLocation) {
        console.log(`  Branch: "${branch.name}" - Location raw:`, branch.location);
        console.log(`  Location type: ${typeof branch.location}`);

        const branchCoords = parseLocationString(branch.location);
        if (!branchCoords) {
          console.log(`  ❌ Failed to parse location for branch "${branch.name}"`);
          continue;
        }

        console.log(`  ✓ Parsed coords: lat=${branchCoords.latitude}, lon=${branchCoords.longitude}`);
        const distance = calculateDistance(userLocation, branchCoords);
        console.log(`  Distance: ${distance} km`);

        if (distance < closestDistance) {
          closestDistance = distance;
          closestBranch = {
            branchId: branch.branchId,
            name: branch.name,
            address: branch.address,
            phone: branch.phone,
            zipCode: branch.zipCode,
            location: branch.location,
          };
        }
      }

      console.log(`Closest distance: ${closestDistance} km (limit: ${radiusKm} km)`);

      // Si la sucursal más cercana está dentro del radio, agregar el colaborador
      if (closestDistance <= radiusKm && closestBranch) {
        console.log(`✅ ADDED to results (within ${radiusKm}km)`);
        collaboratorsWithDistance.push({
          cognitoId: collaborator.cognitoId,
          businessName: collaborator.businessName,
          logoUrl: collaborator.logoUrl,
          description: collaborator.description,
          phone: collaborator.phone,
          email: collaborator.email,
          categories: collaborator.categories,
          distance: closestDistance,
          closestBranch,
          totalBranches: branchesWithLocation.length,
        });
      } else {
        console.log(`❌ EXCLUDED (distance ${closestDistance} > ${radiusKm}km)`);
      }
    }

    console.log('\n========== RESULTS ==========');
    console.log('Total collaborators within radius:', collaboratorsWithDistance.length);
    console.log('=====================================\n');

      // Ordenar por distancia (más cercano primero)
      collaboratorsWithDistance.sort((a, b) => a.distance - b.distance);

      return collaboratorsWithDistance;
    } catch (error) {
      console.error('❌ ERROR in findNearbyCollaborators:', error);
      console.error('Stack trace:', error.stack);
      throw error;
    }
  }

}
