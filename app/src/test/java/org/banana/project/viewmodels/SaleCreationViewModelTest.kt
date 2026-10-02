package org.banana.project.viewmodels

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.banana.project.data.UnitOfWork
import org.banana.project.data.CatalogSnapshot
import org.banana.project.data.CatalogSyncer
import org.banana.project.data.repository.ProductRepository
import org.banana.project.domain.usecase.GetProductCatalogUseCase
import org.banana.project.domain.usecase.ParseAndMatchSpeechUseCase
import org.banana.project.model.ParsedSaleItem
import org.banana.project.model.Product
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import org.banana.project.services.SaleService
import org.banana.project.viewmodels.SaleCreationViewModel.SaleCreationEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SaleCreationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private sealed interface Step {
        data class Parse(val text: String) : Step
        data class Pick(val parsedName: String, val product: Product) : Step
        data class Clear(val parsedName: String) : Step
    }

    class MockProductRepository : ProductRepository {
        var dbProducts: List<Product> = emptyList()

        override suspend fun insert(product: Product): Long = 0L
        override suspend fun insertAll(products: List<Product>): List<Long> = emptyList()
        override suspend fun upsertAll(products: List<Product>) {}
        override suspend fun update(product: Product) {}
        override suspend fun delete(product: Product) {}
        override suspend fun deleteById(productId: Long) {}
        override suspend fun getById(productId: Long): Product? = null
        override fun getAll(): Flow<List<Product>> = flowOf(dbProducts)
        override suspend fun getAllSync(): List<Product> = dbProducts
        override fun getByCategory(category: String): Flow<List<Product>> = flowOf(emptyList())
        override fun searchByName(searchQuery: String): Flow<List<Product>> = flowOf(emptyList())
        override suspend fun getCount(): Int = dbProducts.size
        override suspend fun getSoldCounts(): Map<Long, Int> = emptyMap()
    }

    class MockSaleService : SaleService {
        var createSaleResult: Result<Long> = Result.success(1L)
        var capturedSale: Sale? = null
        var capturedItems: List<SaleItem>? = null

        override suspend fun createSale(sale: Sale, items: List<SaleItem>): Result<Long> {
            capturedSale = sale
            capturedItems = items
            return createSaleResult
        }

        override suspend fun getSale(saleId: Long): Result<Pair<Sale, List<SaleItem>>?> = Result.success(null)
        override suspend fun deleteSale(saleId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun getSalesReport(startDate: Instant, endDate: Instant): Result<UnitOfWork.SalesReport> {
            return Result.success(
                UnitOfWork.SalesReport(
                    totalAmount = 0.0,
                    saleCount = 0,
                    productsSold = 0,
                    periodStart = startDate,
                    periodEnd = endDate
                )
            )
        }
        override suspend fun calculateTotalAmount(sale: Sale, items: List<SaleItem>): Result<Double> = Result.success(0.0)
    }

    private lateinit var mockProductRepository: MockProductRepository
    private lateinit var mockSaleService: MockSaleService
    private lateinit var parseAndMatchSpeechUseCase: ParseAndMatchSpeechUseCase
    private lateinit var viewModel: SaleCreationViewModel

    private val fakeCatalogSyncer = object : CatalogSyncer {
        override suspend fun sync(): Result<CatalogSnapshot> =
            Result.success(CatalogSnapshot(productCount = 0, categories = emptyList()))
    }

    private val appleProduct = Product(1L, "Manzana", "Manzana roja", 10.0, Instant.now(), Instant.now())
    private val bananaProduct = Product(2L, "Plátano", "Plátano maduro", 5.0, Instant.now(), Instant.now())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockProductRepository = MockProductRepository().apply {
            dbProducts = listOf(appleProduct, bananaProduct)
        }
        mockSaleService = MockSaleService()
        parseAndMatchSpeechUseCase = ParseAndMatchSpeechUseCase(mockProductRepository)
        viewModel = SaleCreationViewModel(
            mockSaleService,
            parseAndMatchSpeechUseCase,
            fakeCatalogSyncer,
            GetProductCatalogUseCase(mockProductRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sale creation UDF flow TDT scenarios`() {
        data class TestCase(
            val name: String,
            val eventsToDispatch: List<SaleCreationViewModel.SaleCreationEvent>,
            val expectedParsedItemsSize: Int,
            val expectedMergedKeysSize: Int,
            val expectedSubmitResultClass: Class<out SaleCreationViewModel.SubmitResult>?,
            val shouldCallCreateSale: Boolean,
            val expectedTotalAmount: Double
        )

        val testCases = listOf(
            TestCase(
                name = "Add single matched item by speech",
                eventsToDispatch = listOf(
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech(listOf("2 manzanas"))
                ),
                expectedParsedItemsSize = 1,
                expectedMergedKeysSize = 0,
                expectedSubmitResultClass = null,
                shouldCallCreateSale = false,
                expectedTotalAmount = 20.0
            ),
            TestCase(
                name = "Merge duplicate items sums quantity",
                eventsToDispatch = listOf(
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech(listOf("2 manzanas")),
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech(listOf("3 manzanas"))
                ),
                expectedParsedItemsSize = 1,
                expectedMergedKeysSize = 1,
                expectedSubmitResultClass = null,
                shouldCallCreateSale = false,
                expectedTotalAmount = 50.0
            ),
            TestCase(
                name = "Remove item from list",
                eventsToDispatch = listOf(
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech(listOf("2 manzanas")),
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech(listOf("3 platanos"))
                ),
                expectedParsedItemsSize = 2,
                expectedMergedKeysSize = 0,
                expectedSubmitResultClass = null,
                shouldCallCreateSale = false,
                expectedTotalAmount = 35.0
            ).let { baseTc ->
                // Modify expected values after a simulated removal
                baseTc.copy(
                    eventsToDispatch = baseTc.eventsToDispatch + SaleCreationViewModel.SaleCreationEvent.RemoveItem(
                        ParsedSaleItem(3, "platanos", bananaProduct)
                    ),
                    expectedParsedItemsSize = 1,
                    expectedMergedKeysSize = 0
                )
            },
            TestCase(
                name = "Submit sale successfully with matched items",
                eventsToDispatch = listOf(
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech(listOf("2 manzanas")),
                    SaleCreationViewModel.SaleCreationEvent.SubmitSale
                ),
                expectedParsedItemsSize = 0, // Cleared after success
                expectedMergedKeysSize = 0, // Cleared after success
                expectedSubmitResultClass = SaleCreationViewModel.SubmitResult.Success::class.java,
                shouldCallCreateSale = true,
                expectedTotalAmount = 20.0
            ),
            TestCase(
                name = "Fail to submit sale when there is an unmatched item",
                eventsToDispatch = listOf(
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech(listOf("5 exóticos")),
                    SaleCreationViewModel.SaleCreationEvent.SubmitSale
                ),
                expectedParsedItemsSize = 1, // Not cleared on error
                expectedMergedKeysSize = 0, // No merge key for unmatched
                expectedSubmitResultClass = SaleCreationViewModel.SubmitResult.Error::class.java,
                shouldCallCreateSale = false,
                expectedTotalAmount = 0.0
            )
        )

        testCases.forEach { tc ->
            // Re-instantiate ViewModel to clean up internal state
            viewModel = SaleCreationViewModel(
                mockSaleService,
                parseAndMatchSpeechUseCase,
                fakeCatalogSyncer,
                GetProductCatalogUseCase(mockProductRepository)
            )
            mockSaleService.capturedSale = null
            mockSaleService.capturedItems = null

            // Dispatch events sequentially
            tc.eventsToDispatch.forEach { event ->
                if (event is SaleCreationEvent.RemoveItem) {
                    // Find actual item to remove since instance equality matters
                    val itemToRemove = viewModel.parsedItems.value.find { it.parsedName == event.item.parsedName }
                    if (itemToRemove != null) {
                        viewModel.onEvent(SaleCreationEvent.RemoveItem(itemToRemove))
                    }
                } else {
                    viewModel.onEvent(event)
                }
                testDispatcher.scheduler.advanceUntilIdle()
            }

            // Verify
            assertEquals("Failed scenario: ${tc.name} (parsed items size)", tc.expectedParsedItemsSize, viewModel.parsedItems.value.size)
            assertEquals("Failed scenario: ${tc.name} (merged keys size)", tc.expectedMergedKeysSize, viewModel.mergedItemKeys.value.size)
            if (tc.expectedSubmitResultClass != null) {
                assertNotNull("Failed scenario: ${tc.name} (submitResult should not be null)", viewModel.submitResult.value)
                assertTrue(
                    "Failed scenario: ${tc.name} (expected class ${tc.expectedSubmitResultClass.simpleName} but got ${viewModel.submitResult.value!!::class.java.simpleName})",
                    tc.expectedSubmitResultClass.isInstance(viewModel.submitResult.value)
                )
            }
            if (tc.shouldCallCreateSale) {
                assertNotNull("Failed scenario: ${tc.name} (createSale should be called)", mockSaleService.capturedSale)
                assertEquals("Failed scenario: ${tc.name} (total amount validation)", tc.expectedTotalAmount, mockSaleService.capturedSale!!.totalAmount, 0.001)
            }
        }
    }

    @Test
    fun `product picker selection TDT scenarios`() {
        data class ExpectedItem(
            val parsedName: String,
            val quantity: Int,
            val matchedProductId: Long?
        )

        data class TestCase(
            val name: String,
            val steps: List<Step>,
            val expectedItems: List<ExpectedItem>,
            val expectedMergedKey: String?,
            val expectedCatalogSize: Int
        )

        val testCases = listOf(
            TestCase(
                name = "Selecting a product on an unmatched line sets the match",
                steps = listOf(
                    Step.Parse("1 pera"),
                    Step.Pick("pera", appleProduct)
                ),
                expectedItems = listOf(ExpectedItem("pera", 1, 1L)),
                expectedMergedKey = null,
                expectedCatalogSize = 2
            ),
            TestCase(
                name = "Selecting a product already in the list merges quantities",
                steps = listOf(
                    Step.Parse("1 manzana"),
                    Step.Parse("1 pera"),
                    Step.Pick("pera", appleProduct)
                ),
                expectedItems = listOf(ExpectedItem("manzana", 2, 1L)),
                expectedMergedKey = "product_1",
                expectedCatalogSize = 2
            ),
            TestCase(
                name = "Clearing a match removes the product from the line",
                steps = listOf(
                    Step.Parse("1 manzana"),
                    Step.Clear("manzana")
                ),
                expectedItems = listOf(ExpectedItem("manzana", 1, null)),
                expectedMergedKey = null,
                expectedCatalogSize = 2
            ),
            TestCase(
                name = "Catalog is exposed to the picker",
                steps = emptyList(),
                expectedItems = emptyList(),
                expectedMergedKey = null,
                expectedCatalogSize = 2
            )
        )

        testCases.forEach { tc ->
            mockProductRepository.dbProducts = listOf(appleProduct, bananaProduct)
            viewModel = SaleCreationViewModel(
                mockSaleService,
                parseAndMatchSpeechUseCase,
                fakeCatalogSyncer,
                GetProductCatalogUseCase(mockProductRepository)
            )
            testDispatcher.scheduler.advanceUntilIdle()

            tc.steps.forEach { step ->
                when (step) {
                    is Step.Parse ->
                        viewModel.onEvent(SaleCreationEvent.ParseSpeech(listOf(step.text)))

                    is Step.Pick -> {
                        val item = viewModel.parsedItems.value.firstOrNull { it.parsedName == step.parsedName }
                        assertNotNull("Failed scenario: ${tc.name} (target item for pick)", item)
                        viewModel.onEvent(SaleCreationEvent.SelectProduct(item!!, step.product))
                    }

                    is Step.Clear -> {
                        val item = viewModel.parsedItems.value.firstOrNull { it.parsedName == step.parsedName }
                        assertNotNull("Failed scenario: ${tc.name} (target item for clear)", item)
                        viewModel.onEvent(SaleCreationEvent.ClearProductMatch(item!!))
                    }
                }
                testDispatcher.scheduler.advanceUntilIdle()
            }

            val actualItems = viewModel.parsedItems.value
                .map { ExpectedItem(it.parsedName, it.quantity, it.matchedProduct?.id) }
                .sortedBy { it.parsedName }

            assertEquals(
                "Failed scenario: ${tc.name} (items)",
                tc.expectedItems.sortedBy { it.parsedName },
                actualItems
            )
            if (tc.expectedMergedKey != null) {
                assertTrue(
                    "Failed scenario: ${tc.name} (merged key present)",
                    viewModel.mergedItemKeys.value.contains(tc.expectedMergedKey)
                )
            }
            assertEquals(
                "Failed scenario: ${tc.name} (catalog size)",
                tc.expectedCatalogSize,
                viewModel.catalog.value.size
            )
        }
    }
}
