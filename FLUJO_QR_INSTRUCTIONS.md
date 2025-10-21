# 🎯 INSTRUCCIONES DE USO - SISTEMA DE CUPONES QR

## 📋 RESUMEN

El sistema permite que:
- **Teléfono A (Usuario)** genera un código QR con sus datos personales
- **Teléfono B (Colaborador)** escanea el código QR y registra el uso del cupón

---

## 🔧 CONFIGURACIÓN INICIAL (SOLO UNA VEZ)

### 1. Ejecutar Migración de Base de Datos

Conéctate a tu base de datos PostgreSQL y ejecuta:

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

O usando el archivo de migración:

```bash
cd server-bj
psql -U postgres -d beneficio_juventud -f migrations/add_qr_fields_to_uso_cupon.sql
```

### 2. Reiniciar Servidor Backend

```bash
cd server-bj
npm run start:dev
```

### 3. Compilar App Móvil

```bash
cd app
./gradlew assembleDebug
```

---

## 📱 CÓMO USAR EL SISTEMA

### PASO 1: USUARIO GENERA EL QR (Teléfono A)

1. **Abrir la app** en el teléfono del usuario
2. **Iniciar sesión** como usuario (no colaborador)
3. Navegar a:
   - **Opción A**: Ir a "Cupones" → Seleccionar una promoción
   - **Opción B**: Ir a "Favoritos" → Seleccionar una promoción guardada
4. **Presionar "Ver QR"**
5. El QR se genera con:
   - ID del usuario autenticado
   - ID de la promoción
   - Timestamp de generación
   - Nonce único (código anti-fraude)

**Ejemplo de QR generado:**
```
bj|v=1|pid=42|uid=a1fbe500-a091-70e3-5a7b-3b1f4537f10f|lpu=3|ts=1737380400000|n=abc12345
```

---

### PASO 2: COLABORADOR ESCANEA EL QR (Teléfono B)

1. **Abrir la app** en el teléfono del colaborador
2. **Iniciar sesión** como colaborador (no usuario)
3. **Presionar el botón central** de la barra inferior (ícono de scanner)
   - Es el botón circular grande en el centro de la barra
4. **Apuntar la cámara** al QR del Teléfono A
5. El QR se **escanea automáticamente**
6. El sistema valida automáticamente:
   - ✅ Promoción existe y está activa
   - ✅ Hay stock disponible
   - ✅ QR no ha expirado (< 24 horas)
   - ✅ QR no ha sido usado previamente
   - ✅ Usuario no excedió límites de uso

---

### PASO 3: RESULTADOS

#### ✅ **CASO EXITOSO:**
- Muestra mensaje: **"Cupón canjeado exitosamente"**
- Se guarda en la base de datos:
  - Quién lo usó (userId)
  - Qué promoción (promotionId)
  - Dónde lo canjeó (branchId)
  - Cuándo se generó el QR (qrTimestamp)
  - Cuándo se canjeó (fecha_uso)
  - Código único del QR (nonce)
- El stock de la promoción se **decrementa automáticamente**

#### ❌ **CASO DE ERROR:**
El sistema muestra mensajes específicos:

| Error | Causa |
|-------|-------|
| "Este código QR ya fue utilizado" | El QR ya fue escaneado anteriormente |
| "No hay stock disponible" | La promoción se agotó |
| "Has alcanzado el límite de X usos" | El usuario ya usó todos sus cupones |
| "La promoción no está activa" | La promoción está pausada o finalizada |
| "Código QR expirado" | El QR tiene más de 24 horas |
| "Código QR inválido" | Formato incorrecto o corrupto |

---

## 🔒 SEGURIDAD

### Prevención de Fraude

1. **Un QR = Un Uso**
   - Cada QR tiene un código único (nonce)
   - No se puede usar el mismo QR dos veces
   - Ideal para prevenir screenshots o duplicación

2. **Expiración Automática**
   - Los QR expiran después de 24 horas
   - Evita acumulación de QR antiguos

3. **Validación de Límites**
   - Respeta `limitPerUser` (límite total)
   - Respeta `dailyLimitPerUser` (límite por día)
   - No permite exceder los límites configurados

4. **Control de Stock**
   - Valida que haya stock antes de canjear
   - Decrementa stock solo si todas las validaciones pasan
   - Previene canje sin disponibilidad

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Problema: "Error al procesar el código QR"
**Solución:**
- Asegúrate de que el servidor esté corriendo
- Verifica que la app tenga conexión a internet
- Revisa que el QR no esté dañado o borroso

### Problema: "branchId siempre es 1"
**Solución:**
Por defecto usa branchId=1. Para usar la sucursal correcta:
1. El colaborador debe tener sucursales creadas
2. Modificar `BJBottomBarCollab` para pasar el branchId correcto
3. Ver sección "Configuración Avanzada" abajo

### Problema: "La cámara no abre"
**Solución:**
- Verificar permisos de cámara en la app
- Ir a Settings → Apps → Beneficio Joven → Permisos → Cámara → Permitir

### Problema: "No se guarda en la base de datos"
**Solución:**
1. Verificar que se ejecutó la migración SQL
2. Revisar logs del servidor para errores
3. Verificar conexión del servidor a PostgreSQL

---

## ⚙️ CONFIGURACIÓN AVANZADA (OPCIONAL)

### Configurar branchId del Colaborador

**Actualmente usa branchId = 1 por defecto.**

Para usar la sucursal correcta del colaborador:

#### Opción 1: Selector de Sucursal en el Perfil
Agregar en `ProfileCollab.kt`:
```kotlin
// Cargar sucursales del colaborador
LaunchedEffect(collaboratorId) {
    val branches = branchViewModel.getBranchesByCollaborator(collaboratorId)
    // Mostrar selector para que el colaborador elija su sucursal
}

// Guardar en SharedPreferences
val selectedBranchId = preferences.getInt("selected_branch_id", 1)

// Pasar a BJBottomBarCollab
BJBottomBarCollab(
    nav = nav,
    branchId = selectedBranchId
)
```

#### Opción 2: Usar Primera Sucursal Automáticamente
En `HomeScreenCollab.kt`:
```kotlin
LaunchedEffect(collaboratorId) {
    val branches = branchViewModel.getBranchesByCollaborator(collaboratorId)
    val firstBranch = branches.firstOrNull()?.branchId ?: 1
    // Guardar o pasar a componentes
}
```

---

## 📊 VERIFICAR DATOS GUARDADOS

Para verificar que los cupones se están guardando correctamente:

```sql
-- Ver cupones canjeados recientemente
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
LIMIT 10;

-- Ver cupones por promoción
SELECT
    p.title,
    COUNT(*) as total_canjeados,
    p.available_stock as stock_restante
FROM uso_cupon uc
JOIN promociones p ON uc.promocion_id = p.promocion_id
GROUP BY p.promocion_id, p.title, p.available_stock
ORDER BY total_canjeados DESC;

-- Ver cupones por usuario
SELECT
    u.nombre,
    COUNT(*) as total_usados
FROM uso_cupon uc
JOIN usuarios u ON uc.usuario_id = u.cognito_id
GROUP BY u.cognito_id, u.nombre
ORDER BY total_usados DESC;

-- Detectar posibles fraudes (mismo nonce usado múltiples veces)
SELECT
    nonce,
    COUNT(*) as veces_usado
FROM uso_cupon
WHERE nonce IS NOT NULL
GROUP BY nonce
HAVING COUNT(*) > 1;
```

---

## ✅ CHECKLIST DE VERIFICACIÓN

Antes de usar en producción, verifica:

- [ ] Migración de BD ejecutada correctamente
- [ ] Servidor backend corriendo
- [ ] App móvil compilada e instalada
- [ ] Usuario puede generar QR
- [ ] Colaborador puede escanear QR
- [ ] Se registra el uso en la BD
- [ ] Stock se decrementa correctamente
- [ ] No se puede usar el mismo QR dos veces
- [ ] QR expira después de 24 horas
- [ ] Mensajes de error son claros

---

## 🆘 SOPORTE

Si tienes problemas:

1. **Revisar logs del servidor:**
   ```bash
   cd server-bj
   npm run start:dev
   # Ver logs en tiempo real
   ```

2. **Revisar logs de la app Android:**
   - Buscar por tag `QRScannerViewModel`
   - Buscar por tag `QRScannerScreen`
   - Buscar por tag `PromoQR`

3. **Verificar estado de la promoción:**
   ```sql
   SELECT * FROM promociones WHERE promocion_id = 42;
   ```

---

## 📈 PRÓXIMOS PASOS SUGERIDOS

1. **Dashboard de Analytics**
   - Ver cupones canjeados por sucursal
   - Reportes de uso por promoción
   - Gráficas de tendencias

2. **Notificaciones**
   - Notificar al usuario cuando se canjea su cupón
   - Alertas al colaborador sobre canjes exitosos

3. **Mejoras de UX**
   - Agregar animaciones al escanear
   - Sonidos de éxito/error
   - Vibración al completar escaneo

4. **Selector de Sucursal**
   - Permitir al colaborador elegir su sucursal activa
   - Guardar preferencia en el dispositivo

---

**¡El sistema está listo para usar!** 🚀

Cualquier duda o problema, revisa los logs del servidor y de la app.
