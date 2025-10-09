import { Controller, Get, Post, Body, Patch, Param, Delete } from '@nestjs/common';
import { NotificationsService } from './notifications.service';
import { CreateNotificationDto } from './dto/create-notification.dto';
import { UpdateNotificationDto } from './dto/update-notification.dto';

@Controller('notification')
export class NotificationsController {
  constructor(private readonly notificacionService: NotificationsService) {}

  @Post()
  create(@Body() createNotificacionDto: CreateNotificationDto) {
    return this.notificacionService.create(createNotificacionDto);
  }

  @Get()
  findAll() {
    return this.notificacionService.findAll();
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.notificacionService.findOne(+id);
  }

  @Patch(':id')
  update(@Param('id') id: string, @Body() updateNotificacionDto: UpdateNotificationDto) {
    return this.notificacionService.update(+id, updateNotificacionDto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.notificacionService.remove(+id);
  }
}
