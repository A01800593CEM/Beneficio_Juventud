import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { AdminRole } from '../enums/admin-role.enums';
import { AdminState } from '../enums/admin-state.enum';

/**
 * Entity representing an administrator in the system.
 * Maps to the 'administrador' table in the database.
 */
@Entity({ name: 'administrador' })
export class Administrator {
  /**
   * The unique identifier for the administrator.
   * Auto-generated primary key.
   * @maps admin_id
   */
  @PrimaryGeneratedColumn({ name: 'admin_id' })
  id: number;

  /**
   * The unique identifier from Cognito for the administrator.
   * Used for authentication and linking with Cognito user.
   * @maps cognito_id
   * @maxLength 50
   */
  @Column({name: 'cognito_id'})
  cognitoId: string;

  /**
   * The administrator's first name.
   * @maps nombre
   * @maxLength 255
   */
  @Column({ name: 'nombre', type: 'varchar', length: 255 })
  firstName: string;

  /**
   * The administrator's paternal last name.
   * @maps apellido_paterno
   * @maxLength 255
   */
  @Column({ name: 'apellido_paterno', type: 'varchar', length: 255 })
  lastNameFather: string;

  /**
   * The administrator's maternal last name.
   * Optional field.
   * @maps apellido_materno
   * @maxLength 255
   */
  @Column({ name: 'apellido_materno', type: 'varchar', length: 255, nullable: true })
  lastNameMother: string | null;

  /**
   * The administrator's email address.
   * @maps correo
   * @maxLength 255
   */
  @Column({ name: 'correo', type: 'varchar', length: 255 })
  email: string;

  /**
   * The administrator's contact phone number.
   * Optional field.
   * @maps telefono
   * @maxLength 20
   */
  @Column({ name: 'telefono', type: 'varchar', length: 20, nullable: true })
  phone: string | null;

  /**
   * The role assigned to the administrator.
   * Uses AdminRole enum for possible values.
   * @maps rol
   */
  @Column({
    name: 'rol',
    type: 'enum',
    enum: AdminRole,
  })
  role: AdminRole;

  /**
   * The current status of the administrator account.
   * Uses AdminState enum for possible values.
   * @maps estado
   */
  @Column({
    name: 'estado',
    type: 'enum',
    enum: AdminState,
  })
  status: AdminState;

  /**
   * Timestamp of when the administrator record was created.
   * Automatically set by TypeORM.
   * @maps created_at
   */
  @CreateDateColumn({
    name: 'created_at',
    type: 'timestamp',
    default: () => 'CURRENT_TIMESTAMP',
  })
  createdAt: Date;

  /**
   * Timestamp of the last update to the administrator record.
   * Automatically updated by TypeORM.
   * @maps updated_at
   */
  @UpdateDateColumn({
    name: 'updated_at',
    type: 'timestamp',
    default: () => 'CURRENT_TIMESTAMP',
  })
  updatedAt: Date;
}
