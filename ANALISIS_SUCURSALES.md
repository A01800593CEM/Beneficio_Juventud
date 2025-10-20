# Análisis Completo: Funcionalidad de Sucursales

## Resumen Ejecutivo

Después de revisar exhaustivamente el backend y la app móvil, he identificado que **la funcionalidad de sucursales existe pero está INCOMPLETA e INCONEXA**. La relación entre Promociones y Sucursales NO está implementada actualmente.

### Estado Actual

```
✅ BACKEND: Módulo de sucursales funcional (CRUD básico)
⚠️  BACKEND: SIN autenticación/autorización
❌ BACKEND: Sin endpoint para filtrar sucursales por colaborador
❌ BACKEND: Sin relación directa Promoción ↔ Sucursal
❌ APP MÓVIL: UI de gestión de sucursales comentada/deshabilitada
❌ APP MÓVIL: Sin API service para sucursales
❌ APP MÓVIL: Sin selección de sucursales al crear promociones
```

---

## 1. Estado Actual del Backend

### ✅ Lo que SÍ existe

#### Módulo de Sucursales (Branch)
**Ubicación:** `server-bj/src/branch/`

**Endpoints disponibles:**
```typescript
POST   /branch          // Crear sucursal
GET    /branch          // Obtener todas las sucursales
GET    /branch/:id      // Obtener una sucursal por ID
PATCH  /branch/:id      // Actualizar sucursal
DELETE /branch/:id      // Eliminar sucursal
```

**Entidad Branch:**
```typescript
{
  branchId: number (PK)
  collaboratorId: string (FK -> Collaborator.cognitoId)
  name: string
  phone: string
  address: string
  zipCode: string
  location: point (PostgreSQL GIS)  // (longitude, latitude)
  jsonSchedule: JSON
  state: 'activa' | 'inactiva'
  created_at: timestamp
  updated_at: timestamp
}
```

**Relación con Colaborador:**
```
Collaborator (1) ──→ (N) Branch
```

### ❌ Lo que NO existe

1. **Autenticación y Autorización:**
   - ❌ Sin guards en endpoints
   - ❌ Cualquiera puede CRUD cualquier sucursal
   - ❌ Sin validación de que un colaborador solo edite sus propias sucursales

2. **Endpoint para Filtrar por Colaborador:**
   - ❌ No existe `GET /branch/collaborator/:collaboratorId`
   - ❌ No se puede obtener todas las sucursales de un colaborador específico

3. **Relación Promoción ↔ Sucursal:**
   - ❌ **La tabla `promocion` NO tiene campo `sucursal_id`**
   - ❌ No existe tabla de unión `promocion_sucursal`
   - ❌ Promociones solo están ligadas a Colaborador, no a sucursales específicas

---

## 2. Arquitectura Actual de Relaciones

### Base de Datos (Esquema Real)

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│ Colaborador  │ 1     N │  Sucursal    │         │  Promoción   │
│              │◄────────│              │         │              │
│ cognito_id   │         │ sucursal_id  │         │ promocion_id │
│              │         │ colaborador  │         │ colaborador  │
└──────────────┘         │   _id (FK)   │         │   _id (FK)   │
       ▲                 │ nombre       │         │ titulo       │
       │                 │ ubicacion    │         │ descripcion  │
       │                 └──────────────┘         │ ...          │
       │                                          └──────────────┘
       │                                                 │
       └─────────────────────────────────────────────────┘
                     1 : N
```

**Problema:** `Promoción` apunta a `Colaborador`, pero NO a `Sucursal`.

Esto significa que:
- ✅ Una promoción pertenece a un colaborador
- ❌ Una promoción NO está ligada a sucursales específicas
- ❌ No se puede decir "esta promoción solo aplica en Sucursal A y B"

### Relación Indirecta Actual

La única forma de saber qué sucursales tienen una promoción es:
```
Promoción → Colaborador → Todas las sucursales del colaborador
```

Es decir, **actualmente todas las promociones de un colaborador aplican implícitamente para TODAS sus sucursales**.

---

## 3. Estado Actual de la App Móvil

### ✅ Lo que SÍ existe

1. **Modelo de Datos Branch:**
```kotlin
data class Branch(
    val collaboratorId: Int,
    val name: String,
    val address: String,
    val phone: String,
    val zipCode: String,
    val location: String?,
    val jsonSchedule: String?
)
```

2. **UI Parcialmente Implementada:**
   - `SucursalCard.kt` - Tarjeta para mostrar sucursales (con mock data)
   - `EditSucursalDialog.kt` - Formulario de edición (COMPLETAMENTE COMENTADO)

3. **Uso en Nearby Search:**
   - `NearbyPromotion` incluye `closestBranch: Branch?`
   - `NearbyCollaborator` incluye `closestBranch: Branch?`

4. **Uso en Redención:**
   - `RedeemedCoupon` tiene `branchId: Int?`
   - Indica en qué sucursal se canjeó el cupón

### ❌ Lo que NO existe

1. **API Service para Sucursales:**
   - ❌ No hay `BranchApiService.kt`
   - ❌ No hay métodos para crear/actualizar/eliminar sucursales
   - ❌ No hay método para obtener sucursales de un colaborador

2. **Gestión de Sucursales en UI:**
   - ❌ Sin pantalla activa para gestionar sucursales
   - ❌ Sin navegación a gestión de sucursales
   - ❌ `EditSucursalDialog` está completamente comentado
   - ❌ No existe en perfil del colaborador

3. **Selección de Sucursales al Crear Promociones:**
   - ❌ `CreatePromotionRequest` no incluye campo para sucursales
   - ❌ `NewPromotionSheet.kt` no tiene selector de sucursales
   - ❌ Promociones se crean solo con `collaboratorId`

4. **Selector de Sucursal en QR Scanner:**
   - ❌ Usa `branchId = 1` hardcodeado
   - ❌ TODO comment indica que falta obtenerlo de la sesión

---

## 4. Funcionalidad Deseada vs Realidad

### Lo que QUIERES hacer:

1. ✅ Colaboradores pueden añadir sucursales a su perfil
2. ✅ Al crear promoción, seleccionar si aplica para:
   - Una sucursal específica
   - Varias sucursales
   - Todas las sucursales

### Lo que ACTUALMENTE sucede:

1. ⚠️  Sucursales se pueden crear vía API (sin UI)
2. ❌ Promociones NO tienen selector de sucursales
3. ❌ Promociones aplican implícitamente para TODAS las sucursales del colaborador

---

## 5. Brechas Identificadas

### Backend

| Funcionalidad Faltante | Impacto | Prioridad |
|------------------------|---------|-----------|
| Autenticación en endpoints de sucursales | 🔴 CRÍTICO - Seguridad | ALTA |
| Endpoint `GET /branch/collaborator/:id` | 🟡 MEDIO - UX | ALTA |
| Tabla de unión `promocion_sucursal` | 🔴 CRÍTICO - Funcionalidad | **MUY ALTA** |
| Lógica de negocio para promociones multi-sucursal | 🟡 MEDIO | ALTA |
| Validación: solo editar propias sucursales | 🔴 CRÍTICO - Seguridad | ALTA |

### App Móvil

| Funcionalidad Faltante | Impacto | Prioridad |
|------------------------|---------|-----------|
| `BranchApiService.kt` | 🔴 CRÍTICO | **MUY ALTA** |
| Pantalla de gestión de sucursales | 🟡 MEDIO | ALTA |
| Selector de sucursales en creación de promos | 🔴 CRÍTICO | **MUY ALTA** |
| Obtener `branchId` de sesión en QR Scanner | 🟡 MEDIO | MEDIA |
| Navegación a gestión de sucursales | 🟢 BAJO | MEDIA |

---

## 6. Solución Recomendada

Para implementar completamente la funcionalidad de sucursales con promociones, necesitas:

### FASE 1: Backend - Relación Promoción-Sucursal ⭐ CRÍTICO

#### Opción A: Many-to-Many (RECOMENDADO)

Permite que una promoción aplique a varias sucursales seleccionadas.

**1. Crear tabla de unión:**
```sql
CREATE TABLE promocion_sucursal (
    promocion_id INTEGER NOT NULL,
    sucursal_id INTEGER NOT NULL,
    PRIMARY KEY (promocion_id, sucursal_id),
    FOREIGN KEY (promocion_id) REFERENCES promocion(promocion_id) ON DELETE CASCADE,
    FOREIGN KEY (sucursal_id) REFERENCES sucursal(sucursal_id) ON DELETE CASCADE
);
```

**2. Actualizar entidad Promotion:**
```typescript
// server-bj/src/promotions/entities/promotion.entity.ts

@ManyToMany(() => Branch, branch => branch.promotions)
@JoinTable({
  name: 'promocion_sucursal',
  joinColumn: { name: 'promocion_id', referencedColumnName: 'promotionId' },
  inverseJoinColumn: { name: 'sucursal_id', referencedColumnName: 'branchId' }
})
branches: Branch[];
```

**3. Actualizar entidad Branch:**
```typescript
// server-bj/src/branch/entities/branch.entity.ts

@ManyToMany(() => Promotion, promotion => promotion.branches)
promotions: Promotion[];
```

**4. Actualizar CreatePromotionDto:**
```typescript
// server-bj/src/promotions/dto/create-promotion.dto.ts

@IsOptional()
@IsArray()
@IsInt({ each: true })
branchIds?: number[];  // IDs de sucursales donde aplica la promoción
```

**5. Actualizar PromotionsService:**
```typescript
async create(createPromotionDto: CreatePromotionDto): Promise<Promotion> {
  const { branchIds, categoryIds, ...data } = createPromotionDto;

  // Obtener categorías
  const categories = await this.categoriesRepository.findBy({
    id: In(categoryIds ?? [])
  });

  // Obtener sucursales
  let branches: Branch[] = [];
  if (branchIds && branchIds.length > 0) {
    branches = await this.branchRepository.findBy({
      branchId: In(branchIds)
    });

    // Validar que las sucursales pertenecen al colaborador
    const allBelongToCollaborator = branches.every(
      branch => branch.collaboratorId === data.collaboratorId
    );

    if (!allBelongToCollaborator) {
      throw new BadRequestException(
        'Cannot assign promotion to branches of another collaborator'
      );
    }
  } else {
    // Si no se especifican sucursales, aplicar a TODAS las del colaborador
    branches = await this.branchRepository.find({
      where: { collaboratorId: data.collaboratorId }
    });
  }

  const promotion = this.promotionsRepository.create({
    ...data,
    categories,
    branches
  });

  return this.promotionsRepository.save(promotion);
}
```

#### Opción B: Campo Nullable (NO recomendado)

Agregar `sucursal_id` nullable a tabla `promocion`:
```sql
ALTER TABLE promocion ADD COLUMN sucursal_id INTEGER;
```

**Problema:** Solo permite promociones de 1 sucursal o todas (NULL). No permite seleccionar varias.

---

### FASE 2: Backend - Endpoints de Sucursales

**1. Agregar endpoint para filtrar por colaborador:**
```typescript
// server-bj/src/branch/branch.controller.ts

@Get('collaborator/:collaboratorId')
async findByCollaborator(
  @Param('collaboratorId') collaboratorId: string
): Promise<Branch[]> {
  return this.branchService.findByCollaborator(collaboratorId);
}
```

```typescript
// server-bj/src/branch/branch.service.ts

async findByCollaborator(collaboratorId: string): Promise<Branch[]> {
  return this.branchesRepository.find({
    where: { collaboratorId },
    order: { name: 'ASC' }
  });
}
```

**2. Agregar guards de autenticación:**
```typescript
// server-bj/src/branch/branch.controller.ts

import { UseGuards } from '@nestjs/common';
import { CognitoAuthGuard } from '../auth/cognito-auth.guard';

@Controller('branch')
@UseGuards(CognitoAuthGuard)  // Requiere autenticación
export class BranchController {
  // ...
}
```

**3. Validar propiedad en updates/deletes:**
```typescript
async update(id: number, dto: UpdateBranchDto, userId: string): Promise<Branch> {
  const branch = await this.findOne(id);

  // Validar que el usuario sea el dueño de la sucursal
  if (branch.collaboratorId !== userId) {
    throw new ForbiddenException('Cannot modify branch of another collaborator');
  }

  Object.assign(branch, dto);
  return this.branchesRepository.save(branch);
}
```

---

### FASE 3: App Móvil - API Service

**Crear BranchApiService.kt:**
```kotlin
// app/src/main/java/mx/itesm/beneficiojuventud/model/branch/BranchApiService.kt

interface BranchApiService {
    @GET("branch/collaborator/{collaboratorId}")
    suspend fun getBranchesByCollaborator(
        @Path("collaboratorId") collaboratorId: String
    ): Response<List<Branch>>

    @POST("branch")
    suspend fun createBranch(
        @Body branch: Branch
    ): Response<Branch>

    @PATCH("branch/{id}")
    suspend fun updateBranch(
        @Path("id") id: Int,
        @Body branch: Branch
    ): Response<Branch>

    @DELETE("branch/{id}")
    suspend fun deleteBranch(
        @Path("id") id: Int
    ): Response<Unit>
}
```

---

### FASE 4: App Móvil - Gestión de Sucursales

**1. Crear pantalla de gestión:**
```kotlin
// app/src/main/java/mx/itesm/beneficiojuventud/viewcollab/BranchManagementScreen.kt

@Composable
fun BranchManagementScreen(
    viewModel: BranchViewModel,
    onNavigateBack: () -> Unit
) {
    val branches by viewModel.branches.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Sucursales") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddBranchDialog() }
            ) {
                Icon(Icons.Default.Add, "Agregar sucursal")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(branches) { branch ->
                    BranchCard(
                        branch = branch,
                        onEditClick = { viewModel.showEditBranchDialog(branch) },
                        onDeleteClick = { viewModel.deleteBranch(branch.branchId) }
                    )
                }
            }
        }
    }
}
```

**2. ViewModel:**
```kotlin
class BranchViewModel(
    private val branchApiService: BranchApiService,
    private val collaboratorId: String
) : ViewModel() {

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadBranches()
    }

    fun loadBranches() {
        viewModelScope.launch {
            _isLoading.value = true
            val response = branchApiService.getBranchesByCollaborator(collaboratorId)
            if (response.isSuccessful) {
                _branches.value = response.body() ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun createBranch(branch: Branch) {
        viewModelScope.launch {
            val response = branchApiService.createBranch(branch)
            if (response.isSuccessful) {
                loadBranches()
            }
        }
    }

    fun deleteBranch(branchId: Int) {
        viewModelScope.launch {
            val response = branchApiService.deleteBranch(branchId)
            if (response.isSuccessful) {
                loadBranches()
            }
        }
    }
}
```

---

### FASE 5: App Móvil - Selector de Sucursales en Promociones

**1. Actualizar CreatePromotionRequest:**
```kotlin
// app/src/main/java/mx/itesm/beneficiojuventud/model/promos/CreatePromotionRequest.kt

data class CreatePromotionRequest(
    val collaboratorId: String,
    val title: String,
    val description: String,
    // ... otros campos
    val categories: List<String>,
    val branchIds: List<Int>? = null  // ← NUEVO: IDs de sucursales
)
```

**2. Actualizar NewPromotionSheet:**
```kotlin
@Composable
fun NewPromotionSheet(
    viewModel: PromotionViewModel,
    branches: List<Branch>,  // Sucursales del colaborador
    onDismiss: () -> Unit
) {
    var selectedBranches by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var applyToAllBranches by remember { mutableStateOf(true) }

    // ... otros campos de la promoción

    // Selector de sucursales
    Column {
        Text("Sucursales donde aplica:", style = MaterialTheme.typography.titleSmall)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = applyToAllBranches,
                onCheckedChange = {
                    applyToAllBranches = it
                    if (it) selectedBranches = emptySet()
                }
            )
            Text("Todas mis sucursales")
        }

        if (!applyToAllBranches) {
            branches.forEach { branch ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = branch.branchId in selectedBranches,
                        onCheckedChange = { checked ->
                            selectedBranches = if (checked) {
                                selectedBranches + branch.branchId
                            } else {
                                selectedBranches - branch.branchId
                            }
                        }
                    )
                    Text(branch.name)
                }
            }
        }
    }

    // Al guardar
    Button(
        onClick = {
            val request = CreatePromotionRequest(
                // ... otros campos
                branchIds = if (applyToAllBranches) null else selectedBranches.toList()
            )
            viewModel.createPromotion(request)
        }
    ) {
        Text("Crear Promoción")
    }
}
```

---

## 7. Cambios en Base de Datos

### Migration SQL

```sql
-- 1. Crear tabla de unión promocion_sucursal
CREATE TABLE promocion_sucursal (
    promocion_id INTEGER NOT NULL,
    sucursal_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (promocion_id, sucursal_id),
    FOREIGN KEY (promocion_id) REFERENCES promocion(promocion_id) ON DELETE CASCADE,
    FOREIGN KEY (sucursal_id) REFERENCES sucursal(sucursal_id) ON DELETE CASCADE
);

-- 2. Crear índices para mejorar performance
CREATE INDEX idx_promocion_sucursal_promocion ON promocion_sucursal(promocion_id);
CREATE INDEX idx_promocion_sucursal_sucursal ON promocion_sucursal(sucursal_id);

-- 3. Migrar promociones existentes (aplicar a TODAS las sucursales del colaborador)
INSERT INTO promocion_sucursal (promocion_id, sucursal_id)
SELECT p.promocion_id, s.sucursal_id
FROM promocion p
JOIN sucursal s ON p.colaborador_id = s.colaborador_id;

-- 4. Índice para query común: sucursales por colaborador
CREATE INDEX idx_sucursal_colaborador ON sucursal(colaborador_id);
```

---

## 8. Flujo de Usuario Completo

### Para Colaboradores:

**Gestión de Sucursales:**
```
1. Login como colaborador
2. Ir a Perfil → Mis Sucursales
3. Ver lista de sucursales existentes
4. Agregar nueva sucursal:
   - Nombre, dirección, teléfono, código postal
   - (Opcional) Ubicación GPS
   - (Opcional) Horarios
5. Editar/eliminar sucursales existentes
```

**Creación de Promoción:**
```
1. Ir a Crear Promoción
2. Llenar datos de la promoción
3. En sección "Sucursales":
   a. Opción 1: "Todas mis sucursales" (checkbox)
   b. Opción 2: Seleccionar específicas (checkboxes)
4. Guardar promoción
```

### Para Usuarios:

**Búsqueda de Promociones:**
```
1. Ver promociones cercanas (ya implementado)
2. Ver qué sucursales específicas tienen la promo
3. Navegar al mapa para ver ubicación
4. Reservar/canjear en sucursal específica
```

---

## 9. Priorización de Implementación

### 🔴 URGENTE (Semana 1)

1. ✅ Crear tabla `promocion_sucursal` en BD
2. ✅ Actualizar entidad Promotion con relación Many-to-Many
3. ✅ Crear `BranchApiService.kt` en app
4. ✅ Endpoint `GET /branch/collaborator/:id`

### 🟡 ALTA (Semana 2)

5. ✅ Actualizar CreatePromotionDto con `branchIds`
6. ✅ Lógica de negocio en PromotionsService
7. ✅ Pantalla de gestión de sucursales en app
8. ✅ Selector de sucursales en creación de promoción

### 🟢 MEDIA (Semana 3)

9. ✅ Agregar guards de autenticación en branch endpoints
10. ✅ Validación de propiedad (solo editar propias sucursales)
11. ✅ Migración de datos existentes
12. ✅ Actualizar QR Scanner para obtener branchId de sesión

---

## 10. Archivos a Modificar/Crear

### Backend (NestJS)

**Modificar:**
- `server-bj/src/promotions/entities/promotion.entity.ts` - Agregar relación branches
- `server-bj/src/branch/entities/branch.entity.ts` - Agregar relación promotions
- `server-bj/src/promotions/dto/create-promotion.dto.ts` - Agregar branchIds[]
- `server-bj/src/promotions/promotions.service.ts` - Lógica de asignación de sucursales
- `server-bj/src/promotions/promotions.module.ts` - Importar BranchRepository
- `server-bj/src/branch/branch.controller.ts` - Agregar endpoint collaborator/:id
- `server-bj/src/branch/branch.service.ts` - Método findByCollaborator

**Crear:**
- Migration SQL para tabla `promocion_sucursal`

### App Móvil (Kotlin)

**Crear:**
- `app/src/.../model/branch/BranchApiService.kt` - API service
- `app/src/.../viewcollab/BranchManagementScreen.kt` - Pantalla de gestión
- `app/src/.../viewcollab/BranchViewModel.kt` - ViewModel
- `app/src/.../viewcollab/AddEditBranchDialog.kt` - Diálogo para CRUD

**Modificar:**
- `app/src/.../model/promos/CreatePromotionRequest.kt` - Agregar branchIds
- `app/src/.../viewcollab/NewPromotionSheet.kt` - Agregar selector de sucursales
- `app/src/.../viewcollab/QRScannerViewModel.kt` - Obtener branchId de sesión
- `app/src/.../view/Screens.kt` - Agregar ruta de gestión de sucursales

---

## 11. Testing Sugerido

### Backend

```bash
# Test crear promoción con sucursales específicas
curl -X POST https://localhost:3000/promotions \
  -d '{
    "collaboratorId": "collab123",
    "title": "2x1 Pizzas",
    "branchIds": [1, 3, 5],
    ...
  }'

# Test crear promoción para todas las sucursales (sin branchIds)
curl -X POST https://localhost:3000/promotions \
  -d '{
    "collaboratorId": "collab123",
    "title": "20% Descuento",
    ...
  }'

# Test obtener sucursales de colaborador
curl https://localhost:3000/branch/collaborator/collab123
```

### App Móvil

```kotlin
@Test
fun `test create promotion with specific branches`() = runTest {
    val request = CreatePromotionRequest(
        collaboratorId = "test123",
        title = "Test Promo",
        branchIds = listOf(1, 2)
    )

    val response = promoApiService.createPromotion(request)
    assertTrue(response.isSuccessful)
}
```

---

## 12. Diagrama de Flujo de Datos

```
┌────────────────────────────────────────────────────────────┐
│ CREAR PROMOCIÓN                                            │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│ 1. Colaborador llena formulario                           │
│    - Título, descripción, fechas, stock, etc.            │
│    - Selecciona categorías                                │
│    - Selecciona sucursales: [1, 3, 5] o "Todas"         │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│ 2. POST /promotions                                        │
│    Body: { ..., branchIds: [1, 3, 5] }                   │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│ 3. Backend valida:                                         │
│    - Las sucursales existen                                │
│    - Pertenecen al colaborador autenticado                │
│    - Si branchIds es null/vacío → usar todas las          │
│      sucursales del colaborador                            │
└────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────────┐
│ 4. Insertar en tabla promocion_sucursal:                  │
│    (promocion_id=123, sucursal_id=1)                      │
│    (promocion_id=123, sucursal_id=3)                      │
│    (promocion_id=123, sucursal_id=5)                      │
└────────────────────────────────────────────────────────────┘
```

---

## Conclusión

La funcionalidad de sucursales existe pero **NO está conectada con las promociones**. Para implementar completamente lo que deseas:

1. **Crítico:** Crear tabla `promocion_sucursal` (Many-to-Many)
2. **Crítico:** Actualizar entidades y DTOs en backend
3. **Crítico:** Crear `BranchApiService` en app móvil
4. **Importante:** UI para gestionar sucursales
5. **Importante:** Selector de sucursales al crear promociones
6. **Importante:** Seguridad (guards y validaciones)

**Estimación de Tiempo:** 2-3 semanas para implementación completa

**Archivos a crear/modificar:** ~15 archivos

¿Te gustaría que empiece a implementar alguna fase específica?
