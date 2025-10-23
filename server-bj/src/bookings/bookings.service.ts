import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Booking } from './entities/booking.entity';
import { CreateBookingDto } from './dto/create-booking.dto';
import { UpdateBookingDto } from './dto/update-booking.dto';
import { BookStatus } from './enums/book-status.enum';

@Injectable()
export class BookingsService {
  // Tiempo de expiración automática en segundos
  private readonly AUTO_EXPIRE_SECONDS = 20;
  // Tiempo de cooldown en segundos
  private readonly COOLDOWN_SECONDS = 15;

  constructor(
    @InjectRepository(Booking)
    private bookingsRepository: Repository<Booking>,
  ) {}

  async create(createBookingDto: CreateBookingDto): Promise<Booking> {
    const { promotionId, userId } = createBookingDto;

    // Verificar si existe un booking PENDING para esta promoción y usuario
    const existingPendingBooking = await this.bookingsRepository.findOne({
      where: { promotionId, userId, status: BookStatus.PENDING },
    });

    // Si ya existe un PENDING, devolverlo sin crear uno nuevo
    if (existingPendingBooking) {
      return existingPendingBooking;
    }

    // Verificar si hay un cooldown activo para este usuario y promoción
    const lastBooking = await this.bookingsRepository.findOne({
      where: { promotionId, userId },
      order: { bookingDate: 'DESC' },
    });

    if (lastBooking && lastBooking.cooldownUntil) {
      const now = new Date();
      if (now < lastBooking.cooldownUntil) {
        throw new BadRequestException(
          `Cooldown activo. Intente nuevamente en ${Math.ceil((lastBooking.cooldownUntil.getTime() - now.getTime()) / 1000)} segundos`,
        );
      }

      // Si el cooldown ya expiró, reutilizar el booking existente (cambiar status a PENDING)
      if (lastBooking.status === BookStatus.CANCELLED) {
        const now = new Date();
        lastBooking.status = BookStatus.PENDING;
        lastBooking.cooldownUntil = null;
        lastBooking.cancelledDate = null;
        lastBooking.bookingDate = now; // Actualizar fecha de reserva al momento actual
        const autoExpireDate = new Date(now.getTime() + this.AUTO_EXPIRE_SECONDS * 1000);
        lastBooking.autoExpireDate = autoExpireDate;
        // Actualizar limitUseDate si viene en el DTO
        if (createBookingDto.limitUseDate) {
          lastBooking.limitUseDate = createBookingDto.limitUseDate;
        }
        return this.bookingsRepository.save(lastBooking);
      }
    }

    // Crear la reserva con fechas de expiración y sin cooldown inicial
    const now = new Date();
    const autoExpireDate = new Date(now.getTime() + this.AUTO_EXPIRE_SECONDS * 1000);

    const booking = this.bookingsRepository.create({
      ...createBookingDto,
      autoExpireDate,
      cooldownUntil: null, // Sin cooldown al reservar
      status: BookStatus.PENDING, // Status por defecto
    });

    return this.bookingsRepository.save(booking);
  }

  async findAll(): Promise<Booking[]> {
    return this.bookingsRepository.find({ relations: ['user', 'promotion'] });
  }

  async findOne(id: number): Promise<Booking | null> {
    return this.bookingsRepository.findOne({
      where: { bookingId: id },
      relations: ['user', 'promotion'],
    });
  }

  async update(id: number, updateBookingDto: UpdateBookingDto): Promise<Booking | null> {
    const booking = await this.bookingsRepository.preload({
      bookingId: id,
      ...updateBookingDto,
    });

    if (!booking) {
      throw new NotFoundException(`Booking with id ${id} not found`);
    }

    // Si se está cancelando la reserva, guardar la fecha de cancelación e iniciar cooldown
    if (updateBookingDto.status === BookStatus.CANCELLED && !booking.cancelledDate) {
      booking.cancelledDate = new Date();
      // Iniciar cooldown por 10 segundos (para pruebas rápidas)
      const cooldownStart = new Date();
      booking.cooldownUntil = new Date(cooldownStart.getTime() + this.COOLDOWN_SECONDS * 1000);
    }

    return this.bookingsRepository.save(booking);
  }

  async remove(id: number): Promise<void> {
    await this.bookingsRepository.delete(id);
  }

  // Service method to find bookings by userId
  async findByUserId(userId: string): Promise<Booking[]> {
    return this.bookingsRepository.find({
      where: { user: { cognitoId: userId } },
      relations: ['user', 'promotion'],
    });
  }

  // Verificar si hay cooldown activo para un usuario y promoción
  async isCooldownActive(userId: string, promotionId: number): Promise<boolean> {
    const booking = await this.bookingsRepository.findOne({
      where: { promotionId, user: { cognitoId: userId } },
      relations: ['user'],
      order: { bookingDate: 'DESC' },
    });

    if (!booking || !booking.cooldownUntil) {
      return false;
    }

    return new Date() < booking.cooldownUntil;
  }

  // Obtener información de cooldown restante
  async getCooldownInfo(userId: string, promotionId: number): Promise<{ isActive: boolean; remainingSeconds: number } | null> {
    const booking = await this.bookingsRepository.findOne({
      where: { promotionId, user: { cognitoId: userId } },
      relations: ['user'],
      order: { bookingDate: 'DESC' },
    });

    if (!booking || !booking.cooldownUntil) {
      return null;
    }

    const now = new Date();
    const isActive = now < booking.cooldownUntil;
    const remainingSeconds = isActive ? Math.ceil((booking.cooldownUntil.getTime() - now.getTime()) / 1000) : 0;

    return { isActive, remainingSeconds };
  }
}
