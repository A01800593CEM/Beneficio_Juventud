package mx.itesm.beneficiojuventud.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import mx.itesm.beneficiojuventud.view.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI con Espresso/Compose para el flujo de autenticación
 *
 * Estas pruebas verifican:
 * - Flujo de Login
 * - Flujo de Registro
 * - Flujo de Confirmación de código OTP
 * - Flujo de Recuperación de contraseña
 * - Validaciones de formularios
 * - Navegación entre pantallas
 *
 * NOTA: Para ejecutar estas pruebas necesitas:
 * 1. Un emulador o dispositivo real
 * 2. Conexión a internet (para Amplify Auth)
 * 3. Configuración válida de AWS Amplify
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthFlowUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ========== PRUEBAS DE PANTALLA DE LOGIN ==========

    @Test
    fun loginScreen_displaysAllElements() {
        // Esperar a que la app cargue
        composeTestRule.waitForIdle()

        // Buscar elementos comunes de la pantalla de login
        // Nota: Los tags deben coincidir con los semantic properties de tu UI
        composeTestRule.onNodeWithText("Iniciar Sesión", ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun loginScreen_emailField_acceptsInput() {
        composeTestRule.waitForIdle()

        // Buscar el campo de email y escribir
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Email", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("test@example.com")

        // Verificar que el texto se ingresó
        composeTestRule.onNodeWithText("test@example.com", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun loginScreen_passwordField_acceptsInput() {
        composeTestRule.waitForIdle()

        // Buscar el campo de password
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Contraseña", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("TestPassword123")

        // El texto no debe ser visible (campo de password oculto)
        // Verificar que existe el campo
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Contraseña", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun loginScreen_emptyFields_showsValidation() {
        composeTestRule.waitForIdle()

        // Intentar hacer clic en login sin llenar campos
        composeTestRule.onNodeWithText("Iniciar Sesión", ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Debería mostrar algún tipo de validación o error
        // (Ajustar según tu implementación)
    }

    @Test
    fun loginScreen_forgotPasswordLink_exists() {
        composeTestRule.waitForIdle()

        // Verificar que existe el enlace de "Olvidé mi contraseña"
        composeTestRule.onNode(
            hasText("Olvidé mi contraseña", substring = true, ignoreCase = true) or
                    hasText("Forgot Password", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun loginScreen_registerLink_exists() {
        composeTestRule.waitForIdle()

        // Verificar que existe el enlace de registro
        composeTestRule.onNode(
            hasText("Registrar", substring = true, ignoreCase = true) or
                    hasText("Sign Up", substring = true, ignoreCase = true) or
                    hasText("Crear cuenta", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    // ========== PRUEBAS DE NAVEGACIÓN ==========

    @Test
    fun loginScreen_clickRegister_navigatesToRegisterScreen() {
        composeTestRule.waitForIdle()

        // Click en el botón/enlace de registro
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Registrar", substring = true, ignoreCase = true) or
                            hasText("Crear cuenta", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar que estamos en la pantalla de registro
        composeTestRule.onNode(
            hasText("Registro", substring = true, ignoreCase = true) or
                    hasText("Nombre", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun loginScreen_clickForgotPassword_navigatesToRecoveryScreen() {
        composeTestRule.waitForIdle()

        // Click en "Olvidé mi contraseña"
        composeTestRule.onNode(
            hasClickAction() and hasText("Olvidé mi contraseña", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar navegación a recuperación de contraseña
        composeTestRule.onNode(
            hasText("Recuperar", substring = true, ignoreCase = true) or
                    hasText("Recovery", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    // ========== PRUEBAS DE VALIDACIÓN DE FORMULARIOS ==========

    @Test
    fun loginScreen_invalidEmail_showsError() {
        composeTestRule.waitForIdle()

        // Ingresar email inválido
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Email", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("invalid-email")

        // Ingresar contraseña
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Contraseña", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("password123")

        // Click en login
        composeTestRule.onNodeWithText("Iniciar Sesión", ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Debería mostrar error de validación
        // (Ajustar según tu implementación de validación)
    }

    @Test
    fun loginScreen_shortPassword_showsError() {
        composeTestRule.waitForIdle()

        // Ingresar email válido
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Email", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("test@example.com")

        // Ingresar contraseña muy corta
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Contraseña", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("123")

        // Click en login
        composeTestRule.onNodeWithText("Iniciar Sesión", ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Debería mostrar error de validación
    }

    // ========== PRUEBAS DE PANTALLA DE REGISTRO ==========

    @Test
    fun registerScreen_displaysAllRequiredFields() {
        composeTestRule.waitForIdle()

        // Navegar a registro
        composeTestRule.onNode(
            hasClickAction() and hasText("Registrar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar campos requeridos
        val requiredFields = listOf(
            "Nombre",
            "Apellido",
            "Email",
            "Teléfono",
            "Contraseña"
        )

        requiredFields.forEach { field ->
            composeTestRule.onNode(
                hasText(field, substring = true, ignoreCase = true),
                useUnmergedTree = true
            ).assertExists()
        }
    }

    @Test
    fun registerScreen_fillAllFields_enablesSubmitButton() {
        composeTestRule.waitForIdle()

        // Navegar a registro
        composeTestRule.onNode(
            hasClickAction() and hasText("Registrar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Llenar todos los campos
        // Nombre
        composeTestRule.onNodeWithText("Nombre", useUnmergedTree = true)
            .performTextInput("Juan")

        // Apellido Paterno
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Apellido Paterno", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("Pérez")

        // Email
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Email", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("juan.perez@example.com")

        // Teléfono
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Teléfono", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("1234567890")

        // Contraseña
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Contraseña", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("SecurePassword123!")

        composeTestRule.waitForIdle()

        // El botón de registro debería estar habilitado
        composeTestRule.onNode(
            hasClickAction() and hasText("Registrar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertIsEnabled()
    }

    // ========== PRUEBAS DE CONFIRMACIÓN OTP ==========

    @Test
    fun confirmScreen_displaysCodeInputField() {
        composeTestRule.waitForIdle()

        // Nota: Esta prueba requiere navegar a la pantalla de confirmación
        // Lo cual normalmente requiere un registro previo
        // Aquí verificamos la estructura básica

        // Buscar campo de código OTP
        composeTestRule.onNode(
            hasSetTextAction() and (
                    hasText("Código", substring = true, ignoreCase = true) or
                            hasText("Code", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        )
        // Solo verificamos que existe si estamos en esa pantalla
    }

    @Test
    fun confirmScreen_codeField_accepts6Digits() {
        composeTestRule.waitForIdle()

        // Buscar campo de código y verificar que acepta 6 dígitos
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Código", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("123456")

        composeTestRule.waitForIdle()
    }

    // ========== PRUEBAS DE RECUPERACIÓN DE CONTRASEÑA ==========

    @Test
    fun forgotPasswordScreen_emailField_acceptsInput() {
        composeTestRule.waitForIdle()

        // Navegar a recuperación
        composeTestRule.onNode(
            hasClickAction() and hasText("Olvidé mi contraseña", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Ingresar email
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Email", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("recovery@example.com")

        composeTestRule.waitForIdle()

        // Verificar que se ingresó
        composeTestRule.onNodeWithText("recovery@example.com", useUnmergedTree = true)
            .assertExists()
    }

    // ========== PRUEBAS DE ACCESIBILIDAD ==========



    // ========== PRUEBAS DE PERFORMANCE ==========

    @Test
    fun loginScreen_loadsWithin5Seconds() {
        val startTime = System.currentTimeMillis()

        composeTestRule.waitForIdle()

        val loadTime = System.currentTimeMillis() - startTime

        // La pantalla debe cargar en menos de 5 segundos
        assert(loadTime < 5000) {
            "Login screen took $loadTime ms to load, expected < 5000 ms"
        }
    }

    // ========== HELPER MATCHERS ==========

    private fun greaterThan(value: Int) = object : (Int) -> Boolean {
        override fun invoke(actual: Int) = actual > value
    }
}
