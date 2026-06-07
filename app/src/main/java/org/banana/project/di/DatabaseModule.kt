package org.banana.project.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.banana.project.data.UnitOfWork
import org.banana.project.data.database.BananaDatabase
import org.banana.project.data.repository.ProductRepository
import org.banana.project.data.repository.SaleRepository
import org.banana.project.services.ProductService
import org.banana.project.services.SaleService
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSqlDriver(@ApplicationContext context: Context): SqlDriver {
        return AndroidSqliteDriver(BananaDatabase.Schema, context, "banana_project_db")
    }

    @Provides
    @Singleton
    fun provideBananaDatabase(driver: SqlDriver): BananaDatabase {
        return BananaDatabase(driver)
    }

    @Provides
    @Singleton
    fun provideProductRepository(database: BananaDatabase): ProductRepository {
        return ProductRepository(database)
    }

    @Provides
    @Singleton
    fun provideSaleRepository(database: BananaDatabase): SaleRepository {
        return SaleRepository(database)
    }

    @Provides
    @Singleton
    fun provideUnitOfWork(
        productRepository: ProductRepository,
        saleRepository: SaleRepository
    ): UnitOfWork {
        return UnitOfWork(productRepository, saleRepository)
    }

    @Provides
    @Singleton
    fun provideProductService(
        unitOfWork: UnitOfWork,
        productRepository: ProductRepository
    ): ProductService {
        return ProductService(unitOfWork, productRepository)
    }

    @Provides
    @Singleton
    fun provideSaleService(unitOfWork: UnitOfWork): SaleService {
        return SaleService(unitOfWork)
    }
}
