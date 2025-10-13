import {
  Entity, 
  PrimaryGeneratedColumn, 
  Column,
  CreateDateColumn, 
  UpdateDateColumn, 
  ManyToOne, 
  JoinColumn,
  OneToMany
} from 'typeorm';
import type { Relation } from 'typeorm';
import { Collaborator } from '../../collaborators/entities/collaborator.entity';
import { BranchState } from '../../branch/enums/branch-state.enum';
import { Redeemedcoupon } from '../../redeemedcoupon/entities/redeemedcoupon.entity';

/**
 * Entity representing a branch/location in the system.
 * Maps to the 'sucursal' table in the database.
 */
@Entity({name: 'sucursal'})
export class Branch {
    /**
     * Unique identifier for the branch.
     * @maps sucursal_id
     */
    @PrimaryGeneratedColumn({ name: 'sucursal_id' })
    branchId: number;

  @Column({name: 'colaborador_id'})
  collaboratorId: string;

    /**
     * Name of the branch.
     * @maps nombre
     */
    @Column({ name: 'nombre' })
    name: string;

    /**
     * Contact phone number for the branch.
     * @maps telefono
     */
    @Column({ name: 'telefono' })
    phone: string;

    /**
     * Physical address of the branch.
     * @maps direccion
     */
    @Column({ name: 'direccion' })
    address: string;

    /**
     * Postal/ZIP code of the branch location.
     * @maps codigo_postal
     */
    @Column({ name: 'codigo_postal' })
    zipCode: string;

    /**
     * Geographical coordinates of the branch.
     * Stored as a PostGIS point type.
     * @maps ubicacion
     */
    @Column({ name: 'ubicacion', type: 'point', nullable: true })
    location: string | null;

    /**
     * Operating schedule of the branch stored as JSON.
     * Contains opening hours and other schedule-related information.
     * @maps horario_json
     */
    @Column({ name: 'horario_json', type: 'json', nullable: true })
    jsonSchedule: any | null;

    /**
     * Current state/status of the branch.
     * Uses BranchState enum for possible values.
     * @maps estado
     */
    @Column({
      type: 'enum',
      enum: BranchState,
      enumName: 'estado_sucursal',
      name: 'estado',
    })
    state: BranchState;

    /**
     * Timestamp of when the branch record was created.
     * Automatically set by TypeORM.
     * @maps created_at
     */
    @CreateDateColumn({ name: 'created_at' })
    created_at: Date;

    /**
     * Timestamp of the last update to the branch record.
     * Automatically updated by TypeORM.
     * @maps updated_at
     */
    @UpdateDateColumn({ name: 'updated_at' })
    updated_at: Date;

    // Relations

    /**
     * Many-to-One relationship with Collaborator entity.
     * Each branch is managed by one collaborator.
     */
    @ManyToOne(() => Collaborator, collaborator => collaborator.branch)
    @JoinColumn({ name: 'colaborador_id', referencedColumnName: 'cognitoId' })
    collaborators: Relation<Collaborator>;

    /**
     * One-to-Many relationship with Redeemedcoupon entity.
     * A branch can have multiple redeemed coupons.
     */
    @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.branch)
    redeemedcoupon: Relation<Redeemedcoupon>
}

