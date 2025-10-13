import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  ManyToOne,
  JoinColumn
} from 'typeorm';
import type { Relation } from 'typeorm';
import { User } from '../../users/entities/user.entity';
import { Promotion } from '../../promotions/entities/promotion.entity';
import { BookStatus } from '../enums/book-status.enum';

/**
 * Entity representing a booking in the system.
 * Maps to the 'reserva' table in the database.
 */
@Entity({ name: 'reserva' })
export class Booking {
  /**
   * The unique identifier for the booking.
   * Auto-generated primary key.
   * @maps reserva_id
   */
  @PrimaryGeneratedColumn({ name: 'reserva_id' })
  bookingId: number;

  /**
   * The ID of the user who made the booking.
   * Foreign key to the users table.
   * @maps usuario_id
   */
  @Column({name: 'usuario_id'})
  userId: number

  /**
   * The ID of the promotion being booked.
   * Foreign key to the promotions table.
   * @maps promocion_id
   */
  @Column({name: 'promocion_id'})
  promotionId: number

  /**
   * The date and time when the booking was created.
   * Automatically set on record creation.
   * @maps fecha_reserva
   */
  @CreateDateColumn({ name: 'fecha_reserva' })
  bookingDate: Date;

  /**
   * The deadline by which the booking must be used.
   * Can be null if there's no expiration date.
   * @maps fecha_limite_uso
   */
  @Column({ name: 'fecha_limite_uso', type: 'timestamp', nullable: true })
  limitUseDate: Date | null;

  /**
   * The current status of the booking.
   * Uses BookStatus enum to define possible states.
   * @maps estado
   */
  @Column({
    type: 'enum',
    enum: BookStatus,
    enumName: 'estado_reserva',
    name: 'estado',
  })
  status: BookStatus;

  /**
   * The number of promotions included in this booking.
   * Can be null if not applicable.
   * @maps promocion_reservada
   */
  @Column({ name: 'promocion_reservada', type: 'int', nullable: true })
  bookedPromotion: number | null;


  // RELATIONS
  /**
   * Many-to-One relationship with User entity.
   * Each booking belongs to one user.
   */
  @ManyToOne(() => User, users => users.bookings)
  @JoinColumn({ name: 'usuario_id', referencedColumnName: 'id' })
  user: Relation<User>;

  /**
   * Many-to-One relationship with Promotion entity.
   * Each booking is associated with one promotion.
   */
  @ManyToOne(() => Promotion, promotions => promotions.bookings)
  @JoinColumn({ name: 'promocion_id', referencedColumnName: 'promotionId' })
  promotion: Relation<Promotion>;
}
