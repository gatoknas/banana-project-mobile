package org.banana.project.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.banana.project.data.network.BananaApi
import org.banana.project.data.network.models.CategoryDto
import org.banana.project.data.network.models.LoginRequest
import org.banana.project.data.network.models.LoginResponse
import org.banana.project.data.network.models.ProductDto
import org.banana.project.data.network.models.RefreshRequest
import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call

class ProductCatalogSyncTest {

    private class FakeApi(
        private val products: List<ProductDto> = emptyList(),
        private val categories: List<CategoryDto> = emptyList(),
        private val failProducts: Boolean = false
    ) : BananaApi {
        override suspend fun login(request: LoginRequest): LoginResponse =
            throw NotImplementedError("Not needed for this test")

        override fun refreshToken(request: RefreshRequest): Call<LoginResponse> =
            throw NotImplementedError("Not needed for this test")

        override suspend fun getProducts(): List<ProductDto> {
            if (failProducts) throw RuntimeException("HTTP 500")
            return products
        }

        override suspend fun getCategories(): List<CategoryDto> = categories
    }

    private class FakeProductRepository : ProductRepository {
        val upserted = mutableListOf<Product>()

        override suspend fun insert(product: Product): Long = 0L
        override suspend fun insertAll(products: List<Product>): List<Long> = emptyList()
        override suspend fun upsertAll(products: List<Product>) {
            upserted += products
        }
        override suspend fun update(product: Product) {}
        override suspend fun delete(product: Product) {}
        override suspend fun deleteById(productId: Long) {}
        override suspend fun getById(productId: Long): Product? = null
        override fun getAll(): Flow<List<Product>> = flowOf(emptyList())
        override suspend fun getAllSync(): List<Product> = emptyList()
        override fun getByCategory(category: String): Flow<List<Product>> = flowOf(emptyList())
        override fun searchByName(searchQuery: String): Flow<List<Product>> = flowOf(emptyList())
        override suspend fun getCount(): Int = 0
        override suspend fun getSoldCounts(): Map<Long, Int> = emptyMap()
    }

    @Test
    fun `catalog sync TDT scenarios`() = runBlocking {
        data class TestCase(
            val name: String,
            val products: List<ProductDto>,
            val categories: List<CategoryDto>,
            val failProducts: Boolean,
            val expectedSuccess: Boolean,
            val expectedProductCount: Int,
            val expectedCategories: List<String>,
            val expectedPersisted: Int
        )

        val sellableApple = ProductDto(
            id = 1L,
            name = "Manzana",
            description = "Manzana roja",
            categoryId = 2L,
            categoryName = "Frutas",
            sellPrice = 10.0,
            isForSale = true
        )
        val nonSellableIngredient = ProductDto(
            id = 2L,
            name = "Azucar",
            sellPrice = 0.0,
            isForSale = false
        )
        val sellableJuice = ProductDto(
            id = 3L,
            name = "Jugo",
            description = "Jugo natural",
            categoryId = 4L,
            categoryName = "Jugos Naturales",
            sellPrice = 8.0,
            isForSale = true
        )

        val testCases = listOf(
            TestCase(
                name = "Success persists only sellable products and returns snapshot",
                products = listOf(sellableApple, nonSellableIngredient, sellableJuice),
                categories = listOf(
                    CategoryDto(2L, "Frutas"),
                    CategoryDto(3L, "Helados"),
                    CategoryDto(2L, "Frutas")
                ),
                failProducts = false,
                expectedSuccess = true,
                expectedProductCount = 2,
                expectedCategories = listOf("Frutas", "Helados"),
                expectedPersisted = 2
            ),
            TestCase(
                name = "API failure returns failure and persists nothing",
                products = listOf(sellableApple),
                categories = listOf(CategoryDto(2L, "Frutas")),
                failProducts = true,
                expectedSuccess = false,
                expectedProductCount = 0,
                expectedCategories = emptyList(),
                expectedPersisted = 0
            )
        )

        for (tc in testCases) {
            val repo = FakeProductRepository()
            val syncer = ProductCatalogSync(
                api = FakeApi(tc.products, tc.categories, tc.failProducts),
                productRepository = repo
            )

            val result = syncer.sync()

            assertEquals("Failed scenario: ${tc.name} (success)", tc.expectedSuccess, result.isSuccess)
            assertEquals(
                "Failed scenario: ${tc.name} (persisted count)",
                tc.expectedPersisted,
                repo.upserted.size
            )

            if (tc.expectedSuccess) {
                val snapshot = result.getOrThrow()
                assertEquals(
                    "Failed scenario: ${tc.name} (product count)",
                    tc.expectedProductCount,
                    snapshot.productCount
                )
                assertEquals(
                    "Failed scenario: ${tc.name} (categories)",
                    tc.expectedCategories,
                    snapshot.categories
                )
                assertTrue(
                    "Failed scenario: ${tc.name} (category mapped)",
                    repo.upserted.first { it.id == 1L }.categoryName == "Frutas"
                )
            } else {
                assertTrue("Failed scenario: ${tc.name} (failure)", result.isFailure)
                assertFalse("Failed scenario: ${tc.name} (nothing persisted)", repo.upserted.isNotEmpty())
            }
        }
    }
}
