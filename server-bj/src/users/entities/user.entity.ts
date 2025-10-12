import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn, ManyToMany, JoinTable, OneToMany } from "typeorm";
import type { Relation } from 'typeorm';
import { UserState } from "../enums/user-state.enum";
import { Booking } from "src/bookings/entities/booking.entity";
import { Favorite } from "src/favorites/entities/favorite.entity";
import { Redeemedcoupon } from "src/redeemedcoupon/entities/redeemedcoupon.entity";

/**
 * Entidad que modela a un usuario final.
 *
 * @remarks
 * Contiene datos de identidad, contacto, estado de cuenta y tokens de notificación,
 * además de relaciones con reservas, favoritos y redenciones de cupones.
 */
@Entity({name: "usuario"})
export class User {
    /**
   * Identificador único del usuario.
   * @primaryKey
   * @example 123
   */
    @PrimaryGeneratedColumn({name: "usuario_id"})
    id: number;

    /**
     * The unique identifier from Cognito for the user.
     * Used for authentication and linking with Cognito user.
     * @maps cognito_id
     * @maxLength 50
     */
    @Column({name: "cognito_id"})
    cognitoId: string;

    /**
   * Nombre(s) del usuario.
   * @example "Iván"
   */
    @Column({name : "nombre"})
    name: string;

     /**
   * Apellido paterno del usuario.
   * @example "Carrillo"
   */
    @Column({name: "apellido_paterno"})
    lastNamePaternal: string;

    /**
   * Apellido materno del usuario.
   * @example "López"
   */
    @Column({name: "apellido_materno"})
    lastNameMaternal: string;

     /**
   * Fecha de nacimiento del usuario.
   * @example "2001-08-15T00:00:00.000Z"
   */
    @Column({name: "fecha_nacimiento"})
    birthDate: Date;

    /**
   * Número telefónico del usuario.
   * @example "+52 5512345678"
   */
    @Column({name: "telefono"})
    phoneNumber: string;

    /**
   * Correo electrónico del usuario.
   * @example "ivan@example.com"
   */
    @Column({name: "correo_electronico"})
    email: string;

    /**
   * Fecha de registro del usuario (se asigna automáticamente al crear).
   * @readonly
   */
    @CreateDateColumn({name: "fecha_registro"})
    registrationDate: Date;

    /**
   * Marca de tiempo de última actualización (automática).
   * @readonly
   */
    @UpdateDateColumn({name: "updated_at"})
    updatedAt: Date;

    /**
   * Estado actual de la cuenta del usuario.
   * @see UserState
   * @example UserState.ACTIVE
   */
    @Column({
        type: "enum",
        enum: UserState,
        name: "estado_cuenta"
    })
    accountState: UserState;

    // Relation with Booking Entity

     /**
   * Reservas asociadas al usuario.
   * Un usuario puede tener múltiples reservas.
   */
    @OneToMany(() => Booking, booking => booking.user)
    bookings: Booking[];
    @Column({name: "token_notificacion"})
    notificationToken: string;
    
    // Relation with Favorite Entity
    /**
   * Favoritos guardados por el usuario.
   * Un usuario puede tener múltiples favoritos.
   */
    @OneToMany(() => Favorite, favorite => favorite.user)
    favorites: Relation<Favorite[]>;

    /**
   * Redenciones de cupones realizadas por el usuario.
   * Un usuario puede tener múltiples redenciones.
   */
    @OneToMany(() => Redeemedcoupon, redeemedcoupons => redeemedcoupons.user)
    redeemedcoupon: Relation<Redeemedcoupon>;
}
