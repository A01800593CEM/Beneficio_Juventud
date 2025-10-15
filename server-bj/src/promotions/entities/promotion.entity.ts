import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn, OneToMany, ManyToMany, JoinTable, ManyToOne, JoinColumn, } from 'typeorm';
import { PromotionType } from '../enums/promotion-type.enums';
import { PromotionState } from '../enums/promotion-state.enums';
import { Booking } from '../../bookings/entities/booking.entity';
import type { Relation } from 'typeorm';
import { Redeemedcoupon } from 'src/redeemedcoupon/entities/redeemedcoupon.entity';
import { Category } from 'src/categories/entities/category.entity';
import { Collaborator } from 'src/collaborators/entities/collaborator.entity';
import { Notification } from 'src/notifications/entities/notification.entity';

/**
 * Entity representing a promotion in the system.
 *
 * @remarks
 * This entity stores all core promotion information, including metadata,
 * stock, type, and state, as well as relationships to related records
 * such as bookings, redeemed coupons, notifications, and categories.
 */

@Entity({ name: 'promocion' })
export class Promotion {

  /**
   * Unique identifier of the promotion.
   * @primaryKey
   * @example 101
   */
  @PrimaryGeneratedColumn({ name: 'promocion_id' })
  promotionId: number;

  /**
   * ID of the collaborator associated with this promotion.
   * @example 12
   */
  @Column({name: 'colaborador_id'})
  collaboratorId: string

  /**
   * Title of the promotion.
   * @example "50% OFF on all drinks"
   */
  @Column({ name: 'titulo' })
  title: string;

  /**
   * Description of the promotion and its conditions.
   * @example "Valid for all beverages Monday through Friday."
   */
  @Column({ name: 'descripcion'})
  description: string;

  /**
   * URL of the image associated with the promotion.
   * @example "https://example.com/promo-banner.jpg"
   */
  @Column({ name: 'imagen_url'})
  imageUrl: string;

  /**
   * Start date of the promotion.
   * @example "2025-05-01T00:00:00.000Z"
   */
  @Column({ name: 'fecha_inicio'})
  initialDate: Date;

  /**
   * End date of the promotion.
   * @example "2025-05-31T23:59:59.000Z"
   */
  @Column({ name: 'fecha_fin'})
  endDate: Date;

  /**
   * Type of promotion (percentage, fixed discount, etc.).
   * @see PromotionType
   * @example PromotionType.PERCENTAGE
   */
  @Column({
    type: 'enum',
    enum: PromotionType,
    enumName: 'tipo_promocion',
    name: 'tipo_promocion',
  })
  promotionType: PromotionType;

  /**
   * String identifier or custom code for the promotion.
   * @example "SPRING2025"
   */
  @Column({ name: 'promocion_string'})
  promotionString: string;

  /**
   * Total number of items or coupons available in this promotion.
   * @example 500
   */
  @Column({ name: 'stock_total'})
  totalStock: number;

  /**
   * Current number of items or coupons still available.
   * @example 250
   */
  @Column({ name: 'stock_disponible'})
  availableStock: number;

  /**
   * Maximum number of redemptions allowed per user.
   * @example 3
   */
  @Column({ name: 'limite_por_usuario'})
  limitPerUser: number;

  /**
   * Maximum number of redemptions per user per day.
   * @example 1
   */
  @Column({ name: 'limite_diario_por_usuario'})
  dailyLimitPerUser: number;

  /**
   * Current state of the promotion (active, expired, draft, etc.).
   * @see PromotionState
   * @example PromotionState.ACTIVE
   */
  @Column({
    type: 'enum',
    enum: PromotionState,
    enumName: 'estado_promocion',
    name: 'estado',
  })
  promotionState: PromotionState;

  /**
   * Timestamp automatically set when the record is created.
   * @readonly
   */
  @CreateDateColumn({ name: 'created_at' })
  created_at: Date;

  /**
   * Timestamp automatically updated when the record is modified.
   * @readonly
   */
  @UpdateDateColumn({ name: 'updated_at' })
  updated_at: Date;
  
  //Relations

  /**
   * List of bookings associated with this promotion.
   * One promotion can have multiple bookings.
   */
  @OneToMany(() => Booking, bookings => bookings.promotion)
    bookings: Relation<Booking>;

  /**
   * Redeemed coupons linked to this promotion.
   * One promotion can have multiple redemptions.
   */
  @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.promotion)
    redeemedcoupon: Relation<Redeemedcoupon>

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
  @JoinColumn({name: 'colaborador_id'})
  collaborator: Relation<Collaborator>;

  @OneToMany(() => Notification, notifications => notifications.promotions)
    notifications: Relation<Notification>;
}