package org.banana.project

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.runBlocking
import org.banana.project.data.database.BananaDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Instrumented test for database operations using SQLDelight.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var database: BananaDatabase
    private lateinit var driver: AndroidSqliteDriver

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        driver = AndroidSqliteDriver(BananaDatabase.Schema, context, null)
        database = BananaDatabase(driver)
    }

    @After
    fun closeDb() {
        driver.close()
    }

    @Test
    fun testDatabaseConnectionAndOperations() = runBlocking {
        val productQueries = database.productQueries
        val now = Instant.now().toString()

        // Insert initial product records
        productQueries.insertProduct("Apple", "Delicious Apple", 1.50, now, now)
        productQueries.insertProduct("Banana", "Yellow Banana", 0.75, now, now)
        productQueries.insertProduct("Orange", "Juicy Orange", 1.25, now, now)

        // Query all products and verify count
        val allProducts = productQueries.selectAllProducts().executeAsList()
        assertEquals("Should have 3 products in database", 3, allProducts.size)

        // Verify data integrity for the first product
        val apple = allProducts.find { it.name == "Apple" }
        assertNotNull("Apple product should exist", apple)
        apple?.let {
            assertEquals("Apple name should match", "Apple", it.name)
            assertEquals("Apple description should match", "Delicious Apple", it.description)
            assertEquals("Apple price should match", 1.50, it.price, 0.01)
        }

        // Test update
        val updatedTime = Instant.now().toString()
        apple?.let {
            productQueries.updateProduct("Green Apple", "Tart Green Apple", 1.75, updatedTime, it.id)
        }

        val updatedApple = productQueries.selectProductById(apple!!.id).executeAsOneOrNull()
        assertNotNull("Updated Apple should exist", updatedApple)
        assertEquals("Name should be updated", "Green Apple", updatedApple?.name)
        assertEquals("Price should be updated", 1.75, updatedApple?.price ?: 0.0, 0.01)

        // Test delete
        productQueries.deleteProduct(apple.id)
        val deletedApple = productQueries.selectProductById(apple.id).executeAsOneOrNull()
        assertNull("Apple should be deleted", deletedApple)
        
        val finalCount = productQueries.selectAllProducts().executeAsList().size
        assertEquals("Should have 2 products left", 2, finalCount)
    }
}