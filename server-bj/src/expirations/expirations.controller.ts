import { Controller, Get } from '@nestjs/common';
import { ExpirationsService } from './expirations.service';

@Controller('expirations')
export class ExpirationsController {
  constructor(private readonly expirationsService: ExpirationsService) {}

  @Get('check')
  async runManual() {
    return this.expirationsService.runManualCheck();
  }
}
