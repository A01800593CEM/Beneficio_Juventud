import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { AdminRole } from '../enums/admin-role.enums';
import { AdminState } from '../enums/admin-state.enum';

export enum CollaboratorStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
}

@Entity({ name: 'administrador' })
export class Administrator {
  @PrimaryGeneratedColumn({ name: 'admin_id' })
  id: number;

  @Column({ name: 'nombre', type: 'varchar', length: 255 })
  firstName: string;

  @Column({ name: 'apellido_paterno', type: 'varchar', length: 255 })
  lastNameFather: string;

  @Column({ name: 'apellido_materno', type: 'varchar', length: 255, nullable: true })
  lastNameMother: string | null;

  @Column({ name: 'correo', type: 'varchar', length: 255 })
  email: string;

  @Column({ name: 'telefono', type: 'varchar', length: 20, nullable: true })
  phone: string | null;

  @Column({
    name: 'rol',
    type: 'enum',
    enum: AdminRole,
  })
  role: AdminRole;

  @Column({
    name: 'estado',
    type: 'enum',
    enum: AdminState,
  })
  status: AdminState;

  @CreateDateColumn({
    name: 'created_at',
    type: 'timestamp',
    default: () => 'CURRENT_TIMESTAMP',
  })
  createdAt: Date;

  @UpdateDateColumn({
    name: 'updated_at',
    type: 'timestamp',
    default: () => 'CURRENT_TIMESTAMP',
  })
  updatedAt: Date;
}
