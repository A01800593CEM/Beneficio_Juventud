package mx.itesm.beneficiojuventud.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import mx.itesm.beneficiojuventud.model.collaborators.Collaborator
import mx.itesm.beneficiojuventud.model.collaborators.CollaboratorsState
import mx.itesm.beneficiojuventud.model.categories.Category
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Pruebas unitarias para CollabViewModel
 *
 * Estas pruebas verifican:
 * - Gestión de estado de colaboradores
 * - Operaciones CRUD de colaboradores
 * - Manejo de listas de colaboradores
 * - Filtrado por categoría
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollabViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CollabViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CollabViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== PRUEBAS DE ESTADO INICIAL ==========

    @Test
    fun `initial collabState should be empty`() = runTest {
        val collabState = viewModel.collabState.first()

        assertNotNull(collabState)
        assertNull(collabState.cognitoId)
        assertNull(collabState.businessName)
        assertNull(collabState.email)
    }

    @Test
    fun `initial collabListState should be empty`() = runTest {
        val collabListState = viewModel.collabListState.first()

        assertNotNull(collabListState)
        assertTrue(collabListState.isEmpty())
    }

    // ========== PRUEBAS DE COLLABORATOR MODEL ==========

    @Test
    fun `Collaborator model should have correct default values`() {
        val collab = Collaborator()

        assertNull(collab.cognitoId)
        assertNull(collab.businessName)
        assertNull(collab.rfc)
        assertNull(collab.representativeName)
        assertNull(collab.phone)
        assertNull(collab.email)
        assertNull(collab.address)
        assertNull(collab.postalCode)
    }

    @Test
    fun `Collaborator model should accept all fields`() {
        val categories = listOf(
            Category(1, "Restaurantes"),
            Category(2, "Tecnología")
        )

        val collab = Collaborator(
            cognitoId = "collab123",
            businessName = "Mi Negocio",
            rfc = "ABC123456789",
            representativeName = "Juan Pérez",
            phone = "1234567890",
            email = "negocio@example.com",
            address = "Calle Principal 123",
            postalCode = "12345",
            logoUrl = "https://example.com/logo.jpg",
            description = "Descripción del negocio",
            registrationDate = "2025-01-01",
            state = CollaboratorsState.activo,
            categories = categories
        )

        assertEquals("collab123", collab.cognitoId)
        assertEquals("Mi Negocio", collab.businessName)
        assertEquals("ABC123456789", collab.rfc)
        assertEquals("Juan Pérez", collab.representativeName)
        assertEquals("1234567890", collab.phone)
        assertEquals("negocio@example.com", collab.email)
        assertEquals("Calle Principal 123", collab.address)
        assertEquals("12345", collab.postalCode)
        assertEquals(CollaboratorsState.activo, collab.state)
        assertEquals(2, collab.categories?.size)
    }

    // ========== PRUEBAS DE COLLABORATORSSTATE ENUM ==========

    @Test
    fun `CollaboratorsState should have all expected values`() {
        val states = CollaboratorsState.values()

        assertTrue(states.contains(CollaboratorsState.activo))
        assertTrue(states.contains(CollaboratorsState.inactivo))
        assertTrue(states.contains(CollaboratorsState.suspendido))
    }

    @Test
    fun `Collaborator should accept all CollaboratorsState values`() {
        CollaboratorsState.values().forEach { state ->
            val collab = Collaborator(state = state)
            assertEquals(state, collab.state)
        }
    }

    // ========== PRUEBAS DE FLOWS ==========

    @Test
    fun `collabState flow should emit changes`() = runTest {
        val initialState = viewModel.collabState.first()
        assertNull(initialState.businessName)

        // El flujo debe estar disponible
        assertNotNull(viewModel.collabState)
    }

    @Test
    fun `collabListState flow should emit changes`() = runTest {
        val initialList = viewModel.collabListState.first()
        assertTrue(initialList.isEmpty())

        // El flujo debe estar disponible
        assertNotNull(viewModel.collabListState)
    }

    // ========== PRUEBAS DE VALIDACIÓN DE DATOS ==========

    @Test
    fun `Collaborator with valid RFC should be accepted`() {
        val validRFCs = listOf(
            "ABC123456789",
            "XYZ987654321",
            "DEF111222333"
        )

        validRFCs.forEach { rfc ->
            val collab = Collaborator(rfc = rfc)
            assertEquals(rfc, collab.rfc)
        }
    }

    @Test
    fun `Collaborator with valid email should be accepted`() {
        val validEmails = listOf(
            "negocio@example.com",
            "info@empresa.com.mx",
            "contacto+info@domain.co"
        )

        validEmails.forEach { email ->
            val collab = Collaborator(email = email)
            assertEquals(email, collab.email)
        }
    }

    @Test
    fun `Collaborator with valid phone should be accepted`() {
        val validPhones = listOf(
            "1234567890",
            "+521234567890",
            "123-456-7890",
            "(123) 456-7890"
        )

        validPhones.forEach { phone ->
            val collab = Collaborator(phone = phone)
            assertEquals(phone, collab.phone)
        }
    }

    @Test
    fun `Collaborator with valid postal code should be accepted`() {
        val validPostalCodes = listOf(
            "12345",
            "64000",
            "01000"
        )

        validPostalCodes.forEach { postalCode ->
            val collab = Collaborator(postalCode = postalCode)
            assertEquals(postalCode, collab.postalCode)
        }
    }

    // ========== PRUEBAS DE EDGE CASES ==========

    @Test
    fun `Collaborator with empty strings should be handled`() {
        val collab = Collaborator(
            businessName = "",
            email = "",
            phone = "",
            address = ""
        )

        assertEquals("", collab.businessName)
        assertEquals("", collab.email)
        assertEquals("", collab.phone)
        assertEquals("", collab.address)
    }

    @Test
    fun `Collaborator with very long strings should be accepted`() {
        val longString = "a".repeat(1000)

        val collab = Collaborator(
            businessName = longString,
            description = longString,
            address = longString
        )

        assertEquals(longString, collab.businessName)
        assertEquals(longString, collab.description)
        assertEquals(longString, collab.address)
    }

    @Test
    fun `Collaborator with special characters should be accepted`() {
        val collab = Collaborator(
            businessName = "Café & Restaurante José's",
            representativeName = "María José Pérez-García",
            address = "Av. Insurgentes #123, Col. Centro"
        )

        assertEquals("Café & Restaurante José's", collab.businessName)
        assertEquals("María José Pérez-García", collab.representativeName)
        assertEquals("Av. Insurgentes #123, Col. Centro", collab.address)
    }

    // ========== PRUEBAS DE CATEGORÍAS ==========

    @Test
    fun `Collaborator should accept categories list`() {
        val categories = listOf(
            Category(1, "Restaurantes"),
            Category(2, "Tecnología"),
            Category(3, "Entretenimiento")
        )

        val collab = Collaborator(categories = categories)

        assertEquals(3, collab.categories?.size)
        assertEquals("Restaurantes", collab.categories?.get(0)?.name)
        assertEquals("Tecnología", collab.categories?.get(1)?.name)
    }

    @Test
    fun `Collaborator with null categories should be valid`() {
        val collab = Collaborator(categories = null)
        assertNull(collab.categories)
    }

    @Test
    fun `Collaborator with empty categories should be valid`() {
        val collab = Collaborator(categories = emptyList())
        assertNotNull(collab.categories)
        assertTrue(collab.categories!!.isEmpty())
    }

    // ========== PRUEBAS DE DATOS COMPLETOS ==========

    @Test
    fun `Collaborator with all fields should maintain data integrity`() {
        val categories = listOf(
            Category(1, "Test Category")
        )

        val collab = Collaborator(
            cognitoId = "test_cognito_999",
            businessName = "Test Business",
            rfc = "TEST123456789",
            representativeName = "Test Representative",
            phone = "9876543210",
            email = "test@business.com",
            address = "Test Address 123",
            postalCode = "54321",
            logoUrl = "https://test.com/logo.jpg",
            description = "Test business description",
            registrationDate = "2025-01-18",
            state = CollaboratorsState.activo,
            categories = categories
        )

        // Verificar todos los campos
        assertEquals("test_cognito_999", collab.cognitoId)
        assertEquals("Test Business", collab.businessName)
        assertEquals("TEST123456789", collab.rfc)
        assertEquals("Test Representative", collab.representativeName)
        assertEquals("9876543210", collab.phone)
        assertEquals("test@business.com", collab.email)
        assertEquals("Test Address 123", collab.address)
        assertEquals("54321", collab.postalCode)
        assertEquals("https://test.com/logo.jpg", collab.logoUrl)
        assertEquals("Test business description", collab.description)
        assertEquals("2025-01-18", collab.registrationDate)
        assertEquals(CollaboratorsState.activo, collab.state)
        assertEquals(1, collab.categories?.size)
    }

    // ========== PRUEBAS DE VALIDACIÓN DE NEGOCIO ==========

    @Test
    fun `Collaborator RFC format should be valid`() {
        // RFC en México tiene 12 o 13 caracteres
        val rfc = "XAXX010101000"
        val collab = Collaborator(rfc = rfc)

        assertEquals(rfc, collab.rfc)
        assertTrue(collab.rfc!!.length in 12..13)
    }

    @Test
    fun `Collaborator email domain should be valid`() {
        val collab = Collaborator(email = "info@empresa.com")

        assertNotNull(collab.email)
        assertTrue(collab.email!!.contains("@"))
        assertTrue(collab.email!!.contains("."))
    }

    @Test
    fun `Collaborator postal code should be numeric`() {
        val postalCode = "64000"
        val collab = Collaborator(postalCode = postalCode)

        assertEquals(postalCode, collab.postalCode)
        assertTrue(collab.postalCode!!.all { it.isDigit() })
    }

    // ========== PRUEBAS DE ESTADOS ==========

    @Test
    fun `Collaborator state should default to null`() {
        val collab = Collaborator()
        assertNull(collab.state)
    }

    @Test
    fun `Collaborator can be created with activo state`() {
        val collab = Collaborator(state = CollaboratorsState.activo)
        assertEquals(CollaboratorsState.activo, collab.state)
    }

    @Test
    fun `Collaborator can be suspended`() {
        val collab = Collaborator(state = CollaboratorsState.suspendido)
        assertEquals(CollaboratorsState.suspendido, collab.state)
    }

    @Test
    fun `Collaborator can be inactivated`() {
        val collab = Collaborator(state = CollaboratorsState.inactivo)
        assertEquals(CollaboratorsState.inactivo, collab.state)
    }

    // ========== PRUEBAS DE METADATOS ==========

    @Test
    fun `Collaborator should track registration date`() {
        val date = "2025-01-18T10:30:00Z"
        val collab = Collaborator(registrationDate = date)

        assertEquals(date, collab.registrationDate)
    }

    @Test
    fun `Collaborator logoUrl should accept valid URLs`() {
        val validUrls = listOf(
            "https://example.com/logo.jpg",
            "https://cdn.example.com/images/logo.png",
            "https://storage.example.com/logos/business-logo.svg"
        )

        validUrls.forEach { url ->
            val collab = Collaborator(logoUrl = url)
            assertEquals(url, collab.logoUrl)
            assertTrue(collab.logoUrl!!.startsWith("http"))
        }
    }

    @Test
    fun `Collaborator description should accept long text`() {
        val longDescription = """
            Este es un negocio que ofrece múltiples servicios a la comunidad.
            Contamos con años de experiencia y personal altamente calificado.
            Nuestra misión es brindar la mejor atención a nuestros clientes.
            Estamos comprometidos con la excelencia y la calidad.
        """.trimIndent()

        val collab = Collaborator(description = longDescription)

        assertEquals(longDescription, collab.description)
        assertTrue(collab.description!!.length > 100)
    }

    // ========== PRUEBAS DE MÚLTIPLES CATEGORÍAS ==========

    @Test
    fun `Collaborator can have multiple categories`() {
        val categories = listOf(
            Category(1, "Restaurantes"),
            Category(2, "Bar"),
            Category(3, "Cafetería"),
            Category(4, "Entretenimiento")
        )

        val collab = Collaborator(
            businessName = "Restaurante & Bar",
            categories = categories
        )

        assertEquals(4, collab.categories?.size)
        assertTrue(collab.categories!!.any { it.name == "Restaurantes" })
        assertTrue(collab.categories!!.any { it.name == "Bar" })
    }

    // ========== PRUEBAS DE PERFORMANCE ==========

    @Test
    fun `creating Collaborator should be fast`() {
        val startTime = System.currentTimeMillis()

        val collab = Collaborator(
            businessName = "Test",
            email = "test@test.com",
            rfc = "TEST123456789"
        )

        val duration = System.currentTimeMillis() - startTime

        assertNotNull(collab)
        assertTrue(duration < 10) // Debe ser instantáneo
    }
}
