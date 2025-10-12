import {
  Entity,
  PrimaryGeneratedColumn,
  CreateDateColumn,
  ManyToOne,
  JoinColumn,
  Column,
} from 'typeorm';
import type { Relation } from 'typeorm';

import { User } from '../../users/entities/user.entity';
import { Collaborator } from '../../collaborators/entities/collaborator.entity';
import { Branch } from '../../branch/entities/branch.entity';
import { Promotion } from '../../promotions/entities/promotion.entity';

/**
 * Entity representing a single instance of a coupon redemption.
 *
 * @remarks
 * Each record corresponds to a user redeeming a specific promotion
 * at a given collaborator and branch, and is automatically timestamped
 * with the date of redemption.
 *
 * This entity serves as a join between several domain models:
 * - {@link User} (the user who redeemed the coupon)
 * - {@link Collaborator} (the business offering the promotion)
 * - {@link Branch} (the specific location)
 * - {@link Promotion} (the redeemed promotion)
 */
@Entity({ name: 'uso_cupon' })
export class Redeemedcoupon {
  /**
   * Unique identifier for the coupon redemption record.
   * @primaryKey
   * @example 1023
   */
  @PrimaryGeneratedColumn({ name: 'uso_id' })
  usedId: number;

  /**
   * Timestamp automatically recorded when the coupon is redeemed.
   * @readonly
   * @example "2025-05-15T18:42:00.000Z"
   */
  @CreateDateColumn({ name: 'fecha_uso' })
  usedDate: Date;

  /**
   * ID of the user who redeemed the coupon.
   * @example 14
   */
  @Column({ name: 'usuario_id' })
  userId: number;

  /**
   * ID of the collaborator (business) associated with the redemption.
   * @example 3
   */
  @Column({ name: 'colaborador_id' })
  collaboratorId: number;

  /**
   * ID of the branch where the coupon was redeemed.
   * @example 8
   */
  @Column({ name: 'sucursal_id' })
  branchId: number;

  /**
   * ID of the promotion being redeemed.
   * @example 27
   */
  @Column({ name: 'promocion_id' })
  promotionId: number;

  // ────────────────────────────────
  // Relations
  // ────────────────────────────────

  /**
   * User who redeemed the coupon.
   * Many coupon uses belong to one user.
   */
  @ManyToOne(() => User, (user) => user.redeemedcoupon)
  @JoinColumn({ name: 'usuario_id' })
  user: Relation<User>;

  /**
   * Collaborator (merchant) associated with this redemption.
   * Many redemptions can occur under one collaborator.
   */
  @ManyToOne(() => Collaborator, (collaborator) => collaborator.redeemedcoupon)
  @JoinColumn({ name: 'colaborador_id' })
  collaborator: Relation<Collaborator>;

  /**
   * Branch where the coupon was redeemed.
   * Many redemptions can occur in the same branch.
   */
  @ManyToOne(() => Branch, (branch) => branch.redeemedcoupon)
  @JoinColumn({ name: 'sucursal_id' })
  branch: Relation<Branch>;

  /**
   * Promotion that was redeemed.
   * Many redemptions can belong to one promotion.
   */
  @ManyToOne(() => Promotion, (promotion) => promotion.redeemedcoupon)
  @JoinColumn({ name: 'promocion_id' })
  promotion: Relation<Promotion>;
}
