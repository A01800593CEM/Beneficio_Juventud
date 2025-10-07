import { Collaborator } from 'src/collaborators/entities/collaborator.entity';
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
    collaborators: Collaborator[]
    
}