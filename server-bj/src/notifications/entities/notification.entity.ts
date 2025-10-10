import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import type { Relation } from 'typeorm';
import { NotificationType } from '../enums/notification-type.enums';
import { NotificationStatus } from '../enums/notification-status.enum';
import { Promotion } from '../../promotions/entities/promotion.entity';
import { RecipientType } from '../enums/recipient-type.enums';

@Entity({ name: 'notificacion' })
export class Notification {
  @PrimaryGeneratedColumn({ name: 'notificacion_id' })
  notificationId: number;

  @Column({ name: 'titulo' })
  title: string;

  @Column({ name: 'mensaje', type: 'text' })
  message: string;

  @Column({
    type: 'enum',
    enum: NotificationType,
    enumName: 'tipo_notificacion',
    name: 'tipo',
  })
  type: NotificationType;

  @CreateDateColumn({ name: 'fecha_envio' })
  shipmentDate: Date;

  @Column({
    type: 'enum',
    enum: RecipientType,
    enumName: 'destinatario_tipo',
    name: 'destinatario_tipo',
  })
  recipientType: RecipientType;

  @Column({ name: 'destinatario_id', type: 'int', nullable: true })
  recipientId: number | null;

  @Column({
    type: 'enum',
    enum: NotificationStatus,
    enumName: 'estado_notificacion',
    name: 'estado',
  })
  status: NotificationStatus;

  @Column({ name: 'criterios_segmento', type: 'json', nullable: true })
  segmentCriteria: any | null;

  @ManyToOne(() => Promotion, promotions => promotions.notifications)
  @JoinColumn({ name: 'promocion_id' })
  promotions: Relation<Promotion>;
}
