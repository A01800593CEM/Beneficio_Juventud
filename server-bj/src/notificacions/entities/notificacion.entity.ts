import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { NotificationType } from '../enums/notification-type.enum';
import { RecipientType } from '../enums/recipient-type.enum';
import { NotificationState } from '../enums/notification-state.enum';
import { promocion } from './promocion.entity';

@Entity({ name: 'notificacion' })
export class Notificacion {
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
    enum: RecipientType,
    enumName: 'destinatario_tipo',
    name: 'destinatario_tipo',
  })
  destinatario_tipo: RecipientType;

  @Column({ name: 'destinatario_id', type: 'int', nullable: true })
  destinatario_id: number | null;

  @Column({
    type: 'enum',
    enum: NotificationState,
    enumName: 'estado_notificacion',
    name: 'estado',
  })
  estado: NotificationState;

  @Column({ name: 'criterios_segmento', type: 'json', nullable: true })
  criterios_segmento: any | null;

  @ManyToOne(() => promocion, { nullable: true, onDelete: 'SET NULL' })
  @JoinColumn({ name: 'promocion_id' })
  promocion: promocion | null;
}
