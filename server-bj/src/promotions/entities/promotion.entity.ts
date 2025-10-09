import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn, OneToMany} from 'typeorm';
import { PromotionType } from '../enums/promotion-type.enums';
import { PromotionState } from '../enums/promotion-state.enums';
import { Booking } from '../../bookings/entities/booking.entity';
import type { Relation } from 'typeorm';

@Entity({ name: 'promocion' })
export class Promotion {
  @PrimaryGeneratedColumn({ name: 'promocion_id' })
  promotionId: number;
  @Column({ name: 'titulo' })
  title: string;

  @Column({ name: 'descripcion'})
  description: string;

  @Column({ name: 'imagen_url'})
  imageUrl: string;

  @Column({ name: 'fecha_inicio'})
  initialDate: Date;

  @Column({ name: 'fecha_fin'})
  endDate: Date;

  @Column({
    type: 'enum',
    enum: PromotionType,
    name: 'tipo_promocion',
  })
  promotionType: PromotionType;

  @Column({ name: 'promocion_string'})
  promotionString: string;

  @Column({ name: 'stock_total'})
  totalStock: number;

  @Column({ name: 'stock_disponible'})
  aviableStock: number;

  @Column({ name: 'limite_por_usuario'})
  limitPerUserx: number;

  @Column({ name: 'limite_diario_por_usuario'})
  dairyLimitPerUser: number;

  @Column({
    type: 'enum',
    enum: PromotionState,
    name: 'estado',
  })
  promotionState: PromotionState;

  @CreateDateColumn({ name: 'created_at' })
  created_at: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updated_at: Date;
  
  //Relations
  @OneToMany(() => Booking, bookings => bookings.user)
      bookings: Relation<Booking>;
}