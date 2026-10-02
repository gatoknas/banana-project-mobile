package org.banana.project.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.banana.project.data.CatalogSnapshot
import org.banana.project.data.CatalogSyncer
import org.banana.project.model.Product
import org.banana.project.services.ProductService
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ProductCatalogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeProductService(
        private val products: List<Product>
    ) : ProductService {
        override suspend fun addProduct(product: Product): Result<Long> = Result.success(0L)
        override suspend fun updateProduct(product: Product): Result<Unit> = Result.success(Unit)
        override suspend fun deleteProduct(productId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun getProduct(productId: Long): Result<Product?> = Result.success(null)
        override fun getAllProducts(): Flow<List<Product>> = flowOf(products)
        override fun searchProducts(query: String): Flow<List<Product>> = flowOf(products)
        override fun getProductsByCategory(category: String): Flow<List<Product>> = flowOf(products)
        override suspend fun updateProductCatalog(products: List<Product>): Result<List<Long>> =
            Result.success(emptyList())
    }

    private class FakeCatalogSyncer(
        private val result: Result<CatalogSnapshot>
    ) : CatalogSyncer {
        override suspend fun sync(): Result<CatalogSnapshot> = result
    }

    private val now = Instant.parse("2026-01-01T00:00:00Z")

    private val apple = Product(1L, "Manzana", "Roja", 10.0, now, now, 2L, "Frutas")
    private val juice = Product(2L, "Jugo de Naranja", "Natural", 8.0, now, now, 4L, "Bebidas")
    private val water = Product(3L, "Agua", null, 2.0, now, now, 4L, "Bebidas")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        products: List<Product>,
        syncResult: Result<CatalogSnapshot>
    ): ProductCatalogViewModel {
        val viewModel = ProductCatalogViewModel(
            productService = FakeProductService(products),
            catalogSyncer = FakeCatalogSyncer(syncResult)
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `catalog search and category filtering TDT scenarios`() {
        data class TestCase(
            val name: String,
            val products: List<Product>,
            val syncResult: Result<CatalogSnapshot>,
            val events: List<ProductCatalogEvent>,
            val expectedIds: List<Long>,
            val expectedCategories: List<String>,
            val expectedErrorPresent: Boolean,
            val expectedViewMode: CatalogViewMode = CatalogViewMode.GRID
        )

        val okSnapshot = CatalogSnapshot(productCount = 3, categories = listOf("Bebidas", "Frutas"))

        val testCases = listOf(
            TestCase(
                name = "No filter returns all products with snapshot categories",
                products = listOf(apple, juice, water),
                syncResult = Result.success(okSnapshot),
                events = emptyList(),
                expectedIds = listOf(1L, 2L, 3L),
                expectedCategories = listOf("Bebidas", "Frutas"),
                expectedErrorPresent = false
            ),
            TestCase(
                name = "Name search is case-insensitive substring match",
                products = listOf(apple, juice, water),
                syncResult = Result.success(okSnapshot),
                events = listOf(ProductCatalogEvent.UpdateSearchQuery("manz")),
                expectedIds = listOf(1L),
                expectedCategories = listOf("Bebidas", "Frutas"),
                expectedErrorPresent = false
            ),
            TestCase(
                name = "Name search with no match returns empty",
                products = listOf(apple, juice, water),
                syncResult = Result.success(okSnapshot),
                events = listOf(ProductCatalogEvent.UpdateSearchQuery("zzz")),
                expectedIds = emptyList(),
                expectedCategories = listOf("Bebidas", "Frutas"),
                expectedErrorPresent = false
            ),
            TestCase(
                name = "Category filter narrows the list",
                products = listOf(apple, juice, water),
                syncResult = Result.success(okSnapshot),
                events = listOf(ProductCatalogEvent.SelectCategory("Bebidas")),
                expectedIds = listOf(2L, 3L),
                expectedCategories = listOf("Bebidas", "Frutas"),
                expectedErrorPresent = false
            ),
            TestCase(
                name = "Combined name search and category filter",
                products = listOf(apple, juice, water),
                syncResult = Result.success(okSnapshot),
                events = listOf(
                    ProductCatalogEvent.SelectCategory("Bebidas"),
                    ProductCatalogEvent.UpdateSearchQuery("jugo")
                ),
                expectedIds = listOf(2L),
                expectedCategories = listOf("Bebidas", "Frutas"),
                expectedErrorPresent = false
            ),
            TestCase(
                name = "Clearing the category restores all products",
                products = listOf(apple, juice, water),
                syncResult = Result.success(okSnapshot),
                events = listOf(
                    ProductCatalogEvent.SelectCategory("Bebidas"),
                    ProductCatalogEvent.SelectCategory(null)
                ),
                expectedIds = listOf(1L, 2L, 3L),
                expectedCategories = listOf("Bebidas", "Frutas"),
                expectedErrorPresent = false
            ),
            TestCase(
                name = "View mode toggles to list",
                products = listOf(apple, juice, water),
                syncResult = Result.success(okSnapshot),
                events = listOf(ProductCatalogEvent.SetViewMode(CatalogViewMode.LIST)),
                expectedIds = listOf(1L, 2L, 3L),
                expectedCategories = listOf("Bebidas", "Frutas"),
                expectedErrorPresent = false,
                expectedViewMode = CatalogViewMode.LIST
            ),
            TestCase(
                name = "Sync failure sets error and derives categories from products",
                products = listOf(apple, juice, water),
                syncResult = Result.failure(RuntimeException("network down")),
                events = emptyList(),
                expectedIds = listOf(1L, 2L, 3L),
                expectedCategories = listOf("Bebidas", "Frutas"),
                expectedErrorPresent = true
            ),
            TestCase(
                name = "Empty catalog yields no products but keeps snapshot categories",
                products = emptyList(),
                syncResult = Result.success(CatalogSnapshot(productCount = 0, categories = listOf("Frutas"))),
                events = emptyList(),
                expectedIds = emptyList(),
                expectedCategories = listOf("Frutas"),
                expectedErrorPresent = false
            )
        )

        testCases.forEach { tc ->
            val viewModel = createViewModel(tc.products, tc.syncResult)

            tc.events.forEach { event ->
                viewModel.onEvent(event)
                testDispatcher.scheduler.advanceUntilIdle()
            }

            val state = viewModel.state.value
            assertEquals(
                "Failed scenario: ${tc.name} (filtered ids)",
                tc.expectedIds,
                viewModel.filteredProducts.map { it.id }
            )
            assertEquals(
                "Failed scenario: ${tc.name} (categories)",
                tc.expectedCategories,
                state.categories
            )
            assertEquals(
                "Failed scenario: ${tc.name} (view mode)",
                tc.expectedViewMode,
                state.viewMode
            )
            assertEquals(
                "Failed scenario: ${tc.name} (loading finished)",
                false,
                state.isLoading
            )
            if (tc.expectedErrorPresent) {
                assertEquals(
                    "Failed scenario: ${tc.name} (error set)",
                    true,
                    state.error != null
                )
            } else {
                assertNull("Failed scenario: ${tc.name} (no error)", state.error)
            }
        }
    }
}
