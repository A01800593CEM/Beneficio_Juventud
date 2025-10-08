import { 
  Entity, 
  PrimaryGeneratedColumn, 
  Column,  
  JoinTable, 
  CreateDateColumn, 
  UpdateDateColumn, 
  ManyToMany,
} from 'typeorm';
import { Category } from '../../categories/entities/category.entity';
import { CollaboratorState } from '../enums/collaborator-state.enum';

@Entity({ name: 'colaborador' })
export class Collaborator {
    @PrimaryGeneratedColumn({ name: 'colaborador_id' })
    id: number;

    @Column({ name: 'nombre_negocio', length: 255 })
    businessName: string;

    @Column({ length: 20 })
    rfc: string;

    @Column({ name: 'representante_nombre', length: 255 })
    representativeName: string;

    @Column({ name: 'telefono', length: 20 })
    phone: string;

    @Column({ name: 'correo', length: 255 })
    email: string;

    @Column({ length: 255 })
    address: string;

    @Column({ name: 'codigo_postal', length: 10 })
    postalCode: string;

    @Column({ name: 'logo_url', length: 255, nullable: true })
    logoUrl: string;

    @Column({ type: 'text', nullable: true })
    description: string;

    @CreateDateColumn({ name: 'fecha_registro' })
    registrationDate: Date;

    @UpdateDateColumn({ name: 'updated_at' })
    updatedAt: Date;

    @Column({ type: 'enum', enum: CollaboratorState })
    state: CollaboratorState;

    @ManyToMany(() => Category, category => category.collaborators)
    @JoinTable({
      name: 'colaborador_categoria',
    joinColumn: {
      name: 'colaborador_id',
      referencedColumnName: 'id',
    },
    inverseJoinColumn: {
      name: 'categoria_id',
      referencedColumnName: 'id'
    },
    })
      categories: Category[]

}
