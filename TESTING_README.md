# Guía de Pruebas - Beneficio Juventud

Esta guía explica cómo ejecutar y crear pruebas para la aplicación Beneficio Juventud.

## Tabla de Contenidos

1. [Estructura de Pruebas](#estructura-de-pruebas)
2. [Tipos de Pruebas](#tipos-de-pruebas)
3. [Ejecutar Pruebas](#ejecutar-pruebas)
4. [Cobertura de Pruebas](#cobertura-de-pruebas)
5. [Mejores Prácticas](#mejores-prácticas)

---

## Estructura de Pruebas

```
app/src/
├── test/                                    # Pruebas unitarias (JUnit)
│   └── java/mx/itesm/beneficiojuventud/
│       ├── viewmodel/
│       │   ├── AuthViewModelTest.kt        # Pruebas de autenticación
│       │   ├── PromoViewModelTest.kt       # Pruebas de promociones
│       │   ├── UserViewModelTest.kt        # Pruebas de usuarios
│       │   └── CollabViewModelTest.kt      # Pruebas de colaboradores
│       └── ExampleUnitTest.kt
│
└── androidTest/                             # Pruebas de UI (Espresso)
    └── java/mx/itesm/beneficiojuventud/
        ├── ui/
        │   ├── AuthFlowUITest.kt           # Pruebas UI de autenticación
        │   ├── PromotionsFlowUITest.kt     # Pruebas UI de promociones
        │   └── ProfileFlowUITest.kt        # Pruebas UI de perfil
        └── ExampleInstrumentedTest.kt
```

---

## Tipos de Pruebas

### 1. Pruebas Unitarias (`test/`)

Las pruebas unitarias validan la lógica de negocio sin depender de componentes de Android.

**Tecnologías:**
- JUnit 4
- Kotlinx Coroutines Test
- MockK (para mocks)
- Turbine (para testing de Flows)
- AndroidX Core Testing

**Archivos:**
- `AuthViewModelTest.kt` - 40+ pruebas
- `PromoViewModelTest.kt` - 30+ pruebas
- `UserViewModelTest.kt` - 35+ pruebas
- `CollabViewModelTest.kt` - 30+ pruebas

### 2. Pruebas de UI (`androidTest/`)

Las pruebas de UI validan la interfaz de usuario y la interacción del usuario.

**Tecnologías:**
- Espresso
- Compose UI Test
- AndroidJUnit4
- Navigation Testing

**Archivos:**
- `AuthFlowUITest.kt` - 20+ pruebas
- `PromotionsFlowUITest.kt` - 25+ pruebas
- `ProfileFlowUITest.kt` - 20+ pruebas

---

## Ejecutar Pruebas

### Desde Android Studio

#### Ejecutar todas las pruebas unitarias:
1. Click derecho en `app/src/test`
2. Seleccionar "Run 'All Tests'"

#### Ejecutar todas las pruebas de UI:
1. Click derecho en `app/src/androidTest`
2. Seleccionar "Run 'All Tests'"
3. **Nota:** Requiere un emulador o dispositivo físico conectado

#### Ejecutar una clase de prueba específica:
1. Abrir el archivo de prueba (ej: `AuthViewModelTest.kt`)
2. Click en el icono verde junto a la clase
3. Seleccionar "Run 'AuthViewModelTest'"

#### Ejecutar una prueba individual:
1. Click en el icono verde junto al método de prueba
2. Seleccionar "Run 'nombre_de_prueba'"

### Desde Terminal/Gradle

#### Ejecutar todas las pruebas unitarias:
```bash
./gradlew test
```

#### Ejecutar todas las pruebas de UI:
```bash
./gradlew connectedAndroidTest
```

#### Ejecutar pruebas de un módulo específico:
```bash
./gradlew :app:test
./gradlew :app:connectedAndroidTest
```

#### Ejecutar con reporte de cobertura:
```bash
./gradlew test jacocoTestReport
```

---

## Cobertura de Pruebas

### Pruebas Unitarias Incluidas

#### AuthViewModelTest (40 pruebas)
- ✅ Estado inicial
- ✅ Gestión de perfiles temporales (UserProfile y Collaborator)
- ✅ Credenciales temporales
- ✅ Limpieza de estado
- ✅ Validación de flujos (StateFlow)
- ✅ Manejo de errores
- ✅ Casos límite (edge cases)
- ✅ Concurrencia
- ✅ SessionKey management

#### PromoViewModelTest (30 pruebas)
- ✅ Estado inicial
- ✅ Sistema de favoritos locales
- ✅ Toggle de favoritos
- ✅ Múltiples favoritos simultáneos
- ✅ Validación de modelo Promotions
- ✅ Flujos de datos
- ✅ Casos límite
- ✅ Concurrencia
- ✅ Performance con grandes conjuntos

#### UserViewModelTest (35 pruebas)
- ✅ Estado inicial
- ✅ Limpieza de usuario (clearUser)
- ✅ Modelo UserProfile completo
- ✅ Validación de campos (email, teléfono, fecha)
- ✅ AccountState enum
- ✅ Favoritos (promociones y colaboradores)
- ✅ Categorías de interés
- ✅ Casos límite (strings vacíos, largos, caracteres especiales)
- ✅ Concurrencia
- ✅ Integración de estados

#### CollabViewModelTest (30 pruebas)
- ✅ Estado inicial
- ✅ Modelo Collaborator completo
- ✅ Validación de datos (RFC, email, teléfono, código postal)
- ✅ CollaboratorsState enum
- ✅ Categorías múltiples
- ✅ Metadatos (logo, descripción, fecha)
- ✅ Casos límite
- ✅ Validación de negocio
- ✅ Performance

### Pruebas de UI Incluidas

#### AuthFlowUITest (20 pruebas)
- ✅ Pantalla de Login (elementos, campos, validación)
- ✅ Navegación (Registro, Recuperación)
- ✅ Validación de formularios
- ✅ Pantalla de Registro
- ✅ Confirmación OTP
- ✅ Recuperación de contraseña
- ✅ Accesibilidad
- ✅ Performance

#### PromotionsFlowUITest (25 pruebas)
- ✅ Home Screen (listado, búsqueda)
- ✅ Sistema de favoritos (toggle, persistencia)
- ✅ Detalle de promoción
- ✅ Código QR
- ✅ Filtrado por categorías
- ✅ Scroll y paginación
- ✅ Estados vacíos
- ✅ Creación de promociones (colaborador)
- ✅ Performance
- ✅ Interacciones múltiples

#### ProfileFlowUITest (20 pruebas)
- ✅ Visualización de perfil
- ✅ Edición de perfil
- ✅ Cambio de foto
- ✅ Categorías de interés
- ✅ Configuración
- ✅ Cerrar sesión
- ✅ Validación de formularios
- ✅ Navegación
- ✅ Estados de carga
- ✅ Accesibilidad
- ✅ Performance

---

## Configuración de Dependencias

Las siguientes dependencias se han agregado a `build.gradle.kts`:

```kotlin
// Testing de ViewModels y Coroutines
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
testImplementation("androidx.arch.core:core-testing:2.2.0")
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("app.cash.turbine:turbine:1.0.0")

// Testing de Compose UI
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")
debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")

// Espresso adicional
androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")

// Testing de Room
testImplementation("androidx.room:room-testing:2.6.1")

// Testing con Retrofit
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
```

---

## Mejores Prácticas

### 1. Nomenclatura de Pruebas

Usar formato descriptivo:
```kotlin
@Test
fun `functionName_scenario_expectedResult`() {
    // Arrange
    // Act
    // Assert
}
```

Ejemplo:
```kotlin
@Test
fun `loginScreen_invalidEmail_showsError`() { ... }
```

### 2. Estructura AAA (Arrange-Act-Assert)

```kotlin
@Test
fun `example_test`() {
    // Arrange - Preparar el estado
    val viewModel = AuthViewModel()
    val testEmail = "test@example.com"

    // Act - Ejecutar la acción
    viewModel.signUp(testEmail, "password123")

    // Assert - Verificar el resultado
    val state = viewModel.authState.first()
    assertTrue(state.needsConfirmation)
}
```

### 3. Testing de Coroutines

Usar `runTest` y `advanceUntilIdle`:

```kotlin
@Test
fun `coroutine_test`() = runTest {
    viewModel.loadData()
    advanceUntilIdle()

    val result = viewModel.data.first()
    assertNotNull(result)
}
```

### 4. Testing de Flows

Usar Turbine para testing de StateFlow:

```kotlin
@Test
fun `stateflow_test`() = runTest {
    viewModel.state.test {
        assertEquals(InitialState, awaitItem())

        viewModel.updateState()

        assertEquals(UpdatedState, awaitItem())
    }
}
```

### 5. Testing de UI con Compose

Usar semantic matchers:

```kotlin
@Test
fun `compose_ui_test`() {
    composeTestRule.setContent {
        MyScreen()
    }

    composeTestRule.onNodeWithText("Button")
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithText("Result")
        .assertExists()
}
```

### 6. Manejo de Estados Asíncronos

Siempre usar `waitForIdle()` en pruebas de UI:

```kotlin
@Test
fun `async_ui_test`() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Button")
        .performClick()

    composeTestRule.waitForIdle()

    // Verificar resultados
}
```

---

## Requisitos para Pruebas de UI

### 1. Emulador/Dispositivo
- API Level 26+
- Conexión a internet (para pruebas con backend)
- Permisos habilitados (Cámara, Almacenamiento)

### 2. Configuración de AWS Amplify
- Credenciales válidas en `amplifyconfiguration.json`
- Backend configurado y accesible

### 3. Datos de Prueba
- Crear cuentas de prueba en Cognito
- Tener promociones en el backend
- Configurar colaboradores de prueba

---

## Troubleshooting

### Problema: Las pruebas unitarias fallan con "Main dispatcher not set"

**Solución:**
Asegúrate de tener en tu clase de prueba:

```kotlin
@Before
fun setup() {
    Dispatchers.setMain(testDispatcher)
}

@After
fun tearDown() {
    Dispatchers.resetMain()
}
```

### Problema: Las pruebas de UI no encuentran elementos

**Solución:**
1. Usa `useUnmergedTree = true` en tus matchers
2. Espera con `waitForIdle()` antes de buscar elementos
3. Verifica que los elementos tengan semantic properties

### Problema: Timeouts en pruebas de UI

**Solución:**
1. Aumenta el timeout en `composeTestRule`
2. Verifica conexión a internet
3. Usa mocks para servicios externos

### Problema: Las pruebas de coroutines no terminan

**Solución:**
Usa `runTest` en lugar de `runBlocking`:

```kotlin
@Test
fun test() = runTest {
    // código asíncrono
}
```

---

## Continuous Integration

### GitHub Actions

Ejemplo de workflow para CI:

```yaml
name: Android Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'

      - name: Run Unit Tests
        run: ./gradlew test

      - name: Upload Test Results
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: app/build/test-results/
```

---

## Recursos Adicionales

- [Android Testing Documentation](https://developer.android.com/training/testing)
- [Compose Testing Cheat Sheet](https://developer.android.com/jetpack/compose/testing-cheatsheet)
- [Kotlinx Coroutines Test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/)
- [MockK Documentation](https://mockk.io/)
- [Espresso Documentation](https://developer.android.com/training/testing/espresso)

---

## Contacto y Contribuciones

Para reportar problemas con las pruebas o sugerir mejoras:
1. Abrir un issue en el repositorio
2. Documentar el caso de prueba fallido
3. Incluir logs y capturas de pantalla

**Nota:** Antes de hacer un PR, asegúrate de que todas las pruebas pasen:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

**Última actualización:** Enero 2025
**Versión:** 1.0
**Total de pruebas:** 165+
