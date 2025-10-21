import { Module } from '@nestjs/common';
import { UploadController } from './upload.controller';
import { CommonModule } from 'src/common/common.module';

@Module({
  controllers: [UploadController],
  imports: [CommonModule],
})
export class UploadModule {}