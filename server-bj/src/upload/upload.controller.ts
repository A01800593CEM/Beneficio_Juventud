import {
  Controller,
  Post,
  UploadedFile,
  UseInterceptors,
  BadRequestException,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { S3Service } from 'src/common/services/s3.service';

@Controller('api/upload')
export class UploadController {
  constructor(private readonly s3Service: S3Service) {}

  @Post()
  @UseInterceptors(FileInterceptor('file'))
  async uploadFile(@UploadedFile() file: Express.Multer.File) {
    if (!file) {
      throw new BadRequestException('No file provided');
    }

    const imageUrl = await this.s3Service.uploadFile(file, 'general');

    return {
      success: true,
      imageUrl,
      fileName: file.filename,
      originalName: file.originalname,
      size: file.size,
      type: file.mimetype,
    };
  }
}