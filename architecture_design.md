# Android Local Database Architecture Design

## Overview
This design implements a local Room database for storing products catalog and sales transactions, with a Unit of Work pattern for coordinated CRUD operations.

## Database Schema

### Entities
- **ProductEntity**: Stores product information
  - id: Long (Primary Key, auto-generated)
  - name: String
  - category: String
  - sellPrice: Double
  - cost: Double
  - supplier: String

- **SellEntity**: Stores sell transactions
  - id: Long (Primary Key, auto-generated)
  - totalAmount: Double
  - dateTime: Instant

- **SellItemEntity**: Stores items within a sell (embedded in SellEntity)
  - productId: Long
  - quantity: Int

### Relationships
- SellEntity contains a list of SellItemEntity (one-to-many, embedded)

## Architecture Layers

### 1. Data Layer
- **Room Database**: `AppDatabase` class extending RoomDatabase
- **DAOs**: `ProductDao`, `SellDao` interfaces with CRUD operations
- **Entities**: Room entity classes

### 2. Repository Layer
- **ProductRepository**: Handles Product CRUD operations
- **SellRepository**: Handles Sell CRUD operations

### 3. Unit of Work Layer
- **UnitOfWork**: Coordinates multiple repository operations within transactions
- Provides methods for complex operations involving both products and sells

### 4. Service Layer
- **ProductService**: Business logic for product operations
- **SellService**: Business logic for sell operations
- Located in `app/src/main/java/org/banana/project/services/`

### 5. Dependency Injection
- Hilt modules for providing database, repositories, and services

## Key Design Decisions

### Room Configuration
- Use Room 2.6+ for modern features
- Enable foreign key constraints
- Use Kotlin data classes for entities
- Implement type converters for Instant dates

### Unit of Work Pattern
- Provides transactional boundaries for complex operations
- Ensures data consistency across multiple entities
- Methods like `createSellWithItems()` that handle both sell and sell items

### Service Layer Responsibilities
- Business logic validation
- Data transformation between domain models and entities
- Error handling and logging
- Integration with repositories via UnitOfWork

## Implementation Steps

1. Add Room dependencies to build.gradle.kts
2. Create Room entities with proper annotations
3. Define DAO interfaces with suspend functions for async operations
4. Create AppDatabase class
5. Implement repository classes
6. Create UnitOfWork class
7. Implement service classes
8. Set up Hilt dependency injection modules
9. Update domain models if needed for database compatibility

## Dependencies to Add
```kotlin
// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
```

## File Structure
```
app/src/main/java/org/banana/project/
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── entities/
│   │   │   ├── ProductEntity.kt
│   │   │   └── SellEntity.kt
│   │   └── dao/
│   │       ├── ProductDao.kt
│   │       └── SellDao.kt
│   ├── repository/
│   │   ├── ProductRepository.kt
│   │   └── SellRepository.kt
│   └── UnitOfWork.kt
├── services/
│   ├── ProductService.kt
│   └── SellService.kt
└── di/
    └── DatabaseModule.kt
```

This architecture provides a clean separation of concerns, transactional consistency, and maintainable code structure for the local database operations.