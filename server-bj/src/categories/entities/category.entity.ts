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


@Entity({name: 'categoria'})
export class Category {
    @PrimaryGeneratedColumn({name: 'categoria_id'})
    id: number;

    @Column({name: 'nombre'})
    name: string;

    @ManyToMany(() => Collaborator, collaborator => collaborator.categories)
    collaborators: Relation<Collaborator[]>

    @ManyToMany(() => Promotion, promotions => promotions.categories)
    promotions: Relation<Promotion[]>

    @ManyToMany(() => User, user => user.favorites)
    users: Relation<User[]>
    
}