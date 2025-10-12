import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Booking } from './entities/booking.entity';
import { CreateBookingDto } from './dto/create-booking.dto';
import { UpdateBookingDto } from './dto/update-booking.dto';

/**
 * Service handling all booking-related operations in the system.
 * Provides methods for CRUD operations and custom queries for bookings.
 */
@Injectable()
export class BookingsService {
  /**
   * Creates an instance of the BookingsService.
   * @param bookingsRepository - The TypeORM repository for Booking entities
   */
  constructor(
    @InjectRepository(Booking)
    private bookingsRepository: Repository<Booking>,
  ) {}

  /**
   * Creates a new booking in the system.
   * @param createBookingDto - The data transfer object containing booking details
   * @returns Promise resolving to the created booking
   */
  async create(createBookingDto: CreateBookingDto): Promise<Booking> {
    const booking = this.bookingsRepository.create(createBookingDto);
    return this.bookingsRepository.save(booking);
  }

  /**
   * Retrieves all bookings from the system.
   * Includes related user and promotion data.
   * @returns Promise resolving to an array of all bookings
   */
  async findAll(): Promise<Booking[]> {
    return this.bookingsRepository.find({ relations: ['user', 'promotion'] });
  }

  /**
   * Retrieves a specific booking by its ID.
   * Includes related user and promotion data.
   * @param id - The unique identifier of the booking
   * @returns Promise resolving to the found booking or null if not found
   */
  async findOne(id: number): Promise<Booking | null> {
    return this.bookingsRepository.findOne({
      where: { bookingId: id },
      relations: ['user', 'promotion'],
    });
  }

  /**
   * Updates an existing booking with new data.
   * @param id - The unique identifier of the booking to update
   * @param updateBookingDto - The data transfer object containing updated booking details
   * @returns Promise resolving to the updated booking
   * @throws NotFoundException if the booking doesn't exist
   */
  async update(id: number, updateBookingDto: UpdateBookingDto): Promise<Booking | null> {
    const booking = await this.bookingsRepository.preload({
      bookingId: id,
      ...updateBookingDto,
    });

    if (!booking) {
      throw new NotFoundException(`Booking with id ${id} not found`);
    }

    return this.bookingsRepository.save(booking);
  }

  /**
   * Removes a booking from the system.
   * @param id - The unique identifier of the booking to remove
   * @returns Promise resolving when the booking is deleted
   */
  async remove(id: number): Promise<void> {
    await this.bookingsRepository.delete(id);
  }

  // Service method to find bookings by userId
  /**
   * Finds all bookings associated with a specific user.
   * Includes related user and promotion data.
   * @param userId - The unique identifier of the user
   * @returns Promise resolving to an array of the user's bookings
   */
  async findByUserId(userId: number): Promise<Booking[]> {
    return this.bookingsRepository.find({
      where: { user: { id: userId } },
      relations: ['user', 'promotion'],
    });
  }
}
