import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { NotificationType } from '../enums/notification-type.enums';
import { NotificationStatus } from '../enums/notification-status.enum';
import { Promotion } from '../../promotions/entities/promotion.entity';
import { RecipientType } from '../enums/recipient-type.enums';

@Entity({ name: 'notificacion' })
export class Notification {
  @PrimaryGeneratedColumn({ name: 'notificacion_id' })
  notificacion_id: number;

  @Column({ name: 'titulo' })
  titulo: string;

  @Column({ name: 'mensaje', type: 'text' })
  mensaje: string;

  @Column({
    type: 'enum',
    enum: NotificationType,
    enumName: 'tipo_notificacion',
    name: 'tipo',
  })
  tipo: NotificationType;

  @CreateDateColumn({ name: 'fecha_envio' })
  fecha_envio: Date;

  @Column({
    type: 'enum',
    enum: NotificationStatus,
    enumName: 'destinatario_tipo',
    name: 'destinatario_tipo',
  })
  destinatario_tipo: RecipientType;

  @Column({ name: 'destinatario_id', type: 'int', nullable: true })
  destinatario_id: number | null;

  @Column({
    type: 'enum',
    enum: NotificationStatus,
    enumName: 'estado_notificacion',
    name: 'estado',
  })
  estado: NotificationStatus;

  @Column({ name: 'criterios_segmento', type: 'json', nullable: true })
  criterios_segmento: any | null;

  @ManyToOne(() => Promotion, { nullable: true, onDelete: 'SET NULL' })
  @JoinColumn({ name: 'promocion_id' })
  promocion: Promotion | null;
}
