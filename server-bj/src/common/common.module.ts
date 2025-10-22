import { Module } from '@nestjs/common';
import { GeocodingService } from './geocoding.service';
import { PlacesAutocompleteService } from './places-autocomplete.service';

@Module({
  providers: [GeocodingService, PlacesAutocompleteService],
  exports: [GeocodingService, PlacesAutocompleteService],
})
export class CommonModule {}
