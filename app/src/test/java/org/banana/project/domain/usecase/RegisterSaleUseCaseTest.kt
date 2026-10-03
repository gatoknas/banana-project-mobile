package org.banana.project.domain.usecase

import kotlinx.coroutines.test.runTest
import org.banana.project.data.UnitOfWork
import org.banana.project.data.network.models.SaleRequestDto
import org.banana.project.data.repository.AuthRepository
import org.banana.project.data.repository.SaleRemoteRepository
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import org.banana.project.model.User
import org.banana.project.services.SaleService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RegisterSaleUseCaseTest {

    private class FakeSaleRemoteRepository(
        private val results: MutableList<Result<Long>>
    ) : SaleRemoteRepository {
        var callCount = 0
            private set
        var capturedRequest: SaleRequestDto? = null
            private set

        override suspend fun createSale(request: SaleRequestDto): Result<Long> {
            callCount++
            capturedRequest = request
            return if (results.isNotEmpty()) {
                results.removeAt(0)
            } else {
                Result.failure(RuntimeException("no result"))
            }
        }
    }

    private class FakeSaleService : SaleService {
        var capturedSale: Sale? = null
            private set

        override suspend fun createSale(sale: Sale, items: List<SaleItem>): Result<Long> {
            capturedSale = sale
            return Result.success(99L)
        }

        override suspend fun getSale(saleId: Long): Result<Pair<Sale, List<SaleItem>>?> = Result.success(null)
        override suspend fun deleteSale(saleId: Long): Result<Unit> = Result.success(Unit)
        override suspend fun getSalesReport(startDate: Instant, endDate: Instant): Result<UnitOfWork.SalesReport> =
            Result.success(UnitOfWork.SalesReport(0.0, 0, 0, startDate, endDate))
        override suspend fun calculateTotalAmount(sale: Sale, items: List<SaleItem>): Result<Double> = Result.success(0.0)
    }

    private class FakeAuthRepository(private val user: User?) : AuthRepository {
        override suspend fun login(username: String, password: String): Result<Unit> = Result.success(Unit)
        override fun logout() {}
        override fun getAccessToken(): String? = null
        override fun getRefreshToken(): String? = null
        override fun getCurrentUser(): User? = user
    }

    private val sale = Sale(
        id = 0,
        items = emptyList(),
        totalAmount = 20.0,
        dateTime = Instant.parse("2026-01-01T00:00:00Z")
    )
    private val items = listOf(SaleItem(productId = 1L, quantity = 2, unitPrice = 10.0))

    @Test
    fun `register sale TDT scenarios`() = runTest {
        data class TestCase(
            val name: String,
            val remoteResults: List<Result<Long>>,
            val user: User?,
            val expectedSuccess: Boolean,
            val expectedSynced: Boolean,
            val expectedRemoteCalls: Int,
            val expectedPendingSync: Boolean?
        )

        val validUser = User(id = "7", username = "cashier", name = "Cajero", role = "Vendedor")

        val testCases = listOf(
            TestCase(
                name = "success on first attempt dual-writes with pendingSync=false",
                remoteResults = listOf(Result.success(42L)),
                user = validUser,
                expectedSuccess = true,
                expectedSynced = true,
                expectedRemoteCalls = 1,
                expectedPendingSync = false
            ),
            TestCase(
                name = "success after one failure",
                remoteResults = listOf(Result.failure(RuntimeException("net")), Result.success(7L)),
                user = validUser,
                expectedSuccess = true,
                expectedSynced = true,
                expectedRemoteCalls = 2,
                expectedPendingSync = false
            ),
            TestCase(
                name = "all attempts fail saves offline with pendingSync=true",
                remoteResults = List(5) { Result.failure(RuntimeException("net")) },
                user = validUser,
                expectedSuccess = true,
                expectedSynced = false,
                expectedRemoteCalls = 5,
                expectedPendingSync = true
            ),
            TestCase(
                name = "missing user id fails without persisting",
                remoteResults = listOf(Result.success(1L)),
                user = null,
                expectedSuccess = false,
                expectedSynced = false,
                expectedRemoteCalls = 0,
                expectedPendingSync = null
            )
        )

        testCases.forEach { tc ->
            val remote = FakeSaleRemoteRepository(tc.remoteResults.toMutableList())
            val saleService = FakeSaleService()
            val useCase = RegisterSaleUseCase(remote, saleService, FakeAuthRepository(tc.user))

            val result = useCase(sale, items, "Cash")

            assertEquals("Failed scenario: ${tc.name} (success)", tc.expectedSuccess, result.isSuccess)
            assertEquals("Failed scenario: ${tc.name} (remote calls)", tc.expectedRemoteCalls, remote.callCount)

            if (tc.expectedSuccess) {
                val outcome = result.getOrThrow()
                if (tc.expectedSynced) {
                    assertTrue("Failed scenario: ${tc.name} (synced)", outcome is RegisterSaleResult.Synced)
                } else {
                    assertTrue("Failed scenario: ${tc.name} (saved offline)", outcome is RegisterSaleResult.SavedOffline)
                }
                assertEquals(
                    "Failed scenario: ${tc.name} (pendingSync persisted)",
                    tc.expectedPendingSync,
                    saleService.capturedSale?.pendingSync
                )
            } else {
                assertTrue("Failed scenario: ${tc.name} (failure)", result.isFailure)
            }
        }
    }
}
