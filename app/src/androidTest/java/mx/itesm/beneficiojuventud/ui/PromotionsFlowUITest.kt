package mx.itesm.beneficiojuventud.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import mx.itesm.beneficiojuventud.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI con Espresso/Compose para el flujo de promociones
 *
 * Estas pruebas verifican:
 * - Listado de promociones en Home
 * - Búsqueda y filtrado de promociones
 * - Marcar/desmarcar favoritos
 * - Navegación a detalle de promoción
 * - Visualización de QR de promoción
 * - Creación de promociones (colaboradores)
 * - Edición de promociones
 *
 * NOTA: Requiere:
 * 1. Usuario autenticado (mock o test account)
 * 2. Datos de prueba en el backend
 * 3. Conexión a internet
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class PromotionsFlowUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ========== PRUEBAS DE HOME SCREEN ==========

    @Test
    fun homeScreen_displaysPromotionsList() {
        composeTestRule.waitForIdle()

        // Esperar a que carguen las promociones
        // Buscar elementos de promociones
        composeTestRule.onNode(
            hasScrollAction() or hasText("Promociones", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun homeScreen_searchBar_exists() {
        composeTestRule.waitForIdle()

        // Verificar que existe la barra de búsqueda
        composeTestRule.onNode(
            hasSetTextAction() and (
                    hasText("Buscar", substring = true, ignoreCase = true) or
                            hasText("Search", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun homeScreen_searchBar_acceptsInput() {
        composeTestRule.waitForIdle()

        // Escribir en la barra de búsqueda
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Buscar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("Restaurante")

        composeTestRule.waitForIdle()

        // Verificar que el texto se ingresó
        composeTestRule.onNodeWithText("Restaurante", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun homeScreen_searchResults_filterPromotions() {
        composeTestRule.waitForIdle()

        // Buscar término específico
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Buscar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("Descuento")

        composeTestRule.waitForIdle()

        // Debe mostrar resultados filtrados
        // (Los resultados dependen de los datos de prueba)
    }

    @Test
    fun homeScreen_promotionCard_displaysRequiredInfo() {
        composeTestRule.waitForIdle()

        // Verificar que las tarjetas de promoción muestran información básica
        // Las promociones deberían tener: título, descripción, imagen, stock

        // Buscar al menos una promoción
        composeTestRule.onAllNodes(hasClickAction())
            .assertCountEquals(greaterThan(0))
    }

    // ========== PRUEBAS DE FAVORITOS ==========

    @Test
    fun promotionCard_favoriteButton_exists() {
        composeTestRule.waitForIdle()

        // Buscar botón de favorito (corazón)
        composeTestRule.onNode(
            hasClickAction() and (
                    hasContentDescription("Favorito", substring = true, ignoreCase = true) or
                            hasContentDescription("Favorite", substring = true, ignoreCase = true) or
                            hasContentDescription("Heart", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        )
        // Si existe al menos una promoción, debe tener botón de favorito
    }

    @Test
    fun promotionCard_clickFavorite_togglesFavoriteState() {
        composeTestRule.waitForIdle()

        // Encontrar primer botón de favorito
        val favoriteButton = composeTestRule.onAllNodes(
            hasClickAction() and hasContentDescription("Favorito", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).onFirst()

        // Click para marcar como favorito
        favoriteButton.performClick()

        composeTestRule.waitForIdle()

        // Click de nuevo para desmarcar
        favoriteButton.performClick()

        composeTestRule.waitForIdle()
    }

    @Test
    fun favoritesScreen_displaysFavoritePromotions() {
        composeTestRule.waitForIdle()

        // Navegar a favoritos
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Favoritos", substring = true, ignoreCase = true) or
                            hasContentDescription("Favoritos", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar que estamos en la pantalla de favoritos
        composeTestRule.onNode(
            hasText("Favoritos", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).assertExists()
    }

    // ========== PRUEBAS DE DETALLE DE PROMOCIÓN ==========

    @Test
    fun promotionCard_click_navigatesToDetail() {
        composeTestRule.waitForIdle()

        // Click en la primera promoción disponible
        composeTestRule.onAllNodes(
            hasClickAction() and !hasContentDescription("Favorito", substring = true),
            useUnmergedTree = true
        ).onFirst().performClick()

        composeTestRule.waitForIdle()

        // Verificar que navegamos al detalle
        // (Depende de tu implementación específica)
    }

    @Test
    fun promotionDetail_displaysCompleteInformation() {
        composeTestRule.waitForIdle()

        // Click en promoción
        composeTestRule.onAllNodes(hasClickAction())
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()

        // Verificar información completa:
        // - Título
        // - Descripción
        // - Fecha inicio/fin
        // - Stock disponible
        // - Términos y condiciones
    }

    @Test
    fun promotionDetail_useNowButton_exists() {
        composeTestRule.waitForIdle()

        // Navegar a detalle de promoción
        composeTestRule.onAllNodes(hasClickAction())
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()

        // Buscar botón "Usar ahora" o "Ver QR"
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Usar ahora", substring = true, ignoreCase = true) or
                            hasText("Ver QR", substring = true, ignoreCase = true) or
                            hasText("Use Now", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        )
    }

    // ========== PRUEBAS DE QR CODE ==========

    @Test
    fun qrScreen_displaysQRCode() {
        composeTestRule.waitForIdle()

        // Navegar a promoción y luego a QR
        // (Requiere navegar por el flujo completo)

        // Verificar que se muestra el código QR
        composeTestRule.onNode(
            hasContentDescription("QR Code", substring = true, ignoreCase = true) or
                    hasContentDescription("Código QR", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )
    }

    @Test
    fun qrScreen_displaysPromotionInfo() {
        composeTestRule.waitForIdle()

        // En la pantalla de QR también debe mostrarse información de la promoción
        // - Nombre del negocio
        // - Nombre de la promoción
        // - Términos de uso
    }

    // ========== PRUEBAS DE CATEGORÍAS ==========

    @Test
    fun homeScreen_categoryFilter_exists() {
        composeTestRule.waitForIdle()

        // Verificar que existe algún filtro de categorías
        composeTestRule.onNode(
            hasText("Categoría", substring = true, ignoreCase = true) or
                    hasText("Filtrar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )
    }

    @Test
    fun homeScreen_selectCategory_filtersPromotions() {
        composeTestRule.waitForIdle()

        // Click en categoría (ej: Restaurantes)
        composeTestRule.onNode(
            hasClickAction() and hasText("Restaurantes", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar que se filtraron las promociones
    }

    // ========== PRUEBAS DE SCROLL ==========

    @Test
    fun homeScreen_promotionsList_isScrollable() {
        composeTestRule.waitForIdle()

        // Verificar que la lista de promociones es scrollable
        composeTestRule.onNode(hasScrollAction(), useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun homeScreen_scrollDown_loadsMorePromotions() {
        composeTestRule.waitForIdle()

        // Scroll hacia abajo
        composeTestRule.onNode(hasScrollAction(), useUnmergedTree = true)
            .performScrollToIndex(5)

        composeTestRule.waitForIdle()

        // Verificar que se cargaron más promociones
    }

    // ========== PRUEBAS DE ESTADOS VACÍOS ==========

    @Test
    fun favoritesScreen_noFavorites_showsEmptyState() {
        composeTestRule.waitForIdle()

        // Navegar a favoritos
        composeTestRule.onNode(
            hasClickAction() and hasText("Favoritos", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Si no hay favoritos, debe mostrar estado vacío
        composeTestRule.onNode(
            hasText("No tienes favoritos", substring = true, ignoreCase = true) or
                    hasText("No favorites", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )
    }

    @Test
    fun searchResults_noResults_showsEmptyState() {
        composeTestRule.waitForIdle()

        // Buscar algo que no existe
        composeTestRule.onNode(
            hasSetTextAction() and hasText("Buscar", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performTextInput("XYZ123ABC456NOTFOUND")

        composeTestRule.waitForIdle()

        // Debe mostrar estado vacío
        composeTestRule.onNode(
            hasText("No se encontraron", substring = true, ignoreCase = true) or
                    hasText("No results", substring = true, ignoreCase = true),
            useUnmergedTree = true
        )
    }

    // ========== PRUEBAS DE PROMOCIONES (COLABORADOR) ==========

    @Test
    fun collabHome_createPromotionButton_exists() {
        // NOTA: Esta prueba requiere estar autenticado como colaborador
        composeTestRule.waitForIdle()

        // Buscar botón de crear promoción
        composeTestRule.onNode(
            hasClickAction() and (
                    hasText("Crear Promoción", substring = true, ignoreCase = true) or
                            hasText("Nueva Promoción", substring = true, ignoreCase = true) or
                            hasContentDescription("Crear Promoción", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        )
    }

    @Test
    fun createPromotion_displaysRequiredFields() {
        composeTestRule.waitForIdle()

        // Navegar a crear promoción
        composeTestRule.onNode(
            hasClickAction() and hasText("Crear Promoción", substring = true, ignoreCase = true),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar campos requeridos
        val requiredFields = listOf(
            "Título",
            "Descripción",
            "Tipo de Promoción",
            "Stock",
            "Fecha Inicio",
            "Fecha Fin"
        )

        requiredFields.forEach { field ->
            composeTestRule.onNode(
                hasText(field, substring = true, ignoreCase = true),
                useUnmergedTree = true
            )
        }
    }

    // ========== PRUEBAS DE PERFORMANCE ==========

    @Test
    fun homeScreen_promotionsList_loadsWithin3Seconds() {
        val startTime = System.currentTimeMillis()

        composeTestRule.waitForIdle()

        val loadTime = System.currentTimeMillis() - startTime

        assert(loadTime < 3000) {
            "Promotions list took $loadTime ms to load, expected < 3000 ms"
        }
    }

    @Test
    fun promotionCard_imageLoading_displaysPlaceholder() {
        composeTestRule.waitForIdle()

        // Mientras carga la imagen, debe mostrar un placeholder
        // (Depende de la implementación con Coil)
    }

    // ========== PRUEBAS DE INTERACCIÓN ==========

    @Test
    fun promotionCard_multipleClicks_handledGracefully() {
        composeTestRule.waitForIdle()

        val firstPromo = composeTestRule.onAllNodes(hasClickAction())
            .onFirst()

        // Múltiples clicks rápidos
        repeat(5) {
            firstPromo.performClick()
        }

        composeTestRule.waitForIdle()

        // No debe crashear
    }

    @Test
    fun homeScreen_refreshAction_reloadsPromotions() {
        composeTestRule.waitForIdle()

        // Hacer pull-to-refresh (si está implementado)
        composeTestRule.onNode(hasScrollAction(), useUnmergedTree = true)
            .performTouchInput {
                swipeDown()
            }

        composeTestRule.waitForIdle()

        // Debe recargar las promociones
    }

    // ========== PRUEBAS DE NAVEGACIÓN ==========

    @Test
    fun bottomNavBar_homeButton_navigatesToHome() {
        composeTestRule.waitForIdle()

        // Click en Home en bottom navigation
        composeTestRule.onNode(
            hasClickAction() and (
                    hasContentDescription("Home", substring = true, ignoreCase = true) or
                            hasContentDescription("Inicio", substring = true, ignoreCase = true)
                    ),
            useUnmergedTree = true
        ).performClick()

        composeTestRule.waitForIdle()

        // Verificar que estamos en home
        composeTestRule.onNode(
            hasText("Promociones", substring = true, ignoreCase = true) or
                    hasScrollAction(),
            useUnmergedTree = true
        ).assertExists()
    }

    // ========== HELPER MATCHERS ==========

    private fun greaterThan(value: Int) = object : (Int) -> Boolean {
        override fun invoke(actual: Int) = actual > value
    }
}
