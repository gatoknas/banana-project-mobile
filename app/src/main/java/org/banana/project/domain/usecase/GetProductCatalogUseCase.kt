package org.banana.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.Product
import javax.inject.Inject

/**
 * Exposes the locally synced product catalog as a stream, for pickers that let the
 * user choose a real product instead of relying on speech matching.
 */
class GetProductCatalogUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = productRepository.getAll()
}
