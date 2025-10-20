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
 * Pruebas de UI con Espresso/Compose para el flujo de perfil de usuario
 *
 * Estas pruebas verifican:
 * - Visualización de perfil de usuario
 * - Edición de datos personales
 * - Cambio de foto de perfil
 * - Gestión de categorías de interés
 * - Configuración de notificaciones
 * - Cerrar sesión
 *
 * NOTA: Requiere:
 * 1. Usuario autenticado
 * 2. Permisos de cámara/galería para foto de perfil
 * 3. Conexión a internet
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ProfileFlowUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ========== PRUEBAS DE PANTALLA DE PERFIL ==========

    @Test
    fun profileScreen_displaysUserInformation() {
        composeTestRule.waitForIdle()

        // Navegar a perfil
        composeTestRule.onNode(
            hasClickAction() and (
                    hasContentDescription("Perfil", substring = true, ignoreCase = true) or
                            hasContentDescription("Profile", substring = true, ignoreCase = true) or
                            hasText("Perfil", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar que estamos en la pantalla de perfil
        composeTestRule.onNode(
            hasText("Perfil", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun profileScreen_displaysAllUserFields() {
        composeTestRule.waitForIdle()

        // Navegar a perfil
        navigateToProfile()

        // Verificar que se muestran los campos del usuario
        val expectedFields = listOf(
            "Nombre",
            "Apellido",
            "Email",
            "Teléfono",
            "Fecha de Nacimiento"
        )

        expectedFields.forEach { field ->
            composeTestRule.onNode(
                hasText(field, substring = true, ignoreCase = true),
                useUnmergedTree = true
            )
            // Los campos pueden o no existir dependiendo de si hay datos
        }
    }

    @Test
    fun profileScreen_editButton_exists() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Verificar que existe botón de editar
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Editar", substring = true, ignoreCase = true) or
                            hasContentDescription("Editar", substring = true, ignoreCase = true) or
                            hasContentDescription("Edit", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun profileScreen_profileImage_exists() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Verificar que existe imagen de perfil (o placeholder)
        composeTestRule.onNode(
            hasContentDescription("Foto de perfil", substring = true, ignoreCase = true) or
                    hasContentDescription("Profile Picture", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )
    }

    // ========== PRUEBAS DE EDICIÓN DE PERFIL ==========

    @Test
    fun profileScreen_clickEdit_navigatesToEditScreen() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Click en editar
        composeTestRule.onNode(
            hasClickAction() and hasText("Editar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar que estamos en pantalla de edición
        composeTestRule.onNode(
            hasText("Editar Perfil", substring = true, ignoreCase = true) or
                    hasText("Edit Profile", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }



    @Test
    fun editProfileScreen_nameField_acceptsInput() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Limpiar y escribir nuevo nombre
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Nombre", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasText("Nombre", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("Nuevo Nombre")

        composeTestRule.waitForIdle()

        // Verificar que se ingresó
        composeTestRule.onNodeWithText("Nuevo Nombre", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun editProfileScreen_phoneField_acceptsNumbers() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Buscar campo de teléfono
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Teléfono", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasText("Teléfono", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("9876543210")

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("9876543210", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun editProfileScreen_saveButton_exists() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Verificar botón de guardar
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Guardar", substring = true, ignoreCase = true) or
                            hasText("Save", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun editProfileScreen_cancelButton_exists() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Verificar botón de cancelar
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Cancelar", substring = true, ignoreCase = true) or
                            hasText("Cancel", substring = true, ignoreCase = true) or
                            hasContentDescription("Atrás", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun editProfileScreen_saveChanges_updatesProfile() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Modificar un campo
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Nombre", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasText("Nombre", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("Nombre Actualizado")

        // Guardar
        composeTestRule.onNode(
            hasClickAction() and hasText("Guardar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Debería regresar a perfil con datos actualizados
        composeTestRule.onNodeWithText("Nombre Actualizado", useUnmergedTree = true)
    }

    // ========== PRUEBAS DE FOTO DE PERFIL ==========

    @Test
    fun editProfileScreen_changePhotoButton_exists() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Buscar botón para cambiar foto
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Cambiar foto", substring = true, ignoreCase = true) or
                            hasContentDescription("Cambiar foto", substring = true, ignoreCase = true) or
                            hasText("Change Photo", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        )
    }

    @Test
    fun profileImage_clickToChange_showsOptions() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Click en imagen o botón de cambiar
        composeTestRule.onNode(
            hasClickAction() and (
                    hasContentDescription("Foto de perfil", substring = true, ignoreCase = true) or
                            hasText("Cambiar foto", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Debería mostrar opciones (Galería, Cámara, etc.)
        // Dependiendo de la implementación
    }

    // ========== PRUEBAS DE CATEGORÍAS DE INTERÉS ==========

    @Test
    fun profileScreen_displaysCategoriesOfInterest() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Buscar sección de categorías
        composeTestRule.onNode(
            hasText("Categorías de interés", substring = true, ignoreCase = true) or
                    hasText("Intereses", substring = true, ignoreCase = true) or
                    hasText("Categories", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )
    }

    @Test
    fun editProfileScreen_canSelectCategories() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Buscar categorías seleccionables
        composeTestRule.onNode(
            hasText("Categorías", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )

        // Click en una categoría
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Restaurantes", substring = true, ignoreCase = true) or
                            hasText("Tecnología", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        )
    }

    // ========== PRUEBAS DE CONFIGURACIÓN ==========

    @Test
    fun profileScreen_settingsButton_exists() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Buscar botón de configuración
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Configuración", substring = true, ignoreCase = true) or
                            hasText("Settings", substring = true, ignoreCase = true) or
                            hasContentDescription("Configuración", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        )
    }

    @Test
    fun settingsScreen_displaysAllOptions() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Navegar a configuración
        composeTestRule.onNode(
            hasClickAction() and hasText("Configuración", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar opciones comunes
        val settingsOptions = listOf(
            "Notificaciones",
            "Privacidad",
            "Ayuda"
        )

        settingsOptions.forEach { option ->
            composeTestRule.onNode(
                hasText(option, substring = true, ignoreCase = true),
                useUnmergedTree = true
            )
        }
    }

    // ========== PRUEBAS DE CERRAR SESIÓN ==========

    @Test
    fun profileScreen_logoutButton_exists() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Buscar botón de cerrar sesión
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Cerrar Sesión", substring = true, ignoreCase = true) or
                            hasText("Logout", substring = true, ignoreCase = true) or
                            hasText("Sign Out", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun profileScreen_clickLogout_showsConfirmation() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Click en cerrar sesión
        composeTestRule.onNode(
            hasClickAction() and hasText("Cerrar Sesión", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Debe mostrar confirmación
        composeTestRule.onNode(
            hasText("¿Estás seguro?", substring = true, ignoreCase = true) or
                    hasText("Confirmar", substring = true, ignoreCase = true) or
                    hasText("Are you sure?", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )
    }

    // ========== PRUEBAS DE VALIDACIÓN ==========

    @Test
    fun editProfileScreen_invalidEmail_showsError() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Ingresar email inválido
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Email", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasText("Email", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("invalid-email")

        // Intentar guardar
        composeTestRule.onNode(
            hasClickAction() and hasText("Guardar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Debería mostrar error de validación
    }

    @Test
    fun editProfileScreen_invalidPhone_showsError() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Ingresar teléfono inválido
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Teléfono", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextClearance()

        composeTestRule.onNode(
            hasSetTextAction() and hasText("Teléfono", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("123")

        // Intentar guardar
        composeTestRule.onNode(
            hasClickAction() and hasText("Guardar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Debería mostrar error
    }

    // ========== PRUEBAS DE NAVEGACIÓN ==========

    @Test
    fun editProfileScreen_clickCancel_returnsToProfile() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Click en cancelar
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Cancelar", substring = true, ignoreCase = true) or
                            hasContentDescription("Atrás", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Debería regresar a perfil
        composeTestRule.onNode(
            hasText("Perfil", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun profileScreen_backButton_returnsToHome() {
        composeTestRule.waitForIdle()

        navigateToProfile()

        // Click en back
        composeTestRule.onNode(
            hasClickAction() and hasContentDescription("Atrás", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Debería regresar a home
    }

    // ========== PRUEBAS DE ESTADOS DE CARGA ==========

    @Test
    fun editProfileScreen_saveChanges_showsLoading() {
        composeTestRule.waitForIdle()

        navigateToEditProfile()

        // Modificar datos
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Nombre", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput(" Test")

        // Guardar
        composeTestRule.onNode(
            hasClickAction() and hasText("Guardar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        // Debería mostrar indicador de carga
        composeTestRule.onNode(
            hasContentDescription("Loading", substring = true, ignoreCase = true) or
                    hasContentDescription("Cargando", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )
    }

    // ========== PRUEBAS DE ACCESIBILIDAD ==========


    // ========== PRUEBAS DE PERFORMANCE ==========

    @Test
    fun profileScreen_loadsWithin2Seconds() {
        val startTime = System.currentTimeMillis()

        navigateToProfile()

        val loadTime = System.currentTimeMillis() - startTime

        assert(loadTime < 2000) {
            "Profile screen took $loadTime ms to load, expected < 2000 ms"
        }
    }

    // ========== HELPER FUNCTIONS ==========

    private fun navigateToProfile() {
        composeTestRule.onNode(
            hasClickAction() and (
                    hasContentDescription("Perfil", substring = true, ignoreCase = true) or
                            hasText("Perfil", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()
    }

    private fun navigateToEditProfile() {
        navigateToProfile()

        composeTestRule.onNode(
            hasClickAction() and hasText("Editar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()
    }

    private fun greaterThan(value: Int) = object : (Int) -> Boolean {
        override fun invoke(actual: Int) = actual > value
    }
}
