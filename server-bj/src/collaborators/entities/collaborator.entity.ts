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
import { Promotion } from 'src/promotions/entities/promotion.entity';

/**
 * Entity representing a collaborator in the system.
 * @entity Collaborator
 * @tableName colaborador
 */
@Entity({ name: 'colaborador' })
export class Collaborator {
    /**
     * Unique identifier for the collaborator.
     * @primaryKey
     * @columnName colaborador_id
     */
    @PrimaryGeneratedColumn({ name: 'colaborador_id' })
    id: number;

    /**
     * The unique identifier from Cognito for the collaborator.
     * Used for authentication and linking with Cognito user.
     * @maps cognito_id
     * @maxLength 50
     */
    @Column({name: 'cognito_id'})
    cognitoId: string;

    /**
     * Official name of the collaborator's business.
     * @columnName nombre_negocio
     * @maxLength 255
     */
    @Column({ name: 'nombre_negocio', length: 255 })
    businessName: string;

    /**
     * RFC (Tax ID) of the business.
     * @maxLength 20
     */
    @Column({ length: 20 })
    rfc: string;

    /**
     * Name of the legal representative.
     * @columnName representante_nombre
     * @maxLength 255
     */
    @Column({ name: 'representante_nombre', length: 255 })
    representativeName: string;

    /**
     * Contact phone number.
     * @columnName telefono
     * @maxLength 20
     */
    @Column({ name: 'telefono', length: 20 })
    phone: string;

    /**
     * Contact email address.
     * @columnName correo
     * @maxLength 255
     */
    @Column({ name: 'correo', length: 255 })
    email: string;

    /**
     * Physical address of the business.
     * @columnName direccion
     * @maxLength 255
     */
    @Column({name: 'direccion', length: 255 })
    address: string;

    /**
     * Postal code of the business location.
     * @columnName codigo_postal
     * @maxLength 10
     */
    @Column({ name: 'codigo_postal', length: 10 })
    postalCode: string;

    /**
     * URL to the business logo image.
     * @columnName logo_url
     * @maxLength 255
     * @optional
     */
    @Column({ name: 'logo_url', length: 255, nullable: true })
    logoUrl: string;

    /**
     * Detailed description of the business.
     * @columnName descripcion
     * @optional
     */
    @Column({ name: 'descripcion', type: 'text', nullable: true })
    description: string;

    /**
     * Date when the collaborator was registered.
     * @columnName fecha_registro
     */
    @CreateDateColumn({ name: 'fecha_registro' })
    registrationDate: Date;

    /**
     * Last update timestamp.
     * @columnName updated_at
     */
    @UpdateDateColumn({ name: 'updated_at' })
    updatedAt: Date;

    /**
     * Current state of the collaborator.
     * @columnName estado
     * @enum CollaboratorState
     */
    @Column({name: 'estado',  type: 'enum', enum: CollaboratorState })
    state: CollaboratorState;

    /**
     * Categories associated with this collaborator.
     * @many-to-many Categories[]
     * @joinTable colaborador_categoria
     */
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
    
    //RELATIONS
    /**
     * Users who have marked this collaborator as favorite.
     * @relation one-to-many
     */
    @OneToMany(() => Favorite, favorite => favorite.user)
    favorites: Relation<Favorite[]>;

    /**
     * Coupons redeemed by users for this collaborator.
     * @relation one-to-many
     */
    @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.collaborator)
    redeemedcoupon: Relation<Redeemedcoupon>;

    /**
     * Physical branches of this collaborator.
     * @relation one-to-many
     */
    @OneToMany(() => Branch, branches => branches.collaborators)
    branch: Relation<Branch>;

    @OneToMany(() => Promotion, promotions => promotions.collaborator)
    promotions: Relation<Promotion[]>;
    

}
