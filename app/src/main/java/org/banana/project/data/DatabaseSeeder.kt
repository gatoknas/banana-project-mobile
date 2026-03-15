package org.banana.project.data

import org.banana.project.data.repository.ProductRepository
import org.banana.project.model.Product
import org.banana.project.utils.AppLogger
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
        "Higo", "Breva", "Mora", "Guayaba", "Piña", "manzana", "mango", 
        "pera", "uva", "naranja", "mandarina", "limon", "lima", "toronja", 
        "pomelo", "kiwi", "fresa", "frambuesa", "arándano", "cereza", "ciruela", 
        "durazno", "melocotón", "albaricoque", "nectarina", "higo", "breva", 
        "mora", "guayaba", "piña", "manzana", "mango", "pera", "uva", "naranja", 
        "mandarina", "limon", "lima", "toronja", "pomelo", "kiwi", "fresa", 
        "frambuesa", "arándano", "cereza", "ciruela", "durazno", "melocotón", 
        "albaricoque", "nectarina", "banano", "platano", "papaya", "sandia", 
        "melon", "salpicon"
    )

    suspend fun seed() {
        AppLogger.i("Starting Database Seeder Evaluation...")
        val count = productRepository.getCount()
        AppLogger.i("Current DB Product count: $count")
        
        if (count == 0) {
            AppLogger.d("Database is empty. Preparing to inject ${colombianFruits.size} default fruits.")
            val now = Instant.now()
            val products = colombianFruits.map { name ->
                Product(
                    id = 0, // Auto-generated
                    name = name,
                    description = "Delicious Colombian fruit: $name",
                    sellPrice = (100..20000).random().toDouble(),
                    createdAt = now,
                    updatedAt = now
                )
            }

            try {
                productRepository.insertAll(products)
                AppLogger.i("Successfully seeded the BananaDatabase with ${products.size} products!")
            } catch (e: Exception) {
                AppLogger.e("Error creating products on database: ${e.message}")
            }
        }
    }
}
