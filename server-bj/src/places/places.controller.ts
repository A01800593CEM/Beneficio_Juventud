// src/places/places.controller.ts
import { Controller, Get, Query, Param, BadRequestException } from '@nestjs/common';
import { PlacesService } from './places.service';

@Controller('places')
export class PlacesController {
  constructor(private readonly places: PlacesService) {}

  // 🧩 3. Find Place from Text
  // GET /places/find?query=Starbucks%20Polanco
  @Get('find')
  async find(@Query('query') query?: string) {
    if (!query || !query.trim()) {
      throw new BadRequestException('Falta query');
    }
    const res = await this.places.findPlaceFromText(query.trim());
    // Devuelve lista breve con place_id(s) para que el cliente elija
    return {
      total: res.candidates.length,
      candidates: res.candidates.map((c) => ({
        name: c.name,
        place_id: c.place_id,
        formatted_address: c.formatted_address,
      })),
    };
  }

  // GET /places/:placeId
  // Usa el place_id obtenido para traer detalles
  @Get(':placeId')
  async details(@Param('placeId') placeId: string) {
    if (!placeId) throw new BadRequestException('Falta placeId');
    const result = await this.places.getPlaceDetails(placeId);
    return result;
  }
}
