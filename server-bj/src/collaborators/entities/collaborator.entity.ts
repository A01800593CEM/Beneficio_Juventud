import { 
  Entity, 
  PrimaryGeneratedColumn, 
  Column,  
  JoinTable, 
  CreateDateColumn, 
  UpdateDateColumn, 
  ManyToMany,
  OneToMany
} from 'typeorm';
import type { Relation } from 'typeorm'
import { Category } from '../../categories/entities/category.entity';
import { CollaboratorState } from '../enums/collaborator-state.enum';
import { Favorite } from 'src/favorites/entities/favorite.entity';
import { Redeemedcoupon } from 'src/redeemedcoupon/entities/redeemedcoupon.entity';
import { Branch } from 'src/branch/entities/branch.entity';

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

    @Column({name: 'direccion', length: 255 })
    address: string;

    @Column({ name: 'codigo_postal', length: 10 })
    postalCode: string;

    @Column({ name: 'logo_url', length: 255, nullable: true })
    logoUrl: string;

    @Column({ name: 'descripcion', type: 'text', nullable: true })
    description: string;

    @CreateDateColumn({ name: 'fecha_registro' })
    registrationDate: Date;

    @UpdateDateColumn({ name: 'updated_at' })
    updatedAt: Date;

    @Column({name: 'estado',  type: 'enum', enum: CollaboratorState })
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
      categories: Category[];
    
    //Relations
    @OneToMany(() => Favorite, favorite => favorite.user)
    favorites: Relation<Favorite[]>;

    @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.collaborator)
    redeemedcoupon: Relation<Redeemedcoupon>

    @OneToMany(() => Branch, branches => branches.collaborators)
    branch: Relation<Branch>
    

}
