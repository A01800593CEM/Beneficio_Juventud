import { Injectable, Logger } from '@nestjs/common';
import axios, { AxiosInstance } from 'axios';

export interface PlacePrediction {
  placeId: string;
  mainText: string;
  secondaryText: string;
  fullText: string;
  description: string;
}

export interface PlaceDetails {
  address: string;
  latitude: number;
  longitude: number;
  placeId: string;
}

/**
 * Service for Google Places Autocomplete API
 * Provides address suggestions as the user types
 */
@Injectable()
export class PlacesAutocompleteService {
  private readonly logger = new Logger(PlacesAutocompleteService.name);
  private httpClient: AxiosInstance;
  private readonly googleApiKey: string;
  private readonly placesAutocompleteUrl =
    'https://maps.googleapis.com/maps/api/place/autocomplete/json';
  private readonly placeDetailsUrl =
    'https://maps.googleapis.com/maps/api/place/details/json';

  constructor() {
    this.googleApiKey = process.env.GOOGLE_MAPS_API_KEY || '';
    this.httpClient = axios.create({
      timeout: 5000,
    });

    if (!this.googleApiKey) {
      this.logger.warn(
        'GOOGLE_MAPS_API_KEY is not configured. Places Autocomplete service will not work.',
      );
    }
  }

  /**
   * Get autocomplete suggestions for an address query
   * @param input The address query string
   * @param country Optional country restriction (e.g., "MX", "US", "FR")
   * @param sessionToken Optional session token to group queries and selections
   * @returns Array of place predictions
   */
  async getAddressPredictions(
    input: string,
    country?: string,
    sessionToken?: string,
  ): Promise<PlacePrediction[]> {
    if (!this.googleApiKey) {
      this.logger.error('Google Maps API key is not configured');
      throw new Error('Places Autocomplete service is not configured');
    }

    if (!input || input.trim().length < 2) {
      return [];
    }

    try {
      this.logger.debug(
        `Getting autocomplete predictions for: "${input}" in country: ${country || 'any'}`,
      );

      const params: any = {
        input: input.trim(),
        key: this.googleApiKey,
        components: 'country:' + (country || 'mx'), // Default to Mexico
        language: 'es', // Spanish language
      };

      if (sessionToken) {
        params.sessiontoken = sessionToken;
      }

      const response = await this.httpClient.get(this.placesAutocompleteUrl, {
        params,
      });

      if (response.data.status !== 'OK' && response.data.status !== 'ZERO_RESULTS') {
        this.logger.warn(
          `Places API error: ${response.data.status}`,
          response.data.error_message,
        );

        if (response.data.status === 'OVER_QUERY_LIMIT') {
          throw new Error('Places service rate limit exceeded');
        } else if (response.data.status === 'REQUEST_DENIED') {
          throw new Error('Places service access denied');
        }
      }

      const predictions = response.data.predictions || [];

      return predictions.map((prediction: any) => ({
        placeId: prediction.place_id,
        mainText: prediction.structured_formatting?.main_text || prediction.description,
        secondaryText: prediction.structured_formatting?.secondary_text || '',
        fullText: prediction.description,
        description: prediction.description,
      }));
    } catch (error) {
      if (axios.isAxiosError(error)) {
        this.logger.error(
          `Places Autocomplete API error: ${error.message}`,
          error.response?.data,
        );
        throw new Error(`Places Autocomplete error: ${error.message}`);
      }

      throw error;
    }
  }

  /**
   * Get detailed information about a place (coordinates, full address)
   * @param placeId The place ID from autocomplete prediction
   * @param sessionToken Optional session token from autocomplete session
   * @returns Place details including coordinates
   */
  async getPlaceDetails(
    placeId: string,
    sessionToken?: string,
  ): Promise<PlaceDetails> {
    if (!this.googleApiKey) {
      this.logger.error('Google Maps API key is not configured');
      throw new Error('Places Autocomplete service is not configured');
    }

    try {
      this.logger.debug(`Getting place details for placeId: ${placeId}`);

      const params: any = {
        place_id: placeId,
        key: this.googleApiKey,
        fields: 'geometry,formatted_address,address_components',
      };

      if (sessionToken) {
        params.sessiontoken = sessionToken;
      }

      const response = await this.httpClient.get(this.placeDetailsUrl, { params });

      if (response.data.status !== 'OK') {
        this.logger.warn(`Places API error: ${response.data.status}`);
        throw new Error(`Failed to get place details: ${response.data.status}`);
      }

      const result = response.data.result;

      const placeDetails: PlaceDetails = {
        address: result.formatted_address || '',
        latitude: result.geometry.location.lat,
        longitude: result.geometry.location.lng,
        placeId,
      };

      this.logger.debug(
        `Successfully got place details: ${placeDetails.address}`,
      );

      return placeDetails;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        this.logger.error(
          `Places Details API error: ${error.message}`,
          error.response?.data,
        );
        throw new Error(`Places Details error: ${error.message}`);
      }

      throw error;
    }
  }

  /**
   * Get a session token for grouping autocomplete and selection queries
   * Reduces cost by billing for 1 session instead of multiple requests
   * @returns A new session token
   */
  generateSessionToken(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
}
