import {
  Controller,
  Post,
  BadRequestException,
  HttpCode,
  HttpStatus,
  Req,
} from '@nestjs/common';
import { FastifyRequest } from 'fastify';
import { UploadsService } from './uploads.service';

@Controller('upload')
export class UploadsController {
  constructor(private readonly uploadsService: UploadsService) {}

  @Post()
  @HttpCode(HttpStatus.OK)
  async uploadImage(@Req() request: FastifyRequest) {
    try {
      const data = await request.file();

      if (!data) {
        throw new BadRequestException('No file provided');
      }

      const buffer = await data.toBuffer();
      const file = {
        buffer,
        originalname: data.filename,
        mimetype: data.mimetype,
        size: buffer.length,
      };

      // Validar tipo de archivo
      const allowedMimes = ['image/jpeg', 'image/png', 'image/webp'];
      if (!allowedMimes.includes(file.mimetype)) {
        throw new BadRequestException(
          'Invalid file type. Only JPEG, PNG, and WebP are allowed',
        );
      }

      // Validar tamaño (5MB máximo)
      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        throw new BadRequestException(
          'File too large. Maximum size is 5MB',
        );
      }

      const imageUrl = await this.uploadsService.uploadViaWebhook(file);
      return {
        success: true,
        imageUrl,
        fileName: file.originalname,
        originalName: file.originalname,
        size: file.size,
        type: file.mimetype,
      };
    } catch (error) {
      if (error instanceof BadRequestException) {
        throw error;
      }
      throw new BadRequestException(
        error.message || 'Error uploading file via webhook',
      );
    }
  }
}
