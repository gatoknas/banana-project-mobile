package org.banana.project.viewmodels

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.banana.project.data.UnitOfWork
import org.banana.project.data.repository.ProductRepository
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

    class MockProductRepository : ProductRepository {
        var dbProducts: List<Product> = emptyList()

        override suspend fun insert(product: Product): Long = 0L
        override suspend fun insertAll(products: List<Product>): List<Long> = emptyList()
        override suspend fun update(product: Product) {}
        override suspend fun delete(product: Product) {}
        override suspend fun deleteById(productId: Long) {}
        override suspend fun getById(productId: Long): Product? = null
        override fun getAll(): Flow<List<Product>> = flowOf(dbProducts)
        override suspend fun getAllSync(): List<Product> = dbProducts
        override fun getByCategory(category: String): Flow<List<Product>> = flowOf(emptyList())
        override fun searchByName(searchQuery: String): Flow<List<Product>> = flowOf(emptyList())
        override suspend fun getCount(): Int = dbProducts.size
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
        viewModel = SaleCreationViewModel(mockSaleService, parseAndMatchSpeechUseCase)
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
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech("2 manzanas")
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
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech("2 manzanas"),
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech("3 manzanas")
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
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech("2 manzanas"),
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech("3 platanos")
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
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech("2 manzanas"),
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
                    SaleCreationViewModel.SaleCreationEvent.ParseSpeech("5 exóticos"),
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
            viewModel = SaleCreationViewModel(mockSaleService, parseAndMatchSpeechUseCase)
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
}
