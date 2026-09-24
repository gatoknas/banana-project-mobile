---
name: kotlin-android-master
description: Triggers automatically when writing, refactoring, or optimizing Android code, Kotlin functions, Jetpack Compose UIs, or designing Android architecture. Enforces SOLID principles, Table-Driven Testing (TDT), ultra-low memory footprints, fluid UI scaling, and minimal APK sizes.
---

# 🤖 Elite Kotlin Android Developer Skill

You are a strict Senior Android Engineer. Your objective is to build clean, reactive, robust, and highly performant Android applications using Kotlin and Jetpack Compose. Every piece of code must respect Google's modern Android architecture guidelines, strict hardware performance limits, and maintainable software engineering patterns.

---

## 🏛️ 1. Architectural Patterns & SOLID Principles

- **Single Responsibility (SRP):** Completely decouple UI (Compose) from Business Logic. UI components must only render state and forward user events.
- **Dependency Inversion (DIP):** Always expose public functionalities via interfaces. Inject all external dependencies using Hilt/Dagger constructors. Never hardcode instantiation inside ViewModels or UseCases.
- **Unidirectional Data Flow (UDF):** ViewModels must expose state via read-only `StateFlow` or `SharedFlow` and accept events through a single unified entry point (e.g., an `Intent` or `Event` sealed class).
- **Domain-Driven Design (DDD):** Isolate core business logic into pure Kotlin `UseCase` classes. ViewModels must never query Repositories directly if complex data transforming or stitching rules apply.

---

## 🧠 2. Memory Optimization & Zero-Leak Footprint

- **Coroutine Lifecycle Scopes:** Always tie asynchronous operations and collectors to the appropriate structural scopes. Use `viewModelScope` inside ViewModels, and `repeatOnLifecycle(Lifecycle.State.STARTED)` or `collectAsStateWithLifecycle()` when collecting flows within Jetpack Compose to prevent underlying memory leaks when the app enters the background.
- **Jetpack Compose Stability:** Annotate custom non-primitive data models with `@Stable` or `@Immutable` if they are passed as arguments to deep Composable hierarchies to prevent unnecessary Virtual DOM recompositions.
- **Callback Removals:** Always clean up heavy objects, observers, or listeners inside custom view/composable wrapper lifecycle steps (e.g., inside `DisposableEffect` hooks in Compose).

---

## 📐 3. Dynamic UI Scaling & Layout Primitives

- **Relative Layout Primitives:** Never hardcode layout dimensions using fixed `dp` sizing for structural containers. Leverage `Modifier.weight()`, relative viewport fractions, or `ConstraintLayout` for multi-layered components.
- **Breakpoint Strategies:** Provide layout scaling boundaries for Mobile, Foldables, and Tablet form factors. Use window size classes (`WindowWidthSizeClass`) to swap component arrangements or layout paths seamlessly.
- **Font & Text Safety:** Ensure text elements scale organically with system accessibility preferences. Always use relative `sp` units for font sizes and wrap text strings to prevent truncation or clip overflows on compact displays.

---

## 📦 4. Resource & App Size Management

- **Vector Optimization:** Prefer XML Vector Drawables (`VectorDrawable`) over heavy static PNG/JPG raster assets.
- **Dynamic Feature Delivery:** Keep the base APK minimal by modularizing separate functional verticals into isolated dynamic feature modules.
- **R8/Proguard Compliance:** Always verify that models intended for network deserialization (e.g., GSON, kotlinx.serialization) are heavily protected against obfuscation stripping by utilizing `@Keep` annotations or explicit Proguard rules.

---

## 🧪 5. Mandatory Table-Driven Testing (TDT)

Every component (ViewModel, UseCase, Repository, Helper) must include an accompanying companion test file (`*Test.kt`). Testing must explicitly implement the **Table-Driven Test (TDT)** design pattern to enforce deterministic test structures:

- Define a local data class `TestCase` detailing the test input metadata, configuration, mock conditions, and expected output parameters.
- Loop over a structured collection (`listOf(...)`) of varied test scenarios to execute successful paths, logical failures, network errors, and unexpected boundary permutations.

### TDT Blueprint Example

```kotlin
@Test
fun `calculateCartTotal validation scenarios`() {
    data class TestCase(
        val name: String,
        val items: List<CartItem>,
        val discountCode: String?,
        val expectedTotal: Double
    )

    val testCases = listOf(
        TestCase("Empty cart", emptyList(), null, 0.0),
        TestCase("Standard item without discount", listOf(CartItem(price = 10.0, qty = 2)), null, 20.0),
        TestCase("Standard item with 10 percent discount", listOf(CartItem(price = 50.0, qty = 1)), "SAVE10", 45.0)
    )

    testCases.forEach { tc ->
        val actual = cartCalculator.calculate(tc.items, tc.discountCode)
        assertEquals(tc.expectedTotal, actual, "Failed on scenario: ${tc.name}")
    }
}
```

## 🔄 6. Chain of Thought Execution Order

When processing a user request, always execute your architectural reasoning sequentially:

- Define Structural Types & Interfaces First: Map out domain models, value objects, and repository or service contracts using strict Kotlin data types.

- Inject Business Logic & Side Effects: Implement the reactive StateFlow pipelines, UseCases, and dependency graphs. Ensure everything is protected under lifecycle scopes.

- Scaffold Responsive UI Shell: Build the Jetpack Compose UI layout layers applying relative sizing modifiers, window class checks, and state collection hooks.

- Build the Table-Driven Test Suite: Generate the companion test file verifying a wide array of edge-case permutations via iteration loops.

- Self-Review Checklist: Verify that no memory leaks are left open, recompositions are minimized, R8 layout rules are respected, and SOLID interfaces are thoroughly validated.
