package org.banana.project.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.banana.project.model.Product
import org.banana.project.services.ProductService
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateProductViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    class MockProductService : ProductService {
        var addProductResult: Result<Long> = Result.success(1L)
        var capturedProduct: Product? = null

        override suspend fun addProduct(product: Product): Result<Long> {
            capturedProduct = product
            return addProductResult
        }

        override suspend fun updateProduct(product: Product): Result<Unit> = Result.success(Unit)
        override suspend fun deleteProduct(productId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun getProduct(productId: Long): Result<Product?> = Result.success(null)
        override fun getAllProducts(): Flow<List<Product>> = flowOf(emptyList())
        override fun searchProducts(query: String): Flow<List<Product>> = flowOf(emptyList())
        override fun getProductsByCategory(category: String): Flow<List<Product>> = flowOf(emptyList())
        override suspend fun updateProductCatalog(products: List<Product>): Result<List<Long>> = Result.success(emptyList())
    }

    private lateinit var mockProductService: MockProductService
    private lateinit var viewModel: CreateProductViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockProductService = MockProductService()
        viewModel = CreateProductViewModel(mockProductService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `form validation and product creation TDT scenarios`() {
        data class TestCase(
            val name: String,
            val inputName: String,
            val inputDescription: String,
            val inputPrice: String,
            val serviceResult: Result<Long>,
            val expectedError: String?,
            val expectedSuccess: String?,
            val shouldCallService: Boolean
        )

        val testCases = listOf(
            TestCase(
                name = "Blank product name error",
                inputName = "",
                inputDescription = "A description",
                inputPrice = "10.5",
                serviceResult = Result.success(1L),
                expectedError = "Product name cannot be blank",
                expectedSuccess = null,
                shouldCallService = false
            ),
            TestCase(
                name = "Invalid decimal price error",
                inputName = "Apple",
                inputDescription = "A description",
                inputPrice = "invalid",
                serviceResult = Result.success(1L),
                expectedError = "Sell price must be a valid positive number",
                expectedSuccess = null,
                shouldCallService = false
            ),
            TestCase(
                name = "Negative price error",
                inputName = "Apple",
                inputDescription = "A description",
                inputPrice = "-5.0",
                serviceResult = Result.success(1L),
                expectedError = "Sell price must be a valid positive number",
                expectedSuccess = null,
                shouldCallService = false
            ),
            TestCase(
                name = "Successful product creation",
                inputName = "Apple",
                inputDescription = "Fresh apple",
                inputPrice = "1.5",
                serviceResult = Result.success(42L),
                expectedError = null,
                expectedSuccess = "Product created successfully!",
                shouldCallService = true
            ),
            TestCase(
                name = "Service failure error handling",
                inputName = "Banana",
                inputDescription = "Fresh banana",
                inputPrice = "2.0",
                serviceResult = Result.failure(Exception("Database connection error")),
                expectedError = "Failed to create product: Database connection error",
                expectedSuccess = null,
                shouldCallService = true
            )
        )

        testCases.forEach { tc ->
            mockProductService.capturedProduct = null
            mockProductService.addProductResult = tc.serviceResult
            
            viewModel.onEvent(CreateProductViewModel.CreateProductEvent.UpdateName(tc.inputName))
            viewModel.onEvent(CreateProductViewModel.CreateProductEvent.UpdateDescription(tc.inputDescription))
            viewModel.onEvent(CreateProductViewModel.CreateProductEvent.UpdateSellPrice(tc.inputPrice))

            var successCallbackCalled = false
            viewModel.onEvent(CreateProductViewModel.CreateProductEvent.CreateProduct {
                successCallbackCalled = true
            })

            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals("Failed scenario: ${tc.name} (error message)", tc.expectedError, viewModel.errorMessage.value)
            assertEquals("Failed scenario: ${tc.name} (success message)", tc.expectedSuccess, viewModel.successMessage.value)
            if (tc.shouldCallService) {
                assertNotNull("Failed scenario: ${tc.name} (service should be called)", mockProductService.capturedProduct)
                if (tc.serviceResult.isSuccess) {
                    assertEquals("Failed scenario: ${tc.name} (onSuccess callback)", true, successCallbackCalled)
                }
            } else {
                assertNull("Failed scenario: ${tc.name} (service should not be called)", mockProductService.capturedProduct)
            }
        }
    }
}
