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

@Entity({ name: 'reserva' })
export class Booking {
  @PrimaryGeneratedColumn({ name: 'reserva_id' })
  bookingId: number;

  @Column({name: 'usuario_id', type: 'varchar'})
  userId: string;

  @Column({name: 'promocion_id'})
  promotionId: number

  @CreateDateColumn({ name: 'fecha_reserva' })
  bookingDate: Date;

  @Column({ name: 'fecha_limite_uso', type: 'timestamp', nullable: true })
  limitUseDate: Date | null;

  @Column({
    type: 'enum',
    enum: BookStatus,
    enumName: 'estado_reserva',
    name: 'estado',
  })
  status: BookStatus;

  @Column({ name: 'promocion_reservada', type: 'int', nullable: true })
  bookedPromotion: number | null;

  @Column({ name: 'fecha_cancelacion', type: 'timestamp', nullable: true })
  cancelledDate: Date | null;

  //Relations
  @ManyToOne(() => User, users => users.bookings)
  @JoinColumn({ name: 'usuario_id',
                referencedColumnName: 'cognitoId' })
  user: Relation<User>;

  @ManyToOne(() => Promotion, promotions => promotions.bookings)
  @JoinColumn({ name: 'promocion_id', referencedColumnName: 'promotionId' })
  promotion: Relation<Promotion>;
}
