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
import org.banana.project.data.repository.SellRepository
import org.banana.project.services.ProductService
import org.banana.project.services.SellService
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
    fun provideSellRepository(database: BananaDatabase): SellRepository {
        return SellRepository(database)
    }

    @Provides
    @Singleton
    fun provideUnitOfWork(
        productRepository: ProductRepository,
        sellRepository: SellRepository
    ): UnitOfWork {
        return UnitOfWork(productRepository, sellRepository)
    }

    @Provides
    @Singleton
    fun provideProductService(unitOfWork: UnitOfWork): ProductService {
        return ProductService(unitOfWork)
    }

    @Provides
    @Singleton
    fun provideSellService(unitOfWork: UnitOfWork): SellService {
        return SellService(unitOfWork)
    }
}