import { Entity, 
  PrimaryGeneratedColumn, 
  CreateDateColumn, 
  ManyToOne, 
  JoinColumn, 
  Column} 
  from 'typeorm';
import type { Relation } from 'typeorm';
import { User } from '../../users/entities/user.entity';
import { Collaborator } from '../../collaborators/entities/collaborator.entity';
import { Branch } from '../../branch/entities/branch.entity';
import { Promotion } from '../../promotions/entities/promotion.entity';

@Entity({ name: 'uso_cupon' })
export class Redeemedcoupon {
  @PrimaryGeneratedColumn({ name: 'uso_id' })
  usedId: number;

  @CreateDateColumn({ name: 'fecha_uso' })
  usedDate: Date;

  @Column({name: 'usuario_id'})
  userId: string;

  @Column({name: 'sucursal_id'})
  branchId: number

  @Column({name: 'promocion_id'})
  promotionId: number

  @Column({name: 'nonce', nullable: true})
  nonce: string;

  @Column({name: 'qr_timestamp', type: 'bigint', nullable: true})
  qrTimestamp: number;

  //Relations
  @ManyToOne(() => User, users => users.redeemedcoupon)
  @JoinColumn({ name: 'usuario_id', referencedColumnName: 'id' })
  user: Relation<User>;

  @ManyToOne(() => Branch, branches => branches.redeemedcoupon)
  @JoinColumn({ name: 'sucursal_id' })
  branch: Relation<Branch>;

  @ManyToOne(() => Promotion, promotions => promotions.redeemedcoupon)
  @JoinColumn({ name: 'promocion_id' })
  promotion: Relation<Promotion>;
}
