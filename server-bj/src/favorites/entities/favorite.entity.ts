import {
  Entity,
  ManyToOne,
  JoinColumn,
  PrimaryColumn,
  CreateDateColumn
} from 'typeorm';
import type { Relation } from 'typeorm';
import { User } from 'src/users/entities/user.entity';
import { Collaborator } from 'src/collaborators/entities/collaborator.entity';

/**
 * Entity representing a favorite relationship between a user and a collaborator.
 * This is a junction table that implements a many-to-many relationship with additional metadata.
 * 
 * @entity Favorite
 * @tableName favorito
 */
@Entity({ name: 'favorito' })
export class Favorite {
  @PrimaryColumn({ name: 'usuario_id', type: 'varchar' })
  userId: string;

  @PrimaryColumn({ name: 'colaborador_id', type: 'varchar' })
  collaboratorId: string;

  /**
   * Many-to-One relationship with User entity.
   * Represents the user who marked the collaborator as favorite.
   * Cascading delete ensures this record is removed if the user is deleted.
   * @relation many-to-one
   * @foreignKey usuario_id
   */
  @ManyToOne(() => User, (user) => user.favorites, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'usuario_id', referencedColumnName: 'cognitoId' })
  user: Relation<User>;

  /**
   * Many-to-One relationship with Collaborator entity.
   * Represents the collaborator marked as favorite.
   * Cascading delete ensures this record is removed if the collaborator is deleted.
   * @relation many-to-one
   * @foreignKey colaborador_id
   */
  @ManyToOne(() => Collaborator, (collaborator) => collaborator.favorites, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'colaborador_id', referencedColumnName: 'cognitoId' })
  collaborator: Relation<Collaborator>;

  /**
   * Timestamp indicating when the favorite relationship was created.
   * Automatically set to current timestamp when record is created.
   * @columnName fecha_agregado
   */
  @CreateDateColumn({
    name: 'fecha_agregado',
    type: 'timestamp',
    default: () => 'CURRENT_TIMESTAMP',
  })
  addedAt: Date;
}
