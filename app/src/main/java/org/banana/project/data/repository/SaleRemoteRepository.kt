package org.banana.project.data.repository

import org.banana.project.data.network.BananaApi
import org.banana.project.data.network.models.SaleRequestDto
import org.banana.project.utils.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote (API) operations for sales.
 */
interface SaleRemoteRepository {
    /**
     * Registers a sale through the API and returns the server-generated sale id.
     */
    suspend fun createSale(request: SaleRequestDto): Result<Long>
}

@Singleton
class SaleRemoteRepositoryImpl @Inject constructor(
    private val api: BananaApi
) : SaleRemoteRepository {

    override suspend fun createSale(request: SaleRequestDto): Result<Long> {
        return try {
            val response = api.createSale(request)
            Result.success(response.saleId)
        } catch (e: Exception) {
            AppLogger.e("Failed to create remote sale", e)
            Result.failure(e)
        }
    }
}
