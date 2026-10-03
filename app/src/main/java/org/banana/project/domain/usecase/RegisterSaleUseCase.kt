package org.banana.project.domain.usecase

import kotlinx.coroutines.delay
import org.banana.project.data.network.models.SaleItemRequestDto
import org.banana.project.data.network.models.SaleRequestDto
import org.banana.project.data.repository.AuthRepository
import org.banana.project.data.repository.SaleRemoteRepository
import org.banana.project.model.Sale
import org.banana.project.model.SaleItem
import org.banana.project.services.SaleService
import org.banana.project.utils.AppLogger
import javax.inject.Inject

/**
 * Outcome of registering a sale.
 */
sealed interface RegisterSaleResult {
    /** The sale was accepted by the API. */
    data class Synced(val saleId: Long) : RegisterSaleResult

    /** The API could not be reached; the sale was stored locally and flagged for sync. */
    data class SavedOffline(val localSaleId: Long) : RegisterSaleResult
}

/**
 * Registers a sale: tries the API up to [MAX_ATTEMPTS] times with a short backoff,
 * then persists it locally. On remote success the sale is also written locally
 * (dual-write, `pendingSync = false`); on repeated failure it is saved locally with
 * `pendingSync = true` so a future sync can retry it.
 */
class RegisterSaleUseCase @Inject constructor(
    private val saleRemoteRepository: SaleRemoteRepository,
    private val saleService: SaleService,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        sale: Sale,
        items: List<SaleItem>,
        paymentMethod: String
    ): Result<RegisterSaleResult> {
        val userId = authRepository.getCurrentUser()?.id?.toLongOrNull()
            ?: return Result.failure(IllegalStateException("No se pudo identificar al usuario actual"))

        val request = SaleRequestDto(
            userId = userId,
            totalAmount = sale.totalAmount,
            paymentMethod = paymentMethod,
            items = items.map { SaleItemRequestDto(productId = it.productId, quantity = it.quantity.toDouble()) }
        )

        return attemptRemoteSale(request).fold(
            onSuccess = { remoteSaleId ->
                persistLocally(sale, items, pendingSync = false)
                Result.success(RegisterSaleResult.Synced(remoteSaleId))
            },
            onFailure = { error ->
                AppLogger.w("Remote sale failed after retries, saving offline: ${error.message}")
                persistLocally(sale, items, pendingSync = true)
                    .map { localSaleId -> RegisterSaleResult.SavedOffline(localSaleId) }
            }
        )
    }

    private suspend fun attemptRemoteSale(request: SaleRequestDto): Result<Long> {
        var lastError: Throwable? = null
        for (attempt in 1..MAX_ATTEMPTS) {
            if (attempt > 1) {
                delay(BACKOFF_MILLIS[attempt - 2])
            }
            val result = saleRemoteRepository.createSale(request)
            if (result.isSuccess) {
                return result
            }
            lastError = result.exceptionOrNull()
        }
        return Result.failure(lastError ?: IllegalStateException("Sale registration failed"))
    }

    private suspend fun persistLocally(
        sale: Sale,
        items: List<SaleItem>,
        pendingSync: Boolean
    ): Result<Long> = saleService.createSale(sale.copy(pendingSync = pendingSync), items)

    private companion object {
        const val MAX_ATTEMPTS = 5
        val BACKOFF_MILLIS = longArrayOf(500, 1_000, 2_000, 3_000)
    }
}
