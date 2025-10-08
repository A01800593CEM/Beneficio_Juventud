import { Entity, OneToMany } from "typeorm";
import { Booking } from "../../bookings/entities/booking.entity";

@Entity({name: 'promocion'})
export class Promotion {
    @OneToMany(() => Booking, booking => booking.user)
        bookings: Booking[];
}
