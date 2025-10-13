import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { BookingsService } from './bookings.service';
import { CreateBookingDto } from './dto/create-booking.dto';
import { UpdateBookingDto } from './dto/update-booking.dto';

/**
 * Controller responsible for handling HTTP requests related to bookings.
 * Provides endpoints for CRUD operations and user-specific booking queries.
 * @route /users/bookings
 */
@Controller('users/bookings')
export class BookingsController {
  /**
   * Creates an instance of the BookingsController.
   * @param bookingsService - The service handling booking business logic
   */
  constructor(private readonly bookingsService: BookingsService) {}

  /**
   * Creates a new booking.
   * @route POST /users/bookings
   * @param createBookingDto - The data transfer object containing booking details
   * @returns The newly created booking
   */
  @Post()
  create(@Body() createBookingDto: CreateBookingDto) {
    return this.bookingsService.create(createBookingDto);
  }

  /**
   * Retrieves all bookings.
   * @route GET /users/bookings
   * @returns Array of all bookings
   */
  @Get()
  findAll() {
    return this.bookingsService.findAll();
  }

  /**
   * Retrieves a specific booking by ID.
   * @route GET /users/bookings/:id
   * @param id - The unique identifier of the booking
   * @returns The booking with the specified ID
   */
  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.bookingsService.findOne(+id);
  }

  /**
   * Updates an existing booking.
   * @route PATCH /users/bookings/:id
   * @param id - The unique identifier of the booking to update
   * @param updateBookingDto - The data transfer object containing updated booking details
   * @returns The updated booking
   */
  @Patch(':id')
  update(@Param('id') id: string, @Body() updateBookingDto: UpdateBookingDto) {
    return this.bookingsService.update(+id, updateBookingDto);
  }

  /**
   * Removes a booking.
   * @route DELETE /users/bookings/:id
   * @param id - The unique identifier of the booking to remove
   * @returns Void if successful
   */
  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.bookingsService.remove(+id);
  }

  /**
   * Retrieves all bookings for a specific user.
   * @route GET /users/bookings/user_bookings/:id
   * @param id - The unique identifier of the user
   * @returns Array of bookings belonging to the specified user
   */
  @Get('user_bookings/:id')
  getUserBookings(@Param('id') id: string){
    return this.bookingsService.findByUserId(id)
  }
}
