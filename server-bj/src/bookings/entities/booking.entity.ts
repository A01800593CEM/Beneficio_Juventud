import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  ManyToOne,
} from 'typeorm';
import { User } from '../../users/entities/user.entity';
import { Promotion } from '../../promotions/entities/promotion.entity';
import { BookStatus } from '../enums/book-status.enum';

@Entity({ name: 'reserva' })
export class Booking {
  @PrimaryGeneratedColumn({ name: 'reserva_id' })
  bookingId: number;

  

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

  //Relations
  @ManyToOne(() => User, user => user.bookings)
  user: User[];

  @ManyToOne(() => Promotion, promotion => promotion.bookings)
  promotion: Promotion[];
}
