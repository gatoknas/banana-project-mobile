package org.banana.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class GetProductCatalogUseCaseTest {

    private class FakeProductRepository(
        private val products: List<Product>
    ) : ProductRepository {
        override suspend fun insert(product: Product): Long = 0L
        override suspend fun insertAll(products: List<Product>): List<Long> = emptyList()
        override suspend fun upsertAll(products: List<Product>) {}
        override suspend fun update(product: Product) {}
        override suspend fun delete(product: Product) {}
        override suspend fun deleteById(productId: Long) {}
        override suspend fun getById(productId: Long): Product? = null
        override fun getAll(): Flow<List<Product>> = flowOf(products)
        override suspend fun getAllSync(): List<Product> = products
        override fun getByCategory(category: String): Flow<List<Product>> = flowOf(products)
        override fun searchByName(searchQuery: String): Flow<List<Product>> = flowOf(products)
        override suspend fun getCount(): Int = products.size
        override suspend fun getSoldCounts(): Map<Long, Int> = emptyMap()
    }

    @Test
    fun `product catalog use case TDT scenarios`() = runBlocking {
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val apple = Product(1L, "Manzana", "Roja", 10.0, now, now, 2L, "Frutas")
        val banana = Product(2L, "Plátano", "Maduro", 5.0, now, now, 2L, "Frutas")

        data class TestCase(
            val name: String,
            val products: List<Product>,
            val expectedNames: List<String>
        )

        val testCases = listOf(
            TestCase(
                name = "Returns catalog products",
                products = listOf(apple, banana),
                expectedNames = listOf("Manzana", "Plátano")
            ),
            TestCase(
                name = "Empty catalog",
                products = emptyList(),
                expectedNames = emptyList()
            )
        )

        testCases.forEach { tc ->
            val useCase = GetProductCatalogUseCase(FakeProductRepository(tc.products))
            val result = useCase().first()
            assertEquals(
                "Failed scenario: ${tc.name}",
                tc.expectedNames,
                result.map { it.name }
            )
        }
    }
}
