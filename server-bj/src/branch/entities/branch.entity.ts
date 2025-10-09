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

@Entity({name: 'sucursal'})
export class Branch {
    @PrimaryGeneratedColumn({ name: 'sucursal_id' })
  branchId: number;

  @Column({name: 'colaborador_id'})
  collaboratorId:number

  @Column({ name: 'nombre' })
  name: string;

  @Column({ name: 'telefono' })
  phone: string;

  @Column({ name: 'direccion' })
  direction: string;

  @Column({ name: 'codigo_postal' })
  zipCode: string;

  @Column({ name: 'ubicacion', type: 'point', nullable: true })
  location: string | null;

  @Column({ name: 'horario_json', type: 'json', nullable: true })
  jsonSchedule: any | null;

  @Column({
    type: 'enum',
    enum: BranchState,
    enumName: 'estado_sucursal',
    name: 'estado',
  })
  state: BranchState;

  @CreateDateColumn({ name: 'created_at' })
  created_at: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updated_at: Date;

  
  //Reltaions

    @OneToMany(() => Collaborator, collaborators => collaborators.branch)
    collaborators: Relation<Collaborator>;

    @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.branch)
    redeemedcoupon: Relation<Redeemedcoupon>
}

