import { Collaborator } from 'src/collaborators/entities/collaborator.entity';
import { Promotion } from 'src/promotions/entities/promotion.entity';
import { User } from 'src/users/entities/user.entity';
import type { Relation } from 'typeorm';
import { 
  Entity, 
  PrimaryGeneratedColumn, 
  Column,
  ManyToMany
} from 'typeorm';

/**
 * Entity representing a category in the system.
 * Categories are used to classify promotions and organize collaborators.
 * Maps to the 'categoria' table in the database.
 */
@Entity({name: 'categoria'})
export class Category {
    /**
     * The unique identifier for the category.
     * Auto-generated primary key.
     * @maps categoria_id
     */
    @PrimaryGeneratedColumn({name: 'categoria_id'})
    id: number;

    /**
     * The name of the category.
     * @maps nombre
     * @example "Electronics", "Food", "Entertainment"
     */
    @Column({name: 'nombre'})
    name: string;

    /**
     * Many-to-Many relationship with Collaborator entity.
     * A category can be associated with multiple collaborators,
     * and a collaborator can belong to multiple categories.
     * Bidirectional relationship with Collaborator.categories.
     */
    @ManyToMany(() => Collaborator, collaborator => collaborator.categories)
    collaborators: Relation<Collaborator[]>

    /**
     * Many-to-Many relationship with Promotion entity.
     * A category can contain multiple promotions,
     * and a promotion can belong to multiple categories.
     * Bidirectional relationship with Promotion.categories.
     */
    @ManyToMany(() => Promotion, promotions => promotions.categories)
    promotions: Relation<Promotion[]>

    @ManyToMany(() => User, user => user.favorites)
    users: Relation<User[]>
    
}