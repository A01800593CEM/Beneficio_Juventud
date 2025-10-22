import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  ManyToOne,
  ManyToMany,
  JoinColumn,
  OneToMany
} from 'typeorm';
import type { Relation } from 'typeorm';
import { Collaborator } from '../../collaborators/entities/collaborator.entity';
import { BranchState } from '../../branch/enums/branch-state.enum';
import { Redeemedcoupon } from '../../redeemedcoupon/entities/redeemedcoupon.entity';
import { Promotion } from '../../promotions/entities/promotion.entity';

@Entity({name: 'sucursal'})
export class Branch {
    @PrimaryGeneratedColumn({ name: 'sucursal_id' })
  branchId: number;

  @Column({name: 'colaborador_id'})
  collaboratorId: string;

  @Column({ name: 'nombre' })
  name: string;

  @Column({ name: 'telefono' })
  phone: string;

  @Column({ name: 'direccion' })
  address: string;

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


  //Relations

    @ManyToOne(() => Collaborator, collaborator => collaborator.branch)
    @JoinColumn({ name: 'colaborador_id', referencedColumnName: 'cognitoId' })
    collaborators: Relation<Collaborator>;

    @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.branch)
    redeemedcoupon: Relation<Redeemedcoupon>

    @ManyToMany(() => Promotion, promotion => promotion.branches)
    promotions: Relation<Promotion[]>;
}

