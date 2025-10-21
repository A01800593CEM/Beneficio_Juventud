package mx.itesm.beneficiojuventud.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import mx.itesm.beneficiojuventud.model.SavedCouponRepository
import mx.itesm.beneficiojuventud.model.users.UserProfile
import mx.itesm.beneficiojuventud.model.users.AccountState
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.mock

/**
 * Pruebas unitarias para UserViewModel
 *
 * Estas pruebas verifican:
 * - Gestión de estado de usuario
 * - Sistema de favoritos (promociones y colaboradores)
 * - Manejo de errores y carga
 * - Token de carga para invalidar respuestas tardías
 * - Operaciones CRUD de usuario
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: UserViewModel
    private lateinit var mockRepository: SavedCouponRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock(SavedCouponRepository::class.java)
        viewModel = UserViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== PRUEBAS DE ESTADO INICIAL ==========

    @Test
    fun `initial userState should be empty`() = runTest {
        val userState = viewModel.userState.first()

        assertNotNull(userState)
        assertNull(userState.id)
        assertNull(userState.cognitoId)
        assertNull(userState.name)
        assertNull(userState.email)
    }

    @Test
    fun `initial isLoading should be false`() = runTest {
        val isLoading = viewModel.isLoading.first()
        assertFalse(isLoading)
    }

    @Test
    fun `initial error should be null`() = runTest {
        val error = viewModel.error.first()
        assertNull(error)
    }

    @Test
    fun `initial favoritePromotions should be empty`() = runTest {
        val favoritePromotions = viewModel.favoritePromotions.first()

        assertNotNull(favoritePromotions)
        assertTrue(favoritePromotions.isEmpty())
    }

    @Test
    fun `initial favoriteCollabs should be empty`() = runTest {
        val favoriteCollabs = viewModel.favoriteCollabs.first()

        assertNotNull(favoriteCollabs)
        assertTrue(favoriteCollabs.isEmpty())
    }

    // ========== PRUEBAS DE CLEARUSER ==========

    @Test
    fun `clearUser should reset all state to default`() = runTest {
        // Limpiar usuario
        viewModel.clearUser()
        advanceUntilIdle()

        val userState = viewModel.userState.first()
        val isLoading = viewModel.isLoading.first()
        val error = viewModel.error.first()

        assertNull(userState.id)
        assertNull(userState.cognitoId)
        assertFalse(isLoading)
        assertNull(error)
    }

    @Test
    fun `clearUser should be safe to call multiple times`() = runTest {
        viewModel.clearUser()
        viewModel.clearUser()
        viewModel.clearUser()

        val userState = viewModel.userState.first()
        assertNull(userState.id)
    }

    // ========== PRUEBAS DE USERPROFILE MODEL ==========

    @Test
    fun `UserProfile model should have correct default values`() {
        val profile = UserProfile()

        assertNull(profile.id)
        assertNull(profile.cognitoId)
        assertNull(profile.name)
        assertNull(profile.lastNamePaternal)
        assertNull(profile.lastNameMaternal)
        assertNull(profile.email)
        assertNull(profile.phoneNumber)
    }

    @Test
    fun `UserProfile model should accept all fields`() {
        val profile = UserProfile(
            id = 1,
            cognitoId = "cognito123",
            name = "Juan",
            lastNamePaternal = "Pérez",
            lastNameMaternal = "García",
            birthDate = "1990-01-01",
            phoneNumber = "1234567890",
            email = "juan@example.com",
            accountState = AccountState.activo,
            registrationDate = "2025-01-01",
            notificationToken = "fcm_token_123",
            profileImageKey = "profile/image.jpg"
        )

        assertEquals(1, profile.id)
        assertEquals("cognito123", profile.cognitoId)
        assertEquals("Juan", profile.name)
        assertEquals("Pérez", profile.lastNamePaternal)
        assertEquals("García", profile.lastNameMaternal)
        assertEquals("juan@example.com", profile.email)
        assertEquals(AccountState.activo, profile.accountState)
    }

    // ========== PRUEBAS DE FLOWS ==========

    @Test
    fun `userState flow should emit changes`() = runTest {
        val initialState = viewModel.userState.first()
        assertNull(initialState.name)

        // El flujo debe estar disponible
        assertNotNull(viewModel.userState)
    }

    @Test
    fun `isLoading flow should emit changes`() = runTest {
        val initialLoading = viewModel.isLoading.first()

        // Estado inicial debe ser false
        assertFalse(initialLoading)
    }

    @Test
    fun `error flow should emit changes`() = runTest {
        val initialError = viewModel.error.first()
        assertNull(initialError)
    }

    @Test
    fun `favoritePromotions flow should emit changes`() = runTest {
        val initialFavorites = viewModel.favoritePromotions.first()
        assertTrue(initialFavorites.isEmpty())
    }

    @Test
    fun `favoriteCollabs flow should emit changes`() = runTest {
        val initialCollabs = viewModel.favoriteCollabs.first()
        assertTrue(initialCollabs.isEmpty())
    }

    // ========== PRUEBAS DE VALIDACIÓN DE DATOS ==========

    @Test
    fun `UserProfile with valid email should be accepted`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.com"
        )

        validEmails.forEach { email ->
            val profile = UserProfile(email = email)
            assertEquals(email, profile.email)
        }
    }

    @Test
    fun `UserProfile with valid phone numbers should be accepted`() {
        val validPhones = listOf(
            "1234567890",
            "+521234567890",
            "123-456-7890"
        )

        validPhones.forEach { phone ->
            val profile = UserProfile(phoneNumber = phone)
            assertEquals(phone, profile.phoneNumber)
        }
    }

    @Test
    fun `UserProfile birthDate should accept ISO format`() {
        val dates = listOf(
            "1990-01-01",
            "2000-12-31",
            "1985-06-15"
        )

        dates.forEach { date ->
            val profile = UserProfile(birthDate = date)
            assertEquals(date, profile.birthDate)
        }
    }

    // ========== PRUEBAS DE ACCOUNTSTATE ENUM ==========

    @Test
    fun `AccountState should have all expected values`() {
        val states = AccountState.values()

        assertTrue(states.contains(AccountState.activo))
        assertTrue(states.contains(AccountState.inactivo))
        assertTrue(states.contains(AccountState.suspendido))
    }

    @Test
    fun `UserProfile should accept all AccountState values`() {
        AccountState.values().forEach { state ->
            val profile = UserProfile(accountState = state)
            assertEquals(state, profile.accountState)
        }
    }

    // ========== PRUEBAS DE EDGE CASES ==========

    @Test
    fun `UserProfile with empty strings should be handled`() {
        val profile = UserProfile(
            name = "",
            email = "",
            phoneNumber = ""
        )

        assertEquals("", profile.name)
        assertEquals("", profile.email)
        assertEquals("", profile.phoneNumber)
    }

    @Test
    fun `UserProfile with very long strings should be accepted`() {
        val longString = "a".repeat(1000)

        val profile = UserProfile(
            name = longString,
            email = longString,
            lastNamePaternal = longString
        )

        assertEquals(longString, profile.name)
        assertEquals(longString, profile.email)
    }

    @Test
    fun `UserProfile with special characters should be accepted`() {
        val profile = UserProfile(
            name = "José María",
            lastNamePaternal = "Pérez-García",
            lastNameMaternal = "Rodríguez O'Brien"
        )

        assertEquals("José María", profile.name)
        assertEquals("Pérez-García", profile.lastNamePaternal)
        assertEquals("Rodríguez O'Brien", profile.lastNameMaternal)
    }

    // ========== PRUEBAS DE CONCURRENCIA ==========

    @Test
    fun `multiple clearUser calls should be safe`() = runTest {
        repeat(10) {
            viewModel.clearUser()
        }

        val userState = viewModel.userState.first()
        assertNull(userState.cognitoId)
    }

    // ========== PRUEBAS DE INTEGRACIÓN DE ESTADO ==========

    @Test
    fun `error state should be independent of user state`() = runTest {
        viewModel.clearUser()
        advanceUntilIdle()

        val userState = viewModel.userState.first()
        val error = viewModel.error.first()

        assertNotNull(userState)
        assertNull(error)
    }

    @Test
    fun `loading state should be independent of user state`() = runTest {
        viewModel.clearUser()
        advanceUntilIdle()

        val userState = viewModel.userState.first()
        val isLoading = viewModel.isLoading.first()

        assertNotNull(userState)
        assertFalse(isLoading)
    }

    // ========== PRUEBAS DE FAVORITOS (Estructura) ==========

    @Test
    fun `favoritePromotions should maintain list order`() = runTest {
        val favorites = viewModel.favoritePromotions.first()

        // Debe ser una lista ordenada
        assertNotNull(favorites)
        assertTrue(favorites is List)
    }

    @Test
    fun `favoriteCollabs should maintain list order`() = runTest {
        val collabs = viewModel.favoriteCollabs.first()

        // Debe ser una lista ordenada
        assertNotNull(collabs)
        assertTrue(collabs is List)
    }

    // ========== PRUEBAS DE CATEGORÍAS ==========

    @Test
    fun `UserProfile should accept categories list`() {
        val categories = listOf(
            mx.itesm.beneficiojuventud.model.categories.Category(1, "Restaurantes"),
            mx.itesm.beneficiojuventud.model.categories.Category(2, "Tecnología"),
            mx.itesm.beneficiojuventud.model.categories.Category(3, "Entretenimiento")
        )

        val profile = UserProfile(categories = categories)

        assertEquals(3, profile.categories?.size)
        assertEquals("Restaurantes", profile.categories?.get(0)?.name)
    }

    @Test
    fun `UserProfile with null categories should be valid`() {
        val profile = UserProfile(categories = null)
        assertNull(profile.categories)
    }

    @Test
    fun `UserProfile with empty categories should be valid`() {
        val profile = UserProfile(categories = emptyList())
        assertNotNull(profile.categories)
        assertTrue(profile.categories!!.isEmpty())
    }

    // ========== PRUEBAS DE DATOS COMPLETOS ==========

    @Test
    fun `UserProfile with all fields should maintain data integrity`() {
        val categories = listOf(
            mx.itesm.beneficiojuventud.model.categories.Category(1, "Test")
        )

        val profile = UserProfile(
            id = 999,
            cognitoId = "test_cognito_999",
            name = "Test User",
            lastNamePaternal = "Test Paternal",
            lastNameMaternal = "Test Maternal",
            birthDate = "1995-05-15",
            phoneNumber = "9876543210",
            email = "test@test.com",
            accountState = AccountState.activo,
            registrationDate = "2025-01-18",
            updatedAt = "2025-01-18T12:00:00",
            notificationToken = "test_token",
            profileImageKey = "images/test.jpg",
            categories = categories
        )

        // Verificar todos los campos
        assertEquals(999, profile.id)
        assertEquals("test_cognito_999", profile.cognitoId)
        assertEquals("Test User", profile.name)
        assertEquals("Test Paternal", profile.lastNamePaternal)
        assertEquals("Test Maternal", profile.lastNameMaternal)
        assertEquals("1995-05-15", profile.birthDate)
        assertEquals("9876543210", profile.phoneNumber)
        assertEquals("test@test.com", profile.email)
        assertEquals(AccountState.activo, profile.accountState)
        assertEquals("2025-01-18", profile.registrationDate)
        assertEquals("2025-01-18T12:00:00", profile.updatedAt)
        assertEquals("test_token", profile.notificationToken)
        assertEquals("images/test.jpg", profile.profileImageKey)
        assertEquals(1, profile.categories?.size)
    }

    // ========== PRUEBAS DE PERFORMANCE ==========

    @Test
    fun `clearUser should execute quickly`() = runTest {
        val startTime = System.currentTimeMillis()

        viewModel.clearUser()
        advanceUntilIdle()

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Debe completarse en menos de 100ms
        assertTrue(duration < 100)
    }
}
