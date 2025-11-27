package org.banana.project

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.banana.project.data.database.AppDatabase
import org.banana.project.data.database.entities.ProductEntity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for database operations.
 * Tests database connection, insertion of initial records, and querying.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // For testing purposes only
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testDatabaseConnectionAndOperations() = runBlocking {
        // Test database connection by performing operations
        val productDao = database.productDao()

        // Insert 10 initial product records
        val initialProducts = listOf(
            ProductEntity(name = "Apple", category = "Fruit", sellPrice = 1.50, cost = 1.00, supplier = "Farm A"),
            ProductEntity(name = "Banana", category = "Fruit", sellPrice = 0.75, cost = 0.50, supplier = "Farm B"),
            ProductEntity(name = "Orange", category = "Fruit", sellPrice = 1.25, cost = 0.80, supplier = "Farm C"),
            ProductEntity(name = "Milk", category = "Dairy", sellPrice = 3.50, cost = 2.50, supplier = "Dairy Co"),
            ProductEntity(name = "Bread", category = "Bakery", sellPrice = 2.00, cost = 1.20, supplier = "Bakery Inc"),
            ProductEntity(name = "Eggs", category = "Dairy", sellPrice = 4.00, cost = 3.00, supplier = "Farm D"),
            ProductEntity(name = "Cheese", category = "Dairy", sellPrice = 5.50, cost = 4.00, supplier = "Dairy Co"),
            ProductEntity(name = "Chicken", category = "Meat", sellPrice = 8.00, cost = 6.00, supplier = "Meat Suppliers"),
            ProductEntity(name = "Rice", category = "Grains", sellPrice = 2.50, cost = 1.80, supplier = "Grain Corp"),
            ProductEntity(name = "Pasta", category = "Grains", sellPrice = 1.80, cost = 1.20, supplier = "Pasta Ltd")
        )

        val insertedIds = productDao.insertAll(initialProducts)
        assertEquals("Should insert 10 products", 10, insertedIds.size)

        // Verify all products were inserted with valid IDs
        insertedIds.forEach { id ->
            assertTrue("Product ID should be greater than 0", id > 0)
        }

        // Query all products and verify count
        val allProducts = productDao.getAll()
        allProducts.collect { products ->
            assertEquals("Should have 10 products in database", 10, products.size)
        }

        // Query specific products to verify data integrity
        val appleProduct = productDao.getById(insertedIds[0])
        assertNotNull("Apple product should exist", appleProduct)
        appleProduct?.let { apple ->
            assertEquals("Apple name should match", "Apple", apple.name)
            assertEquals("Apple category should match", "Fruit", apple.category)
            assertEquals("Apple sell price should match", 1.50, apple.sellPrice, 0.01)
            assertEquals("Apple cost should match", 1.00, apple.cost, 0.01)
            assertEquals("Apple supplier should match", "Farm A", apple.supplier)
        }

        // Test querying by category
        val fruitProducts = productDao.getByCategory("Fruit")
        fruitProducts.collect { fruits ->
            assertEquals("Should have 3 fruit products", 3, fruits.size)
            val fruitNames = fruits.map { it.name }.toSet()
            assertTrue("Should contain Apple", fruitNames.contains("Apple"))
            assertTrue("Should contain Banana", fruitNames.contains("Banana"))
            assertTrue("Should contain Orange", fruitNames.contains("Orange"))
        }

        // Test search functionality
        val searchResults = productDao.searchByName("a")
        searchResults.collect { results ->
            assertTrue("Should find products with 'a' in name", results.size > 0)
        }

        // Verify total count
        val totalCount = productDao.getCount()
        assertEquals("Total product count should be 10", 10, totalCount)
    }
}