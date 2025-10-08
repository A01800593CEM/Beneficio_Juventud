import { Entity, PrimaryGeneratedColumn, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { User } from '../../users/entities/user.entity';
import { colaborators } from '../../collaborators/entities/collaborator.entity';
import { Sucursal } from '../../sucursal/entities/sucursal.entity';
import { Promotion } from './promotion.entity';

@Entity({ name: 'uso_cupon' })
export class usocupon {
  @PrimaryGeneratedColumn({ name: 'uso_id' })
  uso_id: number;

  @ManyToOne(() => User)
  @JoinColumn({ name: 'usuario_id' })
  usuario: User;

  @ManyToOne(() => colaborators)
  @JoinColumn({ name: 'colaborador_id' })
  colaborador: colaborators;

  @ManyToOne(() => Sucursal)
  @JoinColumn({ name: 'sucursal_id' })
  sucursal: Sucursal;

  @ManyToOne(() => Promotion)
  @JoinColumn({ name: 'promocion_id' })
  promocion: Promotion;

  @CreateDateColumn({ name: 'fecha_uso' })
  fecha_uso: Date;
}
