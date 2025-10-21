# 🔍 DEBUG: Sucursales y Promociones Cercanas

## 📋 Resumen del Problema

**Reporte del usuario:**
> Existen 2 sucursales a menos de 3km de la ubicación del usuario pero NO aparecen en "Cerca de ti"

## ✅ Cambios Implementados

He agregado **logs extensivos de debug** tanto en el servidor como en el cliente Android para diagnosticar por qué las sucursales cercanas no se están mostrando.

### Archivos Modificados:

#### **Servidor (Backend)**
1. `server-bj/src/promotions/promotions.service.ts` (líneas 237-337)
   - Logs de ubicación del usuario y radio de búsqueda
   - Total de promociones activas encontradas
   - Para cada promoción:
     - Nombre del colaborador
     - Total de sucursales
     - Sucursales con ubicación
     - Ubicación cruda de cada sucursal
     - Coordenadas parseadas (lat/lon)
     - Distancia calculada
     - Si fue incluida o excluida (con razón)
   - Total de resultados dentro del radio

2. `server-bj/src/collaborators/collaborators.service.ts` (líneas 214-310)
   - Mismos logs que promociones pero para colaboradores

#### **Cliente (Android)**
3. `app/src/main/java/mx/itesm/beneficiojuventud/model/promos/RemoteServicePromos.kt` (líneas 108-129)
   - Logs de request (lat, lon, radius)
   - Response code y success
   - Total de promociones encontradas
   - Listado de cada promoción con distancia

4. `app/src/main/java/mx/itesm/beneficiojuventud/model/collaborators/RemoteServiceCollab.kt` (líneas 77-98)
   - Mismos logs para colaboradores

---

## 🧪 Cómo Probar y Obtener Logs

### Paso 1: Reiniciar el Servidor

```bash
cd server-bj
# Detener servidor actual (Ctrl+C)
npm run start:dev
```

**Importante:** Deja la terminal visible para ver los logs del servidor.

### Paso 2: Compilar y Ejecutar la App

```bash
./gradlew build
./gradlew installDebug
```

O desde Android Studio: **Run** → **Run 'app'**

### Paso 3: Navegar a "Cerca de Ti"

1. Abre la app en el dispositivo/emulador
2. Asegúrate de tener permisos de ubicación activados
3. Navega a la sección de **"Cerca de ti"** en la pantalla Home

### Paso 4: Revisar Logs

#### **Logs del Servidor (Terminal):**
Verás algo como:
```
========== NEARBY PROMOTIONS DEBUG ==========
User Location: { latitude: 25.6866, longitude: -100.3161 }
Search Radius: 3 km
Total active promotions found: 10

--- Processing Promotion: 2x1 en Hamburguesas
Collaborator: Burger King
Total branches: 2
Branches with location: 2
  Branch: "Sucursal Centro" - Location raw: "(-100.3161,25.6866)"
  ✓ Parsed coords: lat=25.6866, lon=-100.3161
  Distance: 0.5 km
  Branch: "Sucursal Norte" - Location raw: "(-100.3200,25.7000)"
  ✓ Parsed coords: lat=25.7, lon=-100.32
  Distance: 1.8 km
Closest distance: 0.5 km (limit: 3 km)
✅ ADDED to results (within 3km)

...

========== RESULTS ==========
Total promotions within radius: 5
=====================================
```

#### **Logs de Android (Logcat):**
Filtra por:
- `RemoteServicePromos`
- `RemoteServiceCollab`
- `Home`

Verás:
```
D/RemoteServicePromos: ========== NEARBY PROMOTIONS REQUEST ==========
D/RemoteServicePromos: Latitude: 25.6866
D/RemoteServicePromos: Longitude: -100.3161
D/RemoteServicePromos: Radius: 3.0 km
D/RemoteServicePromos: Response Code: 200
D/RemoteServicePromos: Response Success: true
D/RemoteServicePromos: Promotions found: 5
D/RemoteServicePromos:   - 2x1 en Hamburguesas (Burger King) at 0.5km
D/RemoteServicePromos:   - 20% Descuento (Pizza Hut) at 1.2km
```

---

## 📊 Qué Buscar en los Logs

### Posibles Problemas y Qué Indicaría Cada Uno:

#### 1. **Problema: Formato de ubicación inválido**
**Log esperado:**
```
Branch: "Sucursal X" - Location raw: "algo-raro"
❌ Failed to parse location for branch "Sucursal X"
```
**Causa:** Las ubicaciones en la BD no están en formato `(lon,lat)`
**Solución:** Verificar formato en la tabla `branch`

#### 2. **Problema: No hay sucursales con ubicación**
**Log esperado:**
```
Total branches: 2
Branches with location: 0
⚠️  No branches with location, skipping...
```
**Causa:** El campo `location` está NULL en todas las sucursales
**Solución:** Insertar coordenadas en la BD

#### 3. **Problema: Distancia mal calculada**
**Log esperado:**
```
✓ Parsed coords: lat=25.6866, lon=-100.3161
Distance: 150.5 km   <-- ❌ Esto está MAL si debería ser 0.5km
```
**Causa:** Error en la fórmula de Haversine o coordenadas invertidas (lat/lon)
**Solución:** Revisar `location.utils.ts`

#### 4. **Problema: Radio muy pequeño**
**Log esperado:**
```
Closest distance: 3.5 km (limit: 3 km)
❌ EXCLUDED (distance 3.5 > 3km)
```
**Causa:** Las sucursales están justo fuera del radio de 3km
**Solución:** Aumentar el radio a 5km o verificar ubicaciones exactas

#### 5. **Problema: No hay promociones activas**
**Log esperado:**
```
Total active promotions found: 0
```
**Causa:** No hay promociones con `promotionState = 'activa'` o `endDate >= now`
**Solución:** Verificar datos en tabla `promotion`

---

## 🗄️ Verificar Datos en la Base de Datos

Si los logs muestran problemas, verifica directamente en la BD:

### Ver todas las sucursales con sus ubicaciones:
```sql
SELECT
    b.branch_id,
    b.name,
    b.address,
    b.location,
    c.business_name
FROM branch b
JOIN collaborator c ON b.collaborator_id = c.cognito_id
WHERE b.location IS NOT NULL
ORDER BY c.business_name, b.name;
```

**Formato esperado del campo `location`:**
```
(-100.3161,25.6866)
```

### Ver promociones activas con sus sucursales:
```sql
SELECT
    p.promotion_id,
    p.title,
    p.promotion_state,
    p.end_date,
    c.business_name,
    COUNT(b.branch_id) as total_branches,
    COUNT(b.location) as branches_with_location
FROM promotion p
JOIN collaborator c ON p.collaborator_id = c.cognito_id
LEFT JOIN branch b ON c.cognito_id = b.collaborator_id
WHERE p.promotion_state = 'activa'
AND p.end_date >= NOW()
GROUP BY p.promotion_id, p.title, p.promotion_state, p.end_date, c.business_name
ORDER BY c.business_name;
```

### Calcular distancia manualmente (ejemplo):
Si tu ubicación es: **lat=25.6866, lon=-100.3161**
Y la sucursal está en: **lat=25.6900, lon=-100.3200**

Puedes usar esta herramienta online: https://www.movable-type.co.uk/scripts/latlong.html

---

## 📝 Información que Necesito

Por favor, envíame:

1. **Logs completos del servidor** (desde que haces la búsqueda de "Cerca de ti")
2. **Logs de Logcat** filtrados por:
   - `RemoteServicePromos`
   - `RemoteServiceCollab`
   - `Home`
3. **Tu ubicación actual** (lat/lon que está usando la app)
4. **Resultado de esta consulta SQL:**
   ```sql
   SELECT
       c.business_name,
       b.name as branch_name,
       b.location
   FROM branch b
   JOIN collaborator c ON b.collaborator_id = c.cognito_id
   WHERE b.location IS NOT NULL
   ORDER BY c.business_name;
   ```

---

## 🔧 Posibles Soluciones Rápidas

### Si el problema es formato de ubicación:
```sql
-- Formato correcto: (lon,lat)
UPDATE branch
SET location = '(-100.3161,25.6866)'
WHERE branch_id = 1;
```

### Si el problema es que no hay ubicaciones:
```sql
-- Ejemplo: Sucursal en Monterrey Centro
UPDATE branch
SET location = '(-100.3161,25.6866)'
WHERE name = 'Sucursal Centro';

-- Ejemplo: Sucursal en San Pedro
UPDATE branch
SET location = '(-100.3700,25.6585)'
WHERE name = 'Sucursal San Pedro';
```

### Si el problema es radio muy pequeño:
Cambiar en `Home.kt` línea 186 y 198:
```kotlin
// Antes:
radius = 3.0

// Después:
radius = 5.0  // Aumentar a 5km
```

---

## 🎯 Resultado Esperado

Después del debug, deberías ver:

**En el servidor:**
```
========== RESULTS ==========
Total promotions within radius: X
Total collaborators within radius: Y
=====================================
```

**En la app:**
- Sección "Cerca de ti" muestra X promociones
- Sección "Cerca de ti" muestra Y colaboradores
- Cada uno con su distancia correcta

Si aún con esto no aparecen, los logs me dirán exactamente dónde está el problema.
