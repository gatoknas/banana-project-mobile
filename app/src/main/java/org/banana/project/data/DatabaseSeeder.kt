package org.banana.project.data

import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.Product
import java.time.Instant
import javax.inject.Inject

class DatabaseSeeder @Inject constructor(
    private val productRepository: ProductRepository
) {
    private val colombianFruits = listOf(
        "Lulo", "Maracuyá", "Guanábana", "Tomate de Árbol", "Uchuva",
        "Borojó", "Chontaduro", "Granadilla", "Pitaya", "Curuba",
        "Feijoa", "Mangostino", "Zapote", "Mamoncillo", "Guama",
        "Corozo", "Níspero", "Gulupa", "Badea", "Caimito",
        "Papayuela", "Anon", "Carambolo", "Pomarrosa", "Tamarindo",
        "Higo", "Breva", "Mora", "Guayaba", "Piña"
    )

    suspend fun seed() {
        android.util.Log.d("DatabaseSeeder", "Seeding started")
        val count = productRepository.getCount()
        android.util.Log.d("DatabaseSeeder", "Current product count: $count")
        if (count == 0) {
            val now = Instant.now()
            val products = colombianFruits.map { name ->
                Product(
                    id = 0, // Auto-generated
                    name = name,
                    description = "Delicious Colombian fruit: $name",
                    sellPrice = 1000.0,
                    createdAt = now,
                    updatedAt = now
                )
            }

            try {
                productRepository.insertAll(products)
            } catch (e: Exception) {
                android.util.Log.e("DatabaseSeeder", "Error creating products on database", e)
            }
        }
    }
}
