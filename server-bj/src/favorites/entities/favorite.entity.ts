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

@Entity({ name: 'favorito' })
export class Favorite {
  @PrimaryColumn({ name: 'usuario_id', type: 'varchar' })
  userId: string;

  @PrimaryColumn({ name: 'colaborador_id', type: 'varchar' })
  collaboratorId: string;

  @ManyToOne(() => User, (user) => user.favorites, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'usuario_id', referencedColumnName: 'cognitoId' })
  user: Relation<User>;

  @ManyToOne(() => Collaborator, (collaborator) => collaborator.favorites, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'colaborador_id', referencedColumnName: 'cognitoId' })
  collaborator: Relation<Collaborator>;

  @CreateDateColumn({
    name: 'fecha_agregado',
    type: 'timestamp',
    default: () => 'CURRENT_TIMESTAMP',
  })
  addedAt: Date;
}
