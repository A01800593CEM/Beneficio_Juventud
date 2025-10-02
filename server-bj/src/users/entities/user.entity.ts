import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn } from "typeorm";
import { UserState } from "../enums/user-state.enum";

@Entity({name: "usuario"})
export class User {
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

    @CreateDateColumn({name: "fecha_registro"})
    registrationDate: Date;

    @UpdateDateColumn({name: "updated_at"})
    updatedAt: Date;

    @Column({
        type: "enum",
        enum: UserState,
        name: "estado_cuenta"
    })
    accountState: UserState;
}
