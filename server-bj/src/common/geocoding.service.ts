import { Injectable, Logger } from '@nestjs/common';
import { Coordinates } from './location.utils';
import axios, { AxiosInstance } from 'axios';

export interface GeocodingResult {
  address: string;
  coordinates: Coordinates;
  formattedAddress: string;
  placeId?: string;
}

/**
 * Service for geocoding addresses to coordinates using Google Maps Geocoding API
 * Supports forward geocoding (address -> coordinates)
 */
@Injectable()
export class GeocodingService {
  private readonly logger = new Logger(GeocodingService.name);
  private httpClient: AxiosInstance;
  private readonly googleApiKey: string;
  private readonly googleGeocodeUrl = 'https://maps.googleapis.com/maps/api/geocode/json';

  constructor() {
    this.googleApiKey = process.env.GOOGLE_MAPS_API_KEY || '';
    this.httpClient = axios.create({
      timeout: 5000, // 5 segundos timeout
    });

    if (!this.googleApiKey) {
      this.logger.warn(
        'GOOGLE_MAPS_API_KEY is not configured. Geocoding service will not work.',
      );
    }
  }

  /**
   * Converts an address string to coordinates using Google Maps Geocoding API
   * @param address The address to geocode
   * @param components Optional country/region restrictions (e.g., "country:MX")
   * @returns GeocodingResult with coordinates and formatted address
   * @throws Error if geocoding fails or API key is not configured
   */
  async geocodeAddress(
    address: string,
    components?: string,
  ): Promise<GeocodingResult> {
    if (!this.googleApiKey) {
      this.logger.error('Google Maps API key is not configured');
      throw new Error('Geocoding service is not configured');
    }

    if (!address || address.trim().length === 0) {
      throw new Error('Address cannot be empty');
    }

    try {
      this.logger.debug(`Geocoding address: ${address}`);

      const params: any = {
        address: address.trim(),
        key: this.googleApiKey,
      };

      // Add components filter if provided (e.g., country restriction)
      if (components) {
        params.components = components;
      }

      const response = await this.httpClient.get(this.googleGeocodeUrl, { params });

      if (response.data.status !== 'OK') {
        const errorMessage = `Geocoding failed: ${response.data.status}`;
        this.logger.warn(`${errorMessage} for address: ${address}`);

        if (response.data.status === 'ZERO_RESULTS') {
          throw new Error(`No results found for address: ${address}`);
        } else if (response.data.status === 'OVER_QUERY_LIMIT') {
          throw new Error('Geocoding service rate limit exceeded');
        } else if (response.data.status === 'REQUEST_DENIED') {
          throw new Error('Geocoding service access denied');
        }

        throw new Error(errorMessage);
      }

      const results = response.data.results;
      if (!results || results.length === 0) {
        throw new Error(`No geocoding results found for address: ${address}`);
      }

      // Use the first (most relevant) result
      const result = results[0];

      const geocodingResult: GeocodingResult = {
        address: address.trim(),
        coordinates: {
          latitude: result.geometry.location.lat,
          longitude: result.geometry.location.lng,
        },
        formattedAddress: result.formatted_address,
        placeId: result.place_id,
      };

      this.logger.debug(
        `Successfully geocoded "${address}" to lat=${geocodingResult.coordinates.latitude}, lon=${geocodingResult.coordinates.longitude}`,
      );

      return geocodingResult;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        this.logger.error(
          `Geocoding API error: ${error.message}`,
          error.response?.data,
        );
        throw new Error(`Geocoding service error: ${error.message}`);
      }

      throw error;
    }
  }

  /**
   * Batch geocode multiple addresses
   * @param addresses Array of addresses to geocode
   * @param components Optional country/region restrictions
   * @returns Array of GeocodingResults
   */
  async geocodeAddresses(
    addresses: string[],
    components?: string,
  ): Promise<GeocodingResult[]> {
    const results = await Promise.allSettled(
      addresses.map((addr) => this.geocodeAddress(addr, components)),
    );

    return results
      .map((result, index) => {
        if (result.status === 'fulfilled') {
          return result.value;
        } else {
          this.logger.error(
            `Failed to geocode address ${index}: ${result.reason}`,
          );
          return null;
        }
      })
      .filter((result) => result !== null) as GeocodingResult[];
  }

  /**
   * Format coordinates as PostgreSQL Point string "(longitude,latitude)"
   * @param coordinates The coordinates to format
   * @returns Formatted string for database storage
   */
  formatCoordinatesForDatabase(coordinates: Coordinates): string {
    return `(${coordinates.longitude},${coordinates.latitude})`;
  }
}
