// src/places/places.service.ts
import { Injectable } from '@nestjs/common';

type FindPlaceCandidate = {
  name?: string;
  place_id: string;
  formatted_address?: string;
  geometry?: unknown;
};

type FindPlaceResponse = {
  candidates: FindPlaceCandidate[];
  status: string;
};

type PlaceDetailsResponse = {
  result?: any;
  status: string;
};

@Injectable()
export class PlacesService {
  private readonly apiKey = process.env.GOOGLE_MAPS_API_KEY!;
  private readonly findFields =
    process.env.GOOGLE_PLACES_FIELDS ??
    'name,place_id,formatted_address,geometry';
  private readonly detailsFields =
    process.env.GOOGLE_PLACES_DETAILS_FIELDS ??
    'name,place_id,formatted_address,geometry,opening_hours,rating,user_ratings_total,website';

  private base = 'https://maps.googleapis.com/maps/api/place';

  /**
   * Find Place from Text (textquery)
   * Doc: https://developers.google.com/maps/documentation/places/web-service/search-find-place
   */
  async findPlaceFromText(query: string) {
    if (!this.apiKey) {
      throw new Error('Falta GOOGLE_MAPS_API_KEY en .env');
    }
    const url = new URL(`${this.base}/findplacefromtext/json`);
    url.searchParams.set('input', query);
    url.searchParams.set('inputtype', 'textquery');
    url.searchParams.set('fields', this.findFields);
    url.searchParams.set('key', this.apiKey);

    const res = await fetch(url.toString());
    if (!res.ok) {
      const text = await res.text();
      throw new Error(`FindPlace HTTP ${res.status}: ${text}`);
    }
    const data = (await res.json()) as FindPlaceResponse;

    if (data.status !== 'OK' && data.status !== 'ZERO_RESULTS') {
      throw new Error(`FindPlace error: ${data.status}`);
    }
    return data; // { candidates: [...], status }
  }

  /**
   * Place Details
   * Doc: https://developers.google.com/maps/documentation/places/web-service/details
   */
  async getPlaceDetails(placeId: string) {
    if (!this.apiKey) {
      throw new Error('Falta GOOGLE_MAPS_API_KEY en .env');
    }
    const url = new URL(`${this.base}/details/json`);
    url.searchParams.set('place_id', placeId);
    url.searchParams.set('fields', this.detailsFields);
    url.searchParams.set('key', this.apiKey);

    const res = await fetch(url.toString());
    if (!res.ok) {
      const text = await res.text();
      throw new Error(`Details HTTP ${res.status}: ${text}`);
    }
    const data = (await res.json()) as PlaceDetailsResponse;

    if (data.status !== 'OK') {
      throw new Error(`Details error: ${data.status}`);
    }
    return data.result; // objeto con los campos solicitados
  }
}
