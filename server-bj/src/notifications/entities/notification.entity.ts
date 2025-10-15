import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import type { Relation } from 'typeorm';
import { NotificationType } from '../enums/notification-type.enums';
import { NotificationStatus } from '../enums/notification-status.enum';
import { Promotion } from '../../promotions/entities/promotion.entity';
import { RecipientType } from '../enums/recipient-type.enums';

/**
 * Entity representing a notification in the system.
 * Handles various types of notifications including promotional and system notifications.
 * 
 * @entity Notification
 * @tableName notificacion
 */
@Entity({ name: 'notificacion' })
export class Notification {
  /**
   * Unique identifier for the notification.
   * @primaryKey
   * @columnName notificacion_id
   */
  @PrimaryGeneratedColumn({ name: 'notificacion_id' })
  notificationId: number;

  /**
   * Title of the notification.
   * @columnName titulo
   */
  @Column({ name: 'titulo' })
  title: string;

  /**
   * Detailed message content of the notification.
   * @columnName mensaje
   */
  @Column({ name: 'mensaje', type: 'text' })
  message: string;

  /**
   * Type of notification (e.g., promotional, system alert).
   * @columnName tipo
   * @enum NotificationType
   */
  @Column({
    type: 'enum',
    enum: NotificationType,
    enumName: 'tipo_notificacion',
    name: 'tipo',
  })
  type: NotificationType;

  /**
   * Date and time when the notification was sent.
   * Automatically set when the notification is created.
   * @columnName fecha_envio
   */
  @CreateDateColumn({ name: 'fecha_envio' })
  shipmentDate: Date;

  /**
   * Type of recipient for the notification (e.g., user, group).
   * @columnName destinatario_tipo
   * @enum RecipientType
   */
  @Column({
    type: 'enum',
    enum: RecipientType,
    enumName: 'destinatario_tipo',
    name: 'destinatario_tipo',
  })
  recipientType: RecipientType;

  /**
   * ID of the recipient. Can be null for broadcast notifications.
   * @columnName destinatario_id
   * @optional
   */
  @Column({ name: 'destinatario_id', type: 'int', nullable: true })
  recipientId: number | null;

  /**
   * Current status of the notification (e.g., sent, read, pending).
   * @columnName estado
   * @enum NotificationStatus
   */
  @Column({
    type: 'enum',
    enum: NotificationStatus,
    enumName: 'estado_notificacion',
    name: 'estado',
  })
  status: NotificationStatus;

  /**
   * JSON criteria for segmenting notification recipients.
   * Used for targeted notifications.
   * @columnName criterios_segmento
   * @optional
   */
  @Column({ name: 'criterios_segmento', type: 'json', nullable: true })
  segmentCriteria: any | null;

  /**
   * Relationship with associated promotion if this is a promotional notification.
   * @relation many-to-one
   * @foreignKey promocion_id
   */
  @ManyToOne(() => Promotion, promotions => promotions.notifications)
  @JoinColumn({ name: 'promocion_id' })
  promotions: Relation<Promotion>;
}
