package org.banana.project.domain.usecase

import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.ParsedSaleItem
import org.banana.project.utils.ProductMatchingService
import org.banana.project.utils.SpanishParserHelper
import javax.inject.Inject

/**
 * UseCase for parsing spoken list input and matching the items against database products.
 * This encapsulates speech parsing and matching domain logic.
 */
class ParseAndMatchSpeechUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(text: String): List<ParsedSaleItem> {
        val items = SpanishParserHelper.parseSpeech(text)
        val dbProducts = productRepository.getAllSync()
        return ProductMatchingService.matchParsedItemsToProducts(items, dbProducts)
    }
}
