# 🍌 Banana Project Mobile

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2036-green.svg)](https://developer.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A voice-driven Android point-of-sale app for small fruit vendors in Colombia. Sellers speak their order in Spanish — the app recognizes products, matches them against a local catalog, and records the sale in a local database. Built for **academic and learning purposes** to sharpen skills in Android development, Kotlin, and speech-driven UX.

## 📱 Features

- 🎤 **Voice-Powered Sales**: Press-and-hold the microphone, speak a product list in Spanish, and the app builds your sell order
- 🧠 **Spanish NLP Parser**: Custom parser that handles spoken numbers ("veinte manzanas", "treinta y cinco naranjas") and extracts product + quantity pairs
- 🔍 **Fuzzy Product Matching**: Automatically matches spoken product names against the local catalog using string-similarity algorithms
- 📊 **Interactive Sell Table**: Swipe-to-delete rows, tap to edit quantities via a bottom sheet stepper, and see totals update live
- 🔄 **Smart Merge**: Speaking the same product again sums its quantity instead of creating duplicates, with a highlight animation
- ✅ **Sell Submission**: Confirmation dialog, database persistence, and automatic page reset after a sale is recorded
- 🗄️ **Local Database**: SQLDelight-backed storage for products, sells, and sell items — works fully offline
- 🌱 **Pre-loaded Catalog**: Ships with 80+ Colombian fruits seeded into the product catalog on first launch
- 🎨 **Modern UI**: Jetpack Compose + Material 3 with responsive portrait/landscape layouts and a Tokyo Night theme option

## 📋 App Screens

| Screen | Description |
|--------|-------------|
| **Crear Venta** | Voice-driven sell creation — the core screen. Speak products, review the table, edit quantities, and submit |
| **Administrar Productos** | Create and manage the product catalog |
| **Indicadores** | Dashboard with sales indicators and metrics |

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer                          │
│  Screens (Compose) → Components (ParsedResultsTable,│
│  MicrophoneAndStatus, RetroCard, HeaderMenu)        │
├─────────────────────────────────────────────────────┤
│                ViewModel Layer                      │
│  SellCreationViewModel (StateFlow, coroutines)      │
├─────────────────────────────────────────────────────┤
│                 Service Layer                       │
│  SellService, ProductService (validation + facade)  │
├─────────────────────────────────────────────────────┤
│               Unit of Work                          │
│  Transactional coordination across repositories     │
├─────────────────────────────────────────────────────┤
│              Repository Layer                       │
│  ProductRepository, SellRepository                  │
├─────────────────────────────────────────────────────┤
│               Database (SQLDelight)                 │
│  Product, Sell, SellItem tables                     │
└─────────────────────────────────────────────────────┘
```

**Patterns used:**
- **MVVM** with `ViewModel` + `StateFlow` for reactive UI
- **Repository Pattern** for data access abstraction
- **Unit of Work** for transactional database operations
- **Service Layer** for business logic and validation
- **Dependency Injection** via Hilt

## 🗄️ Database Schema

| Table | Purpose |
|-------|---------|
| **Product** | Catalog of available products (name, description, sell price) |
| **Sell** | A completed sale transaction (total amount, timestamp) |
| **SellItem** | Individual line items in a sale (product ID, quantity, unit price) |

The database is seeded on first launch with 80+ Colombian fruits at random prices.

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.3.10 |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM with ViewModels + StateFlow |
| **Dependency Injection** | Hilt (Dagger) |
| **Database** | SQLDelight (local SQLite) |
| **Navigation** | Voyager |
| **Speech Recognition** | Android SpeechRecognizer (Google STT) |
| **NLP** | Custom Spanish parser + fuzzy matching |
| **Async** | Kotlin Coroutines |
| **Build** | Gradle with Kotlin DSL + Version Catalog |

## 📋 Prerequisites

- Android Studio Jellyfish (2024.1) or later
- Minimum SDK: API 33 (Android 13)
- Target SDK: API 36
- JDK 17
- An Android device or emulator with Google Play Services (for speech recognition)

## 🏃‍♂️ Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/gatoknas/banana-project-mobile.git
   cd banana-project-mobile
   ```

2. **Open in Android Studio**:
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory

3. **Build and Run**:
   - Connect an Android device or start an emulator
   - Click "Run" (green play button) or use `Shift + F10`
   - On first launch, the database will be automatically seeded with Colombian fruits

## 📖 Usage

1. **Launch the App** and grant microphone permissions when prompted
2. **Navigate to "Crear Venta"** from the top header menu
3. **Press and hold the microphone** button, speak your order in Spanish:
   - Example: *"Veinte manzanas, cinco plátanos y treinta naranjas"*
4. **Review the parsed table** — products are fuzzy-matched against the catalog with prices
5. **Edit quantities** by tapping any quantity cell → a bottom sheet stepper appears
6. **Remove items** by swiping a row to the left
7. **Add more products** by speaking again — duplicates merge automatically
8. **Submit the sell** by tapping "Registrar Venta" → confirm in the dialog
9. The sell is stored, the page resets, and a success snackbar confirms the transaction

## 🧠 Spanish NLP Pipeline

The voice-to-sell pipeline works in 3 stages:

1. **Speech Recognition** — Android's `SpeechRecognizer` converts audio to Spanish text
2. **Parsing** — `SpanishParserHelper` + `SpanishNumberParser` extract `(quantity, product name)` pairs, handling:
   - Written-out numbers: *"veinte"* → 20, *"treinta y cinco"* → 35
   - Compound expressions: *"cinco litros de leche"* → qty=5, name="leche"
   - Conjunctions and separators: *"manzanas y plátanos"* → two items
3. **Matching** — `ProductMatchingService` fuzzy-matches parsed names against the product catalog to link each item to a database product

## 🎓 Learning Objectives

This academic project provides hands-on experience with:

- **Android Development**: Modern app architecture, permissions, and lifecycle management
- **Kotlin Programming**: Coroutines, StateFlow, sealed classes, and data classes
- **Speech Processing**: Integrating platform STT and building custom NLP parsers
- **Database Design**: SQLDelight, migrations, transactional operations, and the Unit of Work pattern
- **UI/UX Design**: Composable components, animations, bottom sheets, swipe gestures, and haptic feedback
- **Dependency Injection**: Clean architecture with Hilt
- **Version Control**: Feature branches and pull requests on GitHub

## 🤝 Contributing

Contributions are welcome! This is a learning project, so feel free to:

- Fork the repository
- Create feature branches
- Submit pull requests with improvements
- Open issues for bugs or suggestions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) for the modern UI toolkit
- [Hilt](https://dagger.dev/hilt/) for dependency injection
- [SQLDelight](https://cashapp.github.io/sqldelight/) for type-safe SQL database
- [Voyager](https://voyager.adriel.cafe/) for multiplatform navigation

---

*Built with ❤️ for learning and exploration in Android development*
