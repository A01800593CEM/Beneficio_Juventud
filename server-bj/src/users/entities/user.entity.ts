import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn, ManyToMany, JoinTable, OneToMany } from "typeorm";
import type { Relation } from 'typeorm';
import { UserState } from "../enums/user-state.enum";
import { Booking } from "src/bookings/entities/booking.entity";
import { Favorite } from "src/favorites/entities/favorite.entity";
import { Redeemedcoupon } from "src/redeemedcoupon/entities/redeemedcoupon.entity";

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

    // Relation with Booking Entity
    @OneToMany(() => Booking, booking => booking.user)
    bookings: Booking[];
    @Column({name: "token_notificacion"})
    notificationToken: string;
    
    // Relation with Favorite Entity
    @OneToMany(() => Favorite, favorite => favorite.user)
    favorites: Relation<Favorite[]>;

    @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.user)
    redeemedcoupon: Relation<Redeemedcoupon>;
}
