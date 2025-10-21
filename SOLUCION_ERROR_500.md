# 🔴 SOLUCIÓN AL ERROR 500

## 🐛 **PROBLEMA IDENTIFICADO**

Los logs muestran:
```
Response Code: 500
Error Body: {"statusCode":500,"message":"Internal server error"}
```

**Causa raíz:** La base de datos NO tiene las columnas `nonce` y `qr_timestamp` que el servidor está intentando guardar.

---

## ✅ **SOLUCIÓN - EJECUTAR MIGRACIÓN DE BD**

### **Opción 1: Ejecutar script SQL (RECOMENDADO)**

```bash
# Conéctate a PostgreSQL
psql -U postgres -d beneficio_juventud

# O si usas otro usuario
psql -U tu_usuario -d beneficio_juventud
```

Luego ejecuta:

```sql
-- Agregar columnas para nonce y timestamp del QR
ALTER TABLE uso_cupon
ADD COLUMN IF NOT EXISTS nonce VARCHAR(255),
ADD COLUMN IF NOT EXISTS qr_timestamp BIGINT;

-- Crear índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_uso_cupon_nonce ON uso_cupon(nonce);
CREATE INDEX IF NOT EXISTS idx_uso_cupon_promocion_nonce ON uso_cupon(promocion_id, nonce);

-- Agregar comentarios
COMMENT ON COLUMN uso_cupon.nonce IS 'Unique nonce from QR code to prevent replay attacks';
COMMENT ON COLUMN uso_cupon.qr_timestamp IS 'Original timestamp when the QR code was generated (milliseconds since epoch)';
```

### **Opción 2: Usar archivo de migración**

```bash
cd server-bj
psql -U postgres -d beneficio_juventud -f migrations/add_qr_fields_to_uso_cupon.sql
```

---

## 🔍 **VERIFICAR QUE LA MIGRACIÓN FUE EXITOSA**

```sql
-- Ver la estructura de la tabla
\d uso_cupon

-- O en SQL puro
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'uso_cupon';
```

**Deberías ver:**
```
 column_name  |     data_type
--------------+-------------------
 uso_id       | integer
 usuario_id   | character varying
 promocion_id | integer
 sucursal_id  | integer
 fecha_uso    | timestamp
 nonce        | character varying  ← NUEVO
 qr_timestamp | bigint             ← NUEVO
```

---

## 🚀 **DESPUÉS DE LA MIGRACIÓN**

1. **Reinicia el servidor backend**
   ```bash
   cd server-bj
   # Ctrl+C para detener
   npm run start:dev
   ```

2. **Vuelve a probar el escaneo del QR**
   - El error 500 debería desaparecer
   - Deberías ver: "¡Cupón canjeado exitosamente!"

---

## 📊 **VERIFICAR QUE SE GUARDÓ CORRECTAMENTE**

Después de escanear un QR exitosamente, verifica en la BD:

```sql
-- Ver los cupones canjeados más recientes
SELECT
    uso_id,
    usuario_id,
    promocion_id,
    sucursal_id,
    nonce,
    qr_timestamp,
    fecha_uso
FROM uso_cupon
ORDER BY fecha_uso DESC
LIMIT 5;
```

**Deberías ver algo como:**
```
 uso_id | usuario_id        | promocion_id | sucursal_id | nonce     | qr_timestamp   | fecha_uso
--------+-------------------+--------------+-------------+-----------+----------------+-------------------------
 1      | a1fbe500-a091...  | 25           | 1           | 6e63c794  | 1761023967477  | 2025-01-20 23:19:51
```

---

## 🔍 **SI AÚN HAY ERROR DESPUÉS DE LA MIGRACIÓN**

### 1. Verificar que el servidor se reinició
```bash
# Ver logs del servidor
cd server-bj
npm run start:dev
```

### 2. Verificar que las columnas existen
```sql
SELECT column_name FROM information_schema.columns
WHERE table_name = 'uso_cupon' AND column_name IN ('nonce', 'qr_timestamp');
```

Debe retornar 2 filas.

### 3. Ver logs detallados del servidor

En el servidor verás:
```
[NestJS] Starting Nest application...
[TypeORM] SELECT * FROM uso_cupon LIMIT 1
[TypeORM] Detected columns: uso_id, usuario_id, promocion_id, sucursal_id, fecha_uso, nonce, qr_timestamp
```

---

## 📝 **RESUMEN**

El problema es simple:
- ❌ Base de datos no tiene las columnas nuevas
- ✅ Ejecutar migración SQL
- ✅ Reiniciar servidor
- ✅ Probar de nuevo

Los datos del QR son **100% correctos**:
```
promotionId: 25
userId: a1fbe500-a091-70e3-5a7b-3b1f4537f10f
branchId: 1
nonce: 6e63c794
qrTimestamp: 1761023967477
```

Solo falta que la BD tenga las columnas para recibirlos.
