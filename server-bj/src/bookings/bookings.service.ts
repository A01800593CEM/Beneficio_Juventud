import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Booking } from './entities/booking.entity';
import { CreateBookingDto } from './dto/create-booking.dto';
import { UpdateBookingDto } from './dto/update-booking.dto';

@Injectable()
export class BookingsService {
  constructor(
    @InjectRepository(Booking)
    private bookingsRepository: Repository<Booking>,
  ) {}

  async create(createBookingDto: CreateBookingDto) {
    const booking = this.bookingsRepository.create(createBookingDto);
    return this.bookingsRepository.save(booking);
  }

  async findAll() {
    return this.bookingsRepository.find({ relations: ['user', 'promotion'] });
  }

  async findOne(id: number) {
    return this.bookingsRepository.findOne({
      where: { bookingId: id },
      relations: ['user', 'promotion'],
    });
  }

  async update(id: number, updateBookingDto: UpdateBookingDto) {
    const booking = await this.bookingsRepository.preload({
      bookingId: id,
      ...updateBookingDto,
    });

    if (!booking) {
      throw new NotFoundException(`Booking with id ${id} not found`);
    }

    return this.bookingsRepository.save(booking);
  }

  remove(id: number) {
    return this.bookingsRepository.delete(id);
  }
}
