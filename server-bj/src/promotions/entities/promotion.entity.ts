import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn, OneToMany, ManyToMany, JoinTable, ManyToOne, JoinColumn, } from 'typeorm';
import { PromotionType } from '../enums/promotion-type.enums';
import { PromotionState } from '../enums/promotion-state.enums';
import { Booking } from '../../bookings/entities/booking.entity';
import type { Relation } from 'typeorm';
import { Redeemedcoupon } from 'src/redeemedcoupon/entities/redeemedcoupon.entity';
import { Category } from 'src/categories/entities/category.entity';
import { Collaborator } from 'src/collaborators/entities/collaborator.entity';
import { PromotionTheme } from '../enums/promotion-theme.enum';
import { User } from 'src/users/entities/user.entity';
import { Notification } from 'src/notifications/entities/notification.entity';

@Entity({ name: 'promocion' })
export class Promotion {
  @PrimaryGeneratedColumn({ name: 'promocion_id' })
  promotionId: number;

  @Column({name: 'colaborador_id'})
  collaboratorId: string

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
    enumName: 'tipo_promocion',
    name: 'tipo_promocion',
  })
  promotionType: PromotionType;

  @Column({ name: 'promocion_string'})
  promotionString: string;

  @Column({ name: 'stock_total'})
  totalStock: number;

  @Column({ name: 'stock_disponible'})
  availableStock: number;

  @Column({ name: 'limite_por_usuario'})
  limitPerUser: number;

  @Column({ name: 'limite_diario_por_usuario'})
  dailyLimitPerUser: number;

  @Column({
    type: 'enum',
    enum: PromotionState,
    enumName: 'estado_promocion',
    name: 'estado',
  })
  promotionState: PromotionState;

  @CreateDateColumn({ name: 'created_at' })
  created_at: Date;

  @UpdateDateColumn({ name: 'updated_at' })
  updated_at: Date;

  @Column({
    type: 'enum',
    enum: PromotionTheme,
    enumName: 'tema',
    name: 'theme',
  })

  @Column({name: 'es_reservable'})
  is_bookable: boolean;
  
  //Relations
  @OneToMany(() => Booking, bookings => bookings.promotion)
    bookings: Relation<Booking>;

  @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.promotion)
    redeemedcoupon: Relation<Redeemedcoupon>

  @ManyToMany(() => User, user => user.favoritePromos)
    favoritedBy: Relation<User[]>;

  @ManyToMany(() => Category, category => category.promotions)
  @JoinTable({
    name: 'promocion_categoria',
    joinColumn: {
    name: 'promocion_id',
    referencedColumnName: 'promotionId',
  },
    inverseJoinColumn: {
    name: 'categoria_id',
    referencedColumnName: 'id'
  },
    })
        categories: Category[];
  @ManyToOne(() => Collaborator, collaborator => collaborator.promotions)
  @JoinColumn({
    name: 'colaborador_id',
    referencedColumnName: 'cognitoId'})
  collaborator: Relation<Collaborator>;

  @OneToMany(() => Notification, notifications => notifications.promotions)
    notifications: Relation<Notification>;
}