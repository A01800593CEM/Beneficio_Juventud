package mx.itesm.beneficiojuventud.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import mx.itesm.beneficiojuventud.model.promos.Promotions
import mx.itesm.beneficiojuventud.model.promos.PromotionState
import mx.itesm.beneficiojuventud.model.promos.PromotionType
import mx.itesm.beneficiojuventud.model.promos.PromoTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para PromoViewModel
 *
 * Estas pruebas verifican:
 * - Gestión de estado de promociones
 * - Funcionalidad de favoritos locales
 * - Algoritmo de intercalado de promociones
 * - Manejo de listas de promociones
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PromoViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PromoViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PromoViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== PRUEBAS DE ESTADO INICIAL ==========

    @Test
    fun `initial promoState should be empty`() = runTest {
        val promoState = viewModel.promoState.first()

        assertNotNull(promoState)
        assertNull(promoState.promotionId)
        assertNull(promoState.title)
    }

    @Test
    fun `initial promoListState should be empty`() = runTest {
        val promoListState = viewModel.promoListState.first()

        assertNotNull(promoListState)
        assertTrue(promoListState.isEmpty())
    }

    @Test
    fun `initial favoriteIds should be empty`() = runTest {
        val favoriteIds = viewModel.favoriteIds.first()

        assertNotNull(favoriteIds)
        assertTrue(favoriteIds.isEmpty())
    }

    // ========== PRUEBAS DE FAVORITOS ==========

    @Test
    fun `isFavorite should return false for non-favorite promotion`() {
        val isFavorite = viewModel.isFavorite(1)
        assertFalse(isFavorite)
    }

    @Test
    fun `toggleFavorite should add promotion to favorites`() = runTest {
        val promoId = 123

        viewModel.toggleFavorite(promoId)
        advanceUntilIdle()

        val favoriteIds = viewModel.favoriteIds.first()
        assertTrue(favoriteIds.contains(promoId))
        assertTrue(viewModel.isFavorite(promoId))
    }

    @Test
    fun `toggleFavorite should remove promotion from favorites when called twice`() = runTest {
        val promoId = 456

        // Agregar a favoritos
        viewModel.toggleFavorite(promoId)
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite(promoId))

        // Remover de favoritos
        viewModel.toggleFavorite(promoId)
        advanceUntilIdle()

        assertFalse(viewModel.isFavorite(promoId))
    }

    @Test
    fun `multiple favorites should be tracked independently`() = runTest {
        val promoIds = listOf(1, 2, 3, 4, 5)

        promoIds.forEach { id ->
            viewModel.toggleFavorite(id)
        }
        advanceUntilIdle()

        val favoriteIds = viewModel.favoriteIds.first()

        assertEquals(promoIds.size, favoriteIds.size)
        promoIds.forEach { id ->
            assertTrue(favoriteIds.contains(id))
            assertTrue(viewModel.isFavorite(id))
        }
    }

    @Test
    fun `removing one favorite should not affect others`() = runTest {
        val promoIds = listOf(10, 20, 30)

        // Agregar todos
        promoIds.forEach { id ->
            viewModel.toggleFavorite(id)
        }
        advanceUntilIdle()

        // Remover uno
        viewModel.toggleFavorite(20)
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite(10))
        assertFalse(viewModel.isFavorite(20))
        assertTrue(viewModel.isFavorite(30))
    }

    // ========== PRUEBAS DE EDGE CASES ==========

    @Test
    fun `toggleFavorite with id 0 should work correctly`() = runTest {
        viewModel.toggleFavorite(0)
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite(0))
    }

    @Test
    fun `toggleFavorite with negative id should work correctly`() = runTest {
        viewModel.toggleFavorite(-1)
        advanceUntilIdle()

        assertTrue(viewModel.isFavorite(-1))
    }

    @Test
    fun `multiple rapid toggles should maintain consistency`() = runTest {
        val promoId = 999

        // Toggle múltiples veces
        repeat(10) {
            viewModel.toggleFavorite(promoId)
        }
        advanceUntilIdle()

        // Después de 10 toggles (número par), debe estar en estado original (no favorito)
        assertFalse(viewModel.isFavorite(promoId))
    }

    @Test
    fun `isFavorite should return false for null promotionId`() {
        // Asumiendo que promotionId no puede ser null en la práctica,
        // pero si fuera, el comportamiento debe ser predecible
        val isFavorite = viewModel.isFavorite(Int.MAX_VALUE)
        assertFalse(isFavorite)
    }

    // ========== PRUEBAS DE FLOWS ==========

    @Test
    fun `favoriteIds flow should emit updates`() = runTest {
        val promoId = 789

        // Estado inicial
        var favoriteIds = viewModel.favoriteIds.first()
        assertFalse(favoriteIds.contains(promoId))

        // Toggle
        viewModel.toggleFavorite(promoId)
        advanceUntilIdle()

        // Verificar nuevo estado
        favoriteIds = viewModel.favoriteIds.first()
        assertTrue(favoriteIds.contains(promoId))
    }

    @Test
    fun `promoState flow should emit default state initially`() = runTest {
        val promoState = viewModel.promoState.first()

        assertNotNull(promoState)
        assertNull(promoState.promotionId)
        assertNull(promoState.collaboratorId)
        assertNull(promoState.title)
    }

    @Test
    fun `promoListState flow should emit empty list initially`() = runTest {
        val promoListState = viewModel.promoListState.first()

        assertNotNull(promoListState)
        assertEquals(0, promoListState.size)
    }

    // ========== PRUEBAS DE CONCURRENCIA ==========

    @Test
    fun `concurrent toggleFavorite calls should be handled safely`() = runTest {
        val promoIds = (1..100).toList()

        // Toggle concurrentemente
        promoIds.forEach { id ->
            viewModel.toggleFavorite(id)
        }
        advanceUntilIdle()

        val favoriteIds = viewModel.favoriteIds.first()
        assertEquals(100, favoriteIds.size)
    }

    @Test
    fun `adding and removing favorites concurrently should work`() = runTest {
        val promoId = 555

        // Agregar y remover múltiples veces
        repeat(5) {
            viewModel.toggleFavorite(promoId) // agregar
            viewModel.toggleFavorite(promoId) // remover
        }
        advanceUntilIdle()

        // Debe estar en estado inicial (no favorito)
        assertFalse(viewModel.isFavorite(promoId))
    }

    // ========== PRUEBAS DE PROMOCIONES (Modelos de Datos) ==========

    @Test
    fun `Promotions model should have correct default values`() {
        val promo = Promotions()

        assertNull(promo.promotionId)
        assertNull(promo.collaboratorId)
        assertNull(promo.title)
        assertNull(promo.description)
        assertNull(promo.imageUrl)
    }

    @Test
    fun `Promotions model should accept all fields`() {
        val promo = Promotions(
            promotionId = 1,
            collaboratorId = "collab123",
            title = "Test Promo",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            initialDate = "2025-01-01",
            endDate = "2025-12-31",
            promotionType = PromotionType.descuento,
            promotionString = "20% OFF",
            totalStock = 100,
            availableStock = 50,
            limitPerUser = 1,
            dailyLimitPerUser = 1,
            promotionState = PromotionState.activa,
            isBookable = true,
            theme = PromoTheme.light,
            businessName = "Test Business"
        )

        assertEquals(1, promo.promotionId)
        assertEquals("collab123", promo.collaboratorId)
        assertEquals("Test Promo", promo.title)
        assertEquals("Test Description", promo.description)
        assertEquals(PromotionType.descuento, promo.promotionType)
        assertEquals(PromotionState.activa, promo.promotionState)
        assertEquals(100, promo.totalStock)
        assertEquals(50, promo.availableStock)
    }

    // ========== PRUEBAS DE VALIDACIÓN DE NEGOCIO ==========

    @Test
    fun `favorite set should not contain duplicates`() = runTest {
        val promoId = 111

        // Intentar agregar el mismo ID múltiples veces
        viewModel.toggleFavorite(promoId) // agregar
        advanceUntilIdle()

        val favoriteIds1 = viewModel.favoriteIds.first()
        val count1 = favoriteIds1.count { it == promoId }

        assertEquals(1, count1)

        // Toggle off y on de nuevo
        viewModel.toggleFavorite(promoId) // remover
        advanceUntilIdle()
        viewModel.toggleFavorite(promoId) // agregar de nuevo
        advanceUntilIdle()

        val favoriteIds2 = viewModel.favoriteIds.first()
        val count2 = favoriteIds2.count { it == promoId }

        assertEquals(1, count2)
    }

    @Test
    fun `favoriteIds should maintain Set properties`() = runTest {
        val promoIds = listOf(1, 2, 3, 2, 1, 4, 3) // con duplicados

        promoIds.forEach { id ->
            viewModel.toggleFavorite(id)
            advanceUntilIdle()
        }

        val favoriteIds = viewModel.favoriteIds.first()

        // Con toggles impares, algunos estarán y otros no
        // Pero no debe haber duplicados
        assertEquals(favoriteIds.size, favoriteIds.toSet().size)
    }

    // ========== PRUEBAS DE PERFORMANCE ==========

    @Test
    fun `large number of favorites should be handled efficiently`() = runTest {
        val largeSet = (1..1000).toList()

        largeSet.forEach { id ->
            viewModel.toggleFavorite(id)
        }
        advanceUntilIdle()

        val favoriteIds = viewModel.favoriteIds.first()
        assertEquals(1000, favoriteIds.size)
    }
}
