/**
 * Utilidades para cálculos de ubicación geográfica
 */

export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface LocationResult<T> {
  data: T;
  distance: number; // distancia en kilómetros
}

/**
 * Radio de la Tierra en kilómetros
 */
const EARTH_RADIUS_KM = 6371;

/**
 * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine
 * @param point1 Coordenadas del primer punto
 * @param point2 Coordenadas del segundo punto
 * @returns Distancia en kilómetros
 */
export function calculateDistance(
  point1: Coordinates,
  point2: Coordinates,
): number {
  const lat1Rad = toRadians(point1.latitude);
  const lat2Rad = toRadians(point2.latitude);
  const deltaLatRad = toRadians(point2.latitude - point1.latitude);
  const deltaLonRad = toRadians(point2.longitude - point1.longitude);

  // Fórmula de Haversine
  const a =
    Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
    Math.cos(lat1Rad) *
      Math.cos(lat2Rad) *
      Math.sin(deltaLonRad / 2) *
      Math.sin(deltaLonRad / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = EARTH_RADIUS_KM * c;

  return Number(distance.toFixed(2)); // Redondear a 2 decimales
}

/**
 * Convierte grados a radianes
 */
function toRadians(degrees: number): number {
  return degrees * (Math.PI / 180);
}

/**
 * Parsea una cadena de ubicación en formato PostgreSQL Point "(lon,lat)"
 * @param locationString Cadena en formato "(longitude,latitude)"
 * @returns Objeto con coordenadas o null si el formato es inválido
 */
export function parseLocationString(
  locationString: string | null,
): Coordinates | null {
  if (!locationString) return null;

  // Formato esperado: "(longitude,latitude)" o "(lon, lat)"
  const match = locationString.match(
    /^\(?\s*(-?\d+\.?\d*)\s*,\s*(-?\d+\.?\d*)\s*\)?$/,
  );

  if (!match) return null;

  const longitude = parseFloat(match[1]);
  const latitude = parseFloat(match[2]);

  // Validar rangos válidos
  if (
    longitude < -180 ||
    longitude > 180 ||
    latitude < -90 ||
    latitude > 90
  ) {
    return null;
  }

  return { latitude, longitude };
}

/**
 * Filtra elementos por proximidad a un punto de referencia
 * @param items Lista de elementos a filtrar
 * @param userLocation Ubicación del usuario
 * @param getItemLocation Función para extraer las coordenadas del elemento
 * @param maxDistanceKm Distancia máxima en kilómetros (por defecto 3km)
 * @returns Lista de elementos con distancia calculada, ordenados por proximidad
 */
export function filterByProximity<T>(
  items: T[],
  userLocation: Coordinates,
  getItemLocation: (item: T) => Coordinates | null,
  maxDistanceKm: number = 3,
): LocationResult<T>[] {
  const results: LocationResult<T>[] = [];

  for (const item of items) {
    const itemLocation = getItemLocation(item);
    if (!itemLocation) continue;

    const distance = calculateDistance(userLocation, itemLocation);

    if (distance <= maxDistanceKm) {
      results.push({
        data: item,
        distance,
      });
    }
  }

  // Ordenar por distancia (más cercano primero)
  return results.sort((a, b) => a.distance - b.distance);
}
