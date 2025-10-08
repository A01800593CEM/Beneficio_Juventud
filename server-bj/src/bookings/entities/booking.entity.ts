import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  ManyToOne,
  JoinColumn,
} from 'typeorm';
import { User } from '../../users/entities/user.entity';
import { Promotion } from '../../promotions/entities/promotion.entity';
import { BookStatus } from '../enums/book-status.enum';

@Entity({ name: 'reserva' })
export class Booking {
  @PrimaryGeneratedColumn({ name: 'reserva_id' })
  bookingId: number;

  @ManyToOne(() => User, user => user.bookings)
  @JoinColumn({ name: 'usuario_id' })
  user: User;

  @ManyToOne(() => Promotion, promotion => promotion.bookings)
  @JoinColumn({ name: 'promocion_id' })
  promotion: Promotion;

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
}
