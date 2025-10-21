# Análisis de Funcionalidad: Recomendaciones Cerca de Ti

**Fecha:** 2025-10-21
**Componentes Analizados:** Backend (NestJS) + App Móvil (Kotlin/Jetpack Compose)

---

## 1. Resumen Ejecutivo

La funcionalidad de "Recomendaciones Cerca de Ti" permite a los usuarios visualizar promociones y colaboradores cercanos a su ubicación actual en un radio configurable (por defecto 3 km). La implementación actual presenta un **problema crítico en la consulta de datos** que impide que funcione correctamente.

### Estado Actual
- ✅ **App Móvil:** Implementación correcta y completa
- ❌ **Backend:** Implementación con error crítico en la consulta de sucursales

---

## 2. Arquitectura Implementada

### 2.1 Flujo de Datos

```
Usuario (App) → LocationManager → obtiene coordenadas GPS
     ↓
PromoViewModel.getNearbyPromotions(lat, lon, radius)
     ↓
RemoteServicePromos → API Call: GET /promotions/nearby/search?latitude={lat}&longitude={lon}&radius={radius}
     ↓
PromotionsController.findNearbyPromotions() → PromotionsService.findNearbyPromotions()
     ↓
Query a BD → Calcula distancias usando Haversine → Filtra por radio → Ordena por proximidad
     ↓
Retorna: List<NearbyPromotion> con {promotion, distance, closestBranch}
     ↓
App renderiza mapa con marcadores (NearbyPromotionsMap)
```

### 2.2 Componentes Backend

#### Endpoint
- **Ruta:** `GET /promotions/nearby/search`
- **Ubicación:** `server-bj/src/promotions/promotions.controller.ts:54-86`
- **Validaciones:**
  - Latitud/Longitud requeridas
  - Rango de latitud: -90 a 90
  - Rango de longitud: -180 a 180
  - Radio: 0 a 50 km

#### Servicio
- **Ubicación:** `server-bj/src/promotions/promotions.service.ts:230-345`
- **Método:** `findNearbyPromotions(latitude, longitude, radiusKm = 3)`

#### Utilidades de Ubicación
- **Ubicación:** `server-bj/src/common/location.utils.ts`
- **Funciones principales:**
  - `calculateDistance()`: Implementa fórmula de Haversine
  - `parseLocationString()`: Parsea formato PostgreSQL Point
  - `filterByProximity()`: Filtro genérico por proximidad

### 2.3 Componentes Móviles

#### Modelos
- **NearbyPromotion:** `app/src/main/java/mx/itesm/beneficiojuventud/model/promos/NearbyPromotion.kt`
  - Extiende `Promotions` con campos `distance` y `closestBranch`
  - Método `getFormattedDistance()` para UI

#### API Service
- **PromoApiService:** Define endpoint Retrofit
- **RemoteServicePromos:** Implementa llamada con logging detallado

#### ViewModel
- **PromoViewModel:** `getNearbyPromotions()` en línea 96-102

#### UI
- **NearbyPromotionsMap:** `app/src/main/java/mx/itesm/beneficiojuventud/components/NearbyPromotionsMap.kt`
  - Muestra Google Maps con marcadores
  - Círculo de radio de búsqueda (3 km)
  - Manejo de estado (loading, error, sin resultados)

- **Home Screen:** Integración completa en `view/Home.kt:600-748`
  - Obtiene ubicación automáticamente
  - Carga datos en paralelo (promos + colaboradores)
  - Manejo de permisos de ubicación

---

## 3. Problema Crítico Identificado

### 3.1 Descripción del Error

**Ubicación:** `server-bj/src/promotions/promotions.service.ts:242-257`

```typescript
// ❌ INCORRECTO - La consulta actual
const promotions = await this.promotionsRepository
  .createQueryBuilder('promotion')
  .leftJoinAndSelect('promotion.categories', 'category')
  .leftJoin('promotion.collaborator', 'collaborator')
  .leftJoinAndSelect('collaborator.branch', 'branch')  // ← PROBLEMA AQUÍ
  .addSelect([
    'collaborator.businessName',
    'collaborator.cognitoId',
    'collaborator.logoUrl',
  ])
  .where('promotion.promotionState = :state', {
    state: PromotionState.ACTIVE,
  })
  .andWhere('promotion.endDate >= :now', { now: new Date() })
  .getMany();
```

### 3.2 Explicación del Error

El problema está en la línea 247:
```typescript
.leftJoinAndSelect('collaborator.branch', 'branch')
```

Esta línea intenta cargar **todas las sucursales del colaborador** desde la relación `Collaborator.branch`, pero **ignora completamente la relación many-to-many `Promotion.branches`** definida en la entidad.

### 3.3 Modelo de Datos Correcto

Según las entidades TypeORM:

```typescript
// Promotion Entity (promotion.entity.ts:121-133)
@ManyToMany(() => Branch, (branch) => branch.promotions)
@JoinTable({
  name: 'promocion_sucursal',
  joinColumn: { name: 'promocion_id', referencedColumnName: 'promotionId' },
  inverseJoinColumn: { name: 'sucursal_id', referencedColumnName: 'branchId' }
})
branches: Branch[];
```

Existe una tabla intermedia `promocion_sucursal` que relaciona qué sucursales están asociadas a cada promoción.

### 3.4 Consecuencias del Error

1. **Datos incorrectos:** La consulta retorna TODAS las sucursales del colaborador, no solo las asociadas a cada promoción
2. **Cálculo de distancia erróneo:** Si una promoción está disponible solo en la sucursal A, pero el colaborador tiene sucursales B y C más cercanas al usuario, el sistema podría mostrar la promoción como "cercana" cuando en realidad no lo está
3. **Violación de reglas de negocio:** Ignora la configuración de sucursales específicas por promoción

---

## 4. Solución Propuesta

### 4.1 Cambio Necesario en el Backend

**Archivo:** `server-bj/src/promotions/promotions.service.ts`
**Método:** `findNearbyPromotions()`

#### Opción 1: Usar la relación correcta (RECOMENDADO)

```typescript
// ✅ CORRECTO - Usar la relación many-to-many de Promotion.branches
const promotions = await this.promotionsRepository
  .createQueryBuilder('promotion')
  .leftJoinAndSelect('promotion.categories', 'category')
  .leftJoinAndSelect('promotion.branches', 'branch')  // ← CAMBIO AQUÍ
  .leftJoin('promotion.collaborator', 'collaborator')
  .addSelect([
    'collaborator.businessName',
    'collaborator.cognitoId',
    'collaborator.logoUrl',
  ])
  .where('promotion.promotionState = :state', {
    state: PromotionState.ACTIVE,
  })
  .andWhere('promotion.endDate >= :now', { now: new Date() })
  .getMany();
```

**Ventajas:**
- Respeta la configuración de sucursales por promoción
- Usa correctamente la tabla intermedia `promocion_sucursal`
- Calcula distancias solo con sucursales relevantes

**Desventajas:**
- Si una promoción se creó sin especificar sucursales, no tendrá branches en la respuesta

#### Opción 2: Cargar ambas relaciones (fallback inteligente)

```typescript
// ✅ ALTERNATIVA - Cargar branches de la promoción, con fallback a todas las del colaborador
const promotions = await this.promotionsRepository
  .createQueryBuilder('promotion')
  .leftJoinAndSelect('promotion.categories', 'category')
  .leftJoinAndSelect('promotion.branches', 'promoBranches')  // Sucursales específicas
  .leftJoin('promotion.collaborator', 'collaborator')
  .leftJoinAndSelect('collaborator.branch', 'collabBranches')  // Todas las del colaborador
  .addSelect([
    'collaborator.businessName',
    'collaborator.cognitoId',
    'collaborator.logoUrl',
  ])
  .where('promotion.promotionState = :state', {
    state: PromotionState.ACTIVE,
  })
  .andWhere('promotion.endDate >= :now', { now: new Date() })
  .getMany();

// Luego en el código de procesamiento (línea 264-283):
const branches = promo.branches?.length > 0
  ? promo.branches  // Usar sucursales específicas de la promoción
  : collaborator?.branch || [];  // Fallback a todas las del colaborador
```

**Ventajas:**
- Maneja promociones sin sucursales específicas asignadas
- Retrocompatible con datos existentes

**Desventajas:**
- Query más pesada
- Lógica de fallback puede ser confusa

### 4.2 Código de Procesamiento Actualizado

Después de la consulta, el código que procesa las sucursales (líneas 264-283) **NO requiere cambios**, ya que trabaja correctamente con el array `branches`:

```typescript
// Este código ya está bien implementado
const branches = collaborator?.branch || [];  // ← Si usas Opción 1, cambia a: promo.branches || []
const branchesWithLocation = branches.filter((b: any) => b.location !== null);

if (branchesWithLocation.length === 0) {
  continue;
}

let closestDistance = Infinity;
let closestBranch = null;

for (const branch of branchesWithLocation) {
  const branchCoords = parseLocationString(branch.location);
  if (!branchCoords) continue;

  const distance = calculateDistance(userLocation, branchCoords);
  if (distance < closestDistance) {
    closestDistance = distance;
    closestBranch = { /* ... */ };
  }
}

if (closestDistance <= radiusKm && closestBranch) {
  promotionsWithDistance.push({ /* ... */ });
}
```

---

## 5. Recomendaciones Adicionales

### 5.1 Optimización de Consultas

La consulta actual carga todas las promociones activas y luego filtra en memoria. Para mejorar performance:

```typescript
// Opción: Filtrar a nivel de base de datos usando PostGIS (requiere extensión)
// Esto solo es viable si PostgreSQL tiene PostGIS instalado
const promotions = await this.promotionsRepository
  .createQueryBuilder('promotion')
  .leftJoinAndSelect('promotion.categories', 'category')
  .leftJoinAndSelect('promotion.branches', 'branch')
  .leftJoin('promotion.collaborator', 'collaborator')
  .addSelect(['collaborator.businessName', 'collaborator.cognitoId', 'collaborator.logoUrl'])
  .where('promotion.promotionState = :state', { state: PromotionState.ACTIVE })
  .andWhere('promotion.endDate >= :now', { now: new Date() })
  .andWhere('branch.location IS NOT NULL')
  .andWhere(
    `ST_DWithin(
      branch.location::geography,
      ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
      :radius
    )`,
    { lat: latitude, lon: longitude, radius: radiusKm * 1000 }  // metros
  )
  .getMany();
```

**Nota:** Esto requiere verificar si PostgreSQL tiene PostGIS instalado.

### 5.2 Validación de Datos

Agregar validación para asegurar que las promociones tengan al menos una sucursal con ubicación:

```typescript
// En el servicio de creación/actualización de promociones
if (branchIds && branchIds.length > 0) {
  const branches = await this.branchRepository.findBy({ branchId: In(branchIds) });

  // Validar que al menos una sucursal tenga ubicación
  const branchesWithLocation = branches.filter(b => b.location !== null);
  if (branchesWithLocation.length === 0) {
    throw new BadRequestException(
      'At least one branch must have a location set for nearby search to work'
    );
  }
}
```

### 5.3 Logging de Producción

El código actual tiene logging excesivo (console.log). Recomendaciones:

1. Usar un logger apropiado (Winston, Pino) en lugar de `console.log`
2. Mover los logs de debug a nivel DEBUG/TRACE
3. Mantener solo logs de ERROR en producción

```typescript
// Ejemplo con Logger de NestJS
constructor(
  // ...
  private readonly logger: Logger,
) {
  this.logger = new Logger('PromotionsService');
}

// En findNearbyPromotions:
this.logger.debug(`Searching nearby promotions at (${latitude}, ${longitude}) within ${radiusKm}km`);
this.logger.verbose(`Found ${promotions.length} active promotions`);
this.logger.warn(`No branches with location for promotion ${promoData.promotionId}`);
```

### 5.4 Caché de Resultados

Considerar implementar caché con TTL corto para reducir carga:

```typescript
import { CacheInterceptor, CacheTTL } from '@nestjs/cache-manager';

@Get('nearby/search')
@UseInterceptors(CacheInterceptor)
@CacheTTL(60000) // 1 minuto
async findNearbyPromotions(/* ... */) {
  // ...
}
```

### 5.5 Testing

Crear tests E2E para validar la funcionalidad:

```typescript
// promotions.e2e-spec.ts
describe('GET /promotions/nearby/search', () => {
  it('should return nearby promotions within 3km radius', async () => {
    const lat = 25.6866;
    const lon = -100.3161;

    const response = await request(app.getHttpServer())
      .get(`/promotions/nearby/search?latitude=${lat}&longitude=${lon}&radius=3`)
      .expect(200);

    expect(response.body).toBeInstanceOf(Array);
    response.body.forEach(promo => {
      expect(promo.distance).toBeLessThanOrEqual(3);
      expect(promo.closestBranch).toBeDefined();
    });
  });

  it('should only include promotions with branches within radius', async () => {
    // Test que verifica que promociones fuera del radio no se incluyen
  });
});
```

---

## 6. Plan de Implementación

### Fase 1: Corrección Crítica (COMPLETADA)
1. ✅ Identificar el problema en `promotions.service.ts:247`
2. ✅ Decidir entre Opción 1 o Opción 2 (se eligió Opción 1)
3. ✅ Aplicar el cambio en `findNearbyPromotions()`:
   - Línea 249: Cambiado `.leftJoinAndSelect('collaborator.branch', 'branch')` → `.leftJoinAndSelect('promotion.branches', 'branch')`
   - Línea 274: Cambiado `collaborator?.branch || []` → `promoData.branches || []`
   - Agregados comentarios explicativos
4. ⏳ Probar endpoint manualmente:
   ```bash
   curl "https://localhost:3000/promotions/nearby/search?latitude=25.6866&longitude=-100.3161&radius=3"
   ```
5. ⏳ Verificar respuesta en app móvil

### Fase 2: Validación y Mejoras (CORTO PLAZO)
1. ⏳ Agregar validación de sucursales con ubicación en creación de promociones
2. ⏳ Implementar tests E2E
3. ⏳ Revisar logging (usar Logger de NestJS)

### Fase 3: Optimización (MEDIANO PLAZO)
1. ⏳ Evaluar PostGIS para filtrado a nivel de BD
2. ⏳ Implementar caché con TTL
3. ⏳ Monitorear performance con métricas

---

## 7. Impacto del Cambio

### Riesgo
- **Bajo:** Solo cambia la consulta de datos, no la estructura de respuesta
- **Retrocompatibilidad:** Depende de la opción elegida
  - Opción 1: Puede romper si hay promociones sin sucursales asignadas
  - Opción 2: Completamente retrocompatible

### Testing Requerido
- ✅ Unit tests del servicio
- ✅ E2E tests del endpoint
- ✅ Prueba manual en app móvil
- ✅ Verificar con datos reales de producción

### Datos a Verificar Antes del Deploy
```sql
-- Verificar cuántas promociones activas tienen sucursales asignadas
SELECT
  COUNT(DISTINCT p.promocion_id) as total_activas,
  COUNT(DISTINCT ps.promocion_id) as con_sucursales,
  COUNT(DISTINCT p.promocion_id) - COUNT(DISTINCT ps.promocion_id) as sin_sucursales
FROM promocion p
LEFT JOIN promocion_sucursal ps ON p.promocion_id = ps.promocion_id
WHERE p.estado = 'activa';

-- Ver promociones sin sucursales asignadas
SELECT p.promocion_id, p.titulo, p.colaborador_id
FROM promocion p
LEFT JOIN promocion_sucursal ps ON p.promocion_id = ps.promocion_id
WHERE p.estado = 'activa' AND ps.sucursal_id IS NULL;
```

---

## 8. Referencias de Código

### Backend
- **Controller:** `server-bj/src/promotions/promotions.controller.ts:54-86`
- **Service:** `server-bj/src/promotions/promotions.service.ts:230-345`
- **Utils:** `server-bj/src/common/location.utils.ts`
- **Entities:**
  - `server-bj/src/promotions/entities/promotion.entity.ts:121-133` (relación branches)
  - `server-bj/src/branch/entities/branch.entity.ts:38-39` (campo location)
  - `server-bj/src/collaborators/entities/collaborator.entity.ts:175-176` (relación branch)

### App Móvil
- **Model:** `app/src/main/java/mx/itesm/beneficiojuventud/model/promos/NearbyPromotion.kt`
- **API:** `app/src/main/java/mx/itesm/beneficiojuventud/model/promos/PromoApiService.kt:31-36`
- **Service:** `app/src/main/java/mx/itesm/beneficiojuventud/model/promos/RemoteServicePromos.kt:103-132`
- **ViewModel:** `app/src/main/java/mx/itesm/beneficiojuventud/viewmodel/PromoViewModel.kt:96-102`
- **UI Map:** `app/src/main/java/mx/itesm/beneficiojuventud/components/NearbyPromotionsMap.kt`
- **UI Integration:** `app/src/main/java/mx/itesm/beneficiojuventud/view/Home.kt:600-748`

---

## 9. Contacto y Seguimiento

Para cualquier duda sobre esta documentación o la implementación del fix, favor de crear un issue en el repositorio con el tag `bug/nearby-promotions`.

**Última actualización:** 2025-10-21
**Autor:** Claude Code Analysis
