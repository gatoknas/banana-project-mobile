package org.banana.project.data.repository

import kotlinx.coroutines.runBlocking
import org.banana.project.data.network.BananaApi
import org.banana.project.data.network.models.CategoryDto
import org.banana.project.data.network.models.LoginRequest
import org.banana.project.data.network.models.LoginResponse
import org.banana.project.data.network.models.ProductDto
import org.banana.project.data.network.models.RefreshRequest
import org.banana.project.data.network.models.SaleItemRequestDto
import org.banana.project.data.network.models.SaleRequestDto
import org.banana.project.data.network.models.SaleResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call

class SaleRemoteRepositoryImplTest {

    private class FakeApi(
        private val result: Result<SaleResponseDto>
    ) : BananaApi {
        var capturedRequest: SaleRequestDto? = null

        override suspend fun login(request: LoginRequest): LoginResponse =
            throw NotImplementedError("Not needed for this test")

        override fun refreshToken(request: RefreshRequest): Call<LoginResponse> =
            throw NotImplementedError("Not needed for this test")

        override suspend fun getProducts(): List<ProductDto> = emptyList()

        override suspend fun getCategories(): List<CategoryDto> = emptyList()

        override suspend fun createSale(request: SaleRequestDto): SaleResponseDto {
            capturedRequest = request
            return result.getOrThrow()
        }
    }

    @Test
    fun `create sale TDT scenarios`() = runBlocking {
        data class TestCase(
            val name: String,
            val result: Result<SaleResponseDto>,
            val expectedSuccess: Boolean,
            val expectedSaleId: Long
        )

        val request = SaleRequestDto(
            userId = 7L,
            totalAmount = 150.0,
            paymentMethod = "Cash",
            items = listOf(SaleItemRequestDto(productId = 5L, quantity = 2.0))
        )

        val testCases = listOf(
            TestCase(
                name = "success returns the server sale id",
                result = Result.success(SaleResponseDto("success", "Sale processed", 42L)),
                expectedSuccess = true,
                expectedSaleId = 42L
            ),
            TestCase(
                name = "api failure returns failure",
                result = Result.failure(RuntimeException("network down")),
                expectedSuccess = false,
                expectedSaleId = 0L
            )
        )

        testCases.forEach { tc ->
            val api = FakeApi(tc.result)
            val repository = SaleRemoteRepositoryImpl(api)

            val result = repository.createSale(request)

            assertEquals("Failed scenario: ${tc.name} (success)", tc.expectedSuccess, result.isSuccess)
            if (tc.expectedSuccess) {
                assertEquals("Failed scenario: ${tc.name} (saleId)", tc.expectedSaleId, result.getOrNull())
                assertEquals("Failed scenario: ${tc.name} (request forwarded)", request, api.capturedRequest)
            } else {
                assertTrue("Failed scenario: ${tc.name} (failure)", result.isFailure)
            }
        }
    }
}
