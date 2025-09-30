import { Entity, PrimaryGeneratedColumn, Column } from "typeorm";
import { UserState } from "../enums/user-state.enum";

@Entity({name: "usuario"})
export class Usuario {
    @PrimaryGeneratedColumn({name: "usuario_id"})
    id: number;

    @Column({name : "nombre"})
    name: String;

    @Column({name: "apellido_paterno"})
    lastNamePaternal: string;

    @Column({name: "apellido_materno"})
    lastNameMaternal: string;

    @Column({name: "fecha_nacimiento"})
    birthDate: Date;

    @Column({name: "telefono"})
    phoneNumber: String;

    @Column({name: "correo_electronico"})
    email: String;

    @Column({name: "fecha_registro"})
    registrationDate: Date;

    @Column({name: "updated_at"})
    updatedAt: Date;

    @Column({
        type: "enum",
        enum: UserState,
        name: "estado_cuenta"
    })
    accountState: UserState;

}
