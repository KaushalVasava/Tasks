# Android Jetpack Compose Copilot Instructions

## General Rules

- Always follow Clean Architecture principles.
- Prefer readability and maintainability over clever code.
- Use Kotlin idiomatic patterns.
- Avoid unnecessary abstraction.
- Keep composables small and focused.
- Follow unidirectional data flow (UDF).

---

# Compose UI Guidelines

## Composable Rules

- Composables should be stateless whenever possible.
- State should be hoisted to parent composables or ViewModel.
- Avoid business logic inside composables.
- Avoid heavy calculations inside composables.
- Avoid side effects directly inside composable body.

Preferred:

```kotlin
@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit
)
```

Avoid:

```kotlin
@Composable
fun UserCard(viewModel: UserViewModel)
```

---

# State Management

## Preferred State Sources

Use:
- `StateFlow`
- immutable UI state data classes
- `collectAsStateWithLifecycle()` (requires `androidx.lifecycle:lifecycle-runtime-compose`)

Avoid:
- mutable shared states across composables
- `LiveData` in new screens
- mutable lists directly in UI

Preferred:

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

---

# UI State Rules

- UI state classes should be immutable.
- Use data classes with `val`.
- Never expose mutable state publicly.

Preferred:

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList()
)
```

Avoid:

```kotlin
data class HomeUiState(
    var items: MutableList<Item>
)
```

---

# One-Time UI Effects (Navigation, Snackbars)

For one-shot events (navigation, toasts, snackbars), use a `Channel` exposed as a `Flow`. Do NOT use `SharedFlow` with replay for this — missed events cause bugs.

Preferred:

```kotlin
// ViewModel
private val _uiEffect = Channel<HomeUiEffect>(Channel.BUFFERED)
val uiEffect = _uiEffect.receiveAsFlow()

// Composable
LaunchedEffect(Unit) {
    viewModel.uiEffect.collect { effect ->
        when (effect) {
            is HomeUiEffect.NavigateToDetail -> navController.navigate(...)
            is HomeUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(...)
        }
    }
}
```

---

# Recomposition Optimization

## Prevent Unnecessary Recompositions

- Use `remember` for expensive calculations.
- Use `derivedStateOf` **only** when the derived value changes less frequently than the source state. It has overhead — don't use it for simple transformations that change at the same rate.
- Use the `key()` composable to control recomposition scope in non-lazy layouts.
- Split large composables into smaller focused composables to minimize recomposition scope.
- Avoid creating objects or lambdas during recomposition.

Preferred:

```kotlin
// derivedStateOf is appropriate: filteredItems changes less often than searchQuery typing
val filteredItems by remember {
    derivedStateOf { items.filter { it.name.contains(searchQuery) } }
}

// For simple transforms that change at the same rate, remember(key) is sufficient
val sortedList = remember(items) {
    items.sortedBy { it.name }
}
```

Avoid:

```kotlin
// No remember — runs on every recomposition
val filteredItems = items.filter { it.visible }
```

## Controlling Recomposition Scope with `key()`

```kotlin
// Forces recomposition of this subtree when userId changes
key(userId) {
    UserProfileSection(userId)
}
```

---

# Stability Rules

Compose skips recomposition only if all parameters are stable. Prefer:

- Immutable data classes annotated with `@Immutable`.
- `@Stable` for classes where Compose can trust equality but properties may change (e.g., classes backed by observable state).
- Avoid `@Stable` on truly mutable classes — it's a contract, not a hint.

```kotlin
// @Immutable: all public properties are permanently immutable
@Immutable
data class User(
    val id: String,
    val name: String
)

// @Stable: equality is reliable, but internals may change in a controlled way
@Stable
class CartState(initialItems: List<Item>) {
    var items by mutableStateOf(initialItems)
}
```

Avoid:

```kotlin
// Compose cannot infer stability — mutable property breaks skipping
data class User(
    var name: String
)
```

Use the Compose Compiler Metrics (`-PcomposeCompilerReports=true`) to audit stability in your build.

---

# LazyColumn Rules

- Always provide stable, unique keys.
- Avoid nested `LazyColumn`/`LazyRow` without `height(IntrinsicSize)` or fixed size.
- Keep item composables small; extract to named functions.
- Avoid `contentType` mismatches in mixed lists — use `contentType` param for heterogeneous lists.

Preferred:

```kotlin
LazyColumn {
    items(
        items = users,
        key = { it.id },
        contentType = { "user_card" }
    ) { user ->
        UserCard(user = user, onClick = { onUserClick(user.id) })
    }
}
```

---

# Modifier Rules

- Reuse `Modifier` instances via top-level `val` or `remember` for frequently used chains.
- Order matters: `padding` before `background` clips differently than after.
- Avoid `Modifier.composed {}` — it is deprecated. Use custom `Modifier.Node` implementations for stateful modifiers.

Preferred:

```kotlin
// Reusable modifier — define at file level
private val cardModifier = Modifier
    .fillMaxWidth()
    .padding(16.dp)

// Custom stateful modifier — use Modifier.Node, not Modifier.composed
```

---

# Side Effects

## Use Correct Effect APIs

| Use case | API |
|---|---|
| One-time or key-triggered async work | `LaunchedEffect` |
| Cleanup on leave/key change | `DisposableEffect` |
| Callback referencing latest value | `rememberUpdatedState` |
| Compose state → Flow bridge | `snapshotFlow` |
| Async value producer | `produceState` |

### `rememberUpdatedState` — capture latest lambda in long-lived effects

```kotlin
@Composable
fun Timer(onTick: () -> Unit) {
    // Without rememberUpdatedState, the LaunchedEffect captures a stale onTick
    val currentOnTick by rememberUpdatedState(onTick)

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentOnTick() // always calls the latest lambda
        }
    }
}
```

### `snapshotFlow` — bridge Compose state into a coroutine Flow

```kotlin
LaunchedEffect(Unit) {
    snapshotFlow { listState.firstVisibleItemIndex }
        .distinctUntilChanged()
        .collect { index -> viewModel.onScrollPositionChanged(index) }
}
```

Avoid:
- Launching coroutines directly in composable body.
- Direct API calls in composables.
- `GlobalScope`.

---

# Coroutine Rules

- ViewModel owns all business coroutines via `viewModelScope`.
- Use `Dispatchers.IO` for I/O, `Dispatchers.Default` for CPU-heavy work — never block `Main`.
- Prefer structured concurrency with `supervisorScope` for parallel independent operations.
- Avoid `GlobalScope`.

```kotlin
viewModelScope.launch {
    supervisorScope {
        val a = async { repository.loadA() }
        val b = async { repository.loadB() }
        combine(a.await(), b.await())
    }
}
```

---

# Navigation

- Use Navigation Compose with **type-safe routes** (`@Serializable` objects, Navigation 2.8+).
- Pass only IDs or primitive arguments — never large objects or JSON.
- Avoid `rememberNavController` deep in the tree; hoist it to the root.

Preferred (type-safe, Navigation 2.8+):

```kotlin
@Serializable
data class DetailRoute(val userId: String)

// Navigate
navController.navigate(DetailRoute(userId = user.id))

// Receive
composable<DetailRoute> { backStackEntry ->
    val route: DetailRoute = backStackEntry.toRoute()
    DetailScreen(userId = route.userId)
}
```

Avoid:

```kotlin
// String routes are error-prone and deprecated practice
navController.navigate("details/${user.id}")
```

---

# Architecture Rules

## Recommended Layers

```
UI (Composable) → ViewModel → UseCase → Repository → DataSource
```

- Composables are UI-only: render state, emit events.
- Business logic lives in domain (UseCase) layer.
- ViewModel transforms domain data into UI state and handles UI events.
- Repository abstracts data sources.

---

# ViewModel Rules

- Expose `uiState` as immutable `StateFlow`.
- Expose one-time effects via `Channel.receiveAsFlow()`.
- Handle UI events as explicit functions, not exposed flows.
- Avoid Android `Context` unless absolutely required (use `ApplicationContext` via Hilt if needed).

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<HomeUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onItemClicked(id: String) {
        viewModelScope.launch {
            _uiEffect.send(HomeUiEffect.NavigateToDetail(id))
        }
    }
}
```

---

# `CompositionLocal`

Use `CompositionLocal` only for cross-cutting ambient data that is genuinely consumed at many levels of the tree (e.g., theming, analytics, locale). Do not use it as a DI shortcut or to avoid passing parameters.

```kotlin
// Correct use: ambient theming data
val LocalAppTypography = staticCompositionLocalOf { AppTypography() }

// Wrong use: passing a ViewModel or repository down the tree
val LocalUserRepository = staticCompositionLocalOf<UserRepository> { error("Not provided") }
```

- Prefer `staticCompositionLocalOf` when the value rarely changes (avoids full recomposition).
- Use `compositionLocalOf` when the value changes and consumers must recompose.

---

# Performance Best Practices

## Avoid

- Unnecessary recompositions (audit with Layout Inspector → Recomposition Counts).
- Creating objects or lambdas inside frequently recomposed composables.
- Mutable collections in UI state.
- Large monolithic composables.
- Heavy image loading in composition phase.
- Sorting/filtering on every recomposition.
- Overusing `derivedStateOf` for values that change at the same rate as their source.
- `SubcomposeLayout` / `BoxWithConstraints` unless strictly necessary — both are expensive.
- Nested scrollable containers.

## Prefer

- `remember` and `rememberSaveable` appropriately.
- Immutable collections (`kotlinx.collections.immutable` for `ImmutableList`/`ImmutableMap`).
- Paging 3 for large lists.
- Coil `AsyncImage` with placeholder and memory caching.
- Stable keys in lazy layouts.
- Baseline Profiles to AOT-compile critical Compose paths (use `ProfileInstaller` + Macrobenchmark).

---

# Baseline Profiles

Generate a Baseline Profile for production apps to eliminate JIT compilation on startup and critical paths:

```kotlin
// benchmark/src/androidTest/kotlin/BaselineProfileGenerator.kt
@ExperimentalBaselineProfilesApi
class BaselineProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "com.example.app") {
        pressHome()
        startActivityAndWait()
        // walk critical user journeys
    }
}
```

Include `ProfileInstaller` in the app module and add the generated profile to `src/main/baseline-prof.txt`.

---

# Image Loading

Use Coil 3:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .build(),
    contentDescription = null,
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error)
)
```

- Avoid loading full-resolution images larger than render size.
- Always provide `placeholder` and `error` painters.
- Prefer memory + disk caching (Coil default).

---

# Theming

- Use Material3 with a custom `ColorScheme` from Material Theme Builder.
- Centralize typography, colors, and shapes in a design system object.
- Avoid hardcoded `dp`, colors, or `sp` values outside the theme.
- Expose via `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`.

---

# Error Handling

Use sealed interfaces for UI state variants:

```kotlin
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val items: List<Item>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```

- Composables `when`-branch on state — no try/catch in UI.
- Errors are caught in the ViewModel/UseCase layer and mapped to UI state.

---

# Testing

- Write composable tests with `ComposeTestRule` (`createComposeRule()`).
- Keep composables stateless for easy isolation.
- Use `testTag` / `semantics` for reliable UI test selectors.
- Unit-test ViewModels with `kotlinx-coroutines-test` and `Turbine` for Flow assertions.
- Business logic (UseCases) should be pure Kotlin — no Android dependencies.

```kotlin
@Test
fun userCard_displaysName() {
    composeTestRule.setContent {
        UserCard(user = fakeUser, onClick = {})
    }
    composeTestRule.onNodeWithText(fakeUser.name).assertIsDisplayed()
}
```

---

# Naming Conventions

## Composables — PascalCase, noun-based

```kotlin
UserCard()
HomeScreen()
SearchBarField()
```

## State / Event / Effect types

```kotlin
HomeUiState     // immutable state
HomeUiEvent     // user intent / input event
HomeUiEffect    // one-shot side effect (navigation, toast)
```

## Preview functions — suffix `Preview`

```kotlin
@Preview
@Composable
fun UserCardPreview() {
    UserCard(user = fakeUser, onClick = {})
}
```

---

# Adaptive UI

## Overview

Adaptive UI means layouts that respond to the available window space — phones, foldables, tablets, and desktop. Use **`WindowSizeClass`** as the primary decision point for screen-level layout changes. Use **`BoxWithConstraints`** only for local, component-level adaptation when `WindowSizeClass` is insufficient. Never use `BoxWithConstraints` at screen level.

## Dependencies

```kotlin
// build.gradle.kts
implementation("androidx.compose.material3:material3-window-size-class:<version>")
// or with BOM:
implementation("androidx.compose.material3.adaptive:adaptive:<version>")
```

---

## WindowSizeClass — Screen-Level Adaptation

`WindowSizeClass` classifies the window into `Compact`, `Medium`, or `Expanded` for both width and height. Compute it **once at the Activity level** and pass it down — never recompute it deep in the tree.

### Setup in Activity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            MyApp(windowSizeClass = windowSizeClass)
        }
    }
}
```

### Passing to screens

```kotlin
@Composable
fun MyApp(windowSizeClass: WindowSizeClass) {
    val widthClass = windowSizeClass.widthSizeClass
    HomeScreen(widthClass = widthClass)
}
```

### Adapting layout at screen level

```kotlin
@Composable
fun HomeScreen(widthClass: WindowWidthSizeClass) {
    when (widthClass) {
        WindowWidthSizeClass.Compact -> {
            // Single-column, bottom navigation
            HomeCompactLayout()
        }
        WindowWidthSizeClass.Medium -> {
            // Two-pane or rail navigation
            HomeMediumLayout()
        }
        WindowWidthSizeClass.Expanded -> {
            // Side-by-side list-detail, nav drawer
            HomeExpandedLayout()
        }
    }
}
```

### Size class reference

| Class | Width range | Typical device |
|---|---|---|
| `Compact` | < 600dp | Phone portrait |
| `Medium` | 600–840dp | Phone landscape, small tablet, foldable |
| `Expanded` | > 840dp | Tablet, desktop |

---

## Navigation Pattern per Size Class

Match navigation chrome to window size — do not use the same navigation component for all sizes.

```kotlin
@Composable
fun AppNavigation(
    widthClass: WindowWidthSizeClass,
    navController: NavHostController
) {
    when (widthClass) {
        WindowWidthSizeClass.Compact ->
            AppBottomBar(navController)          // BottomNavigationBar

        WindowWidthSizeClass.Medium ->
            AppNavigationRail(navController)     // NavigationRail

        WindowWidthSizeClass.Expanded ->
            AppNavigationDrawer(navController)   // PermanentNavigationDrawer
    }
}
```

---

## List-Detail Pattern (Expanded)

On expanded screens, show list and detail pane side by side. Use `ListDetailPaneScaffold` from `material3-adaptive` (recommended) or implement manually.

### With `ListDetailPaneScaffold` (preferred)

```kotlin
@Composable
fun ItemsAdaptiveScreen(widthClass: WindowWidthSizeClass) {
    val navigator = rememberListDetailPaneScaffoldNavigator<ItemId>()

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                ItemListPane(
                    onItemClick = { id ->
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                    }
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val itemId = navigator.currentDestination?.content
                if (itemId != null) {
                    ItemDetailPane(itemId = itemId)
                } else {
                    ItemDetailPlaceholder()
                }
            }
        }
    )
}
```

### Manual two-pane (fallback)

```kotlin
@Composable
fun HomeExpandedLayout(
    selectedId: String?,
    onItemClick: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        ItemListPane(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onItemClick = onItemClick
        )
        VerticalDivider()
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            if (selectedId != null) {
                ItemDetailPane(itemId = selectedId)
            } else {
                ItemDetailPlaceholder()
            }
        }
    }
}
```

---

## BoxWithConstraints — Component-Level Adaptation Only

`BoxWithConstraints` triggers `SubcomposeLayout` under the hood — it is expensive. Use it **only** when a component genuinely needs to adapt to the space given by its parent, and that space is not known at screen level.

### When to use

- A reusable component that adapts its internal layout based on available width (e.g., a card that switches between row and column layout).
- A component embedded in contexts where the surrounding window size does not tell you how much space this specific component gets (e.g., inside a multi-column grid cell).

### When NOT to use

- At the screen or scaffold level — use `WindowSizeClass` instead.
- To replicate `WindowSizeClass` decisions lower in the tree — that's the wrong layer.
- As a default solution for anything responsive.

### Correct usage

```kotlin
@Composable
fun AdaptiveMediaCard(
    title: String,
    imageUrl: String,
    description: String,
    modifier: Modifier = Modifier
) {
    // COST NOTE: BoxWithConstraints uses SubcomposeLayout — justified here
    // because this card is used in both single and multi-column contexts.
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth >= 320.dp) {
            // Landscape card: image left, text right
            Row {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(text = description, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            // Portrait card: image top, text below
            Column {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(text = description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
```

### Wrong usage

```kotlin
// ❌ DO NOT — BoxWithConstraints at screen level; use WindowSizeClass instead
@Composable
fun HomeScreen() {
    BoxWithConstraints {
        if (maxWidth > 840.dp) ExpandedLayout() else CompactLayout()
    }
}
```

---

## LazyGrid for Adaptive Column Counts

Prefer `LazyVerticalGrid` with `GridCells.Adaptive` over manual column count logic — it automatically fills available space without needing `BoxWithConstraints`.

```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp), // fills columns naturally
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(items = products, key = { it.id }) { product ->
        ProductCard(product = product)
    }
}
```

For fixed breakpoints, use `GridCells.Fixed(count)` driven by `WindowSizeClass`:

```kotlin
val columns = when (widthClass) {
    WindowWidthSizeClass.Compact -> 1
    WindowWidthSizeClass.Medium -> 2
    else -> 3
}
LazyVerticalGrid(columns = GridCells.Fixed(columns)) { ... }
```

---

## Foldable Support

For foldable devices, use `WindowInfoTracker` to detect the fold posture and adapt layout:

```kotlin
@Composable
fun FoldableAwareLayout(activity: Activity) {
    val windowInfo = WindowInfoTracker
        .getOrCreate(activity)
        .windowLayoutInfo(activity)
        .collectAsStateWithLifecycle(initialValue = null)

    val isSeparating = windowInfo.value
        ?.displayFeatures
        ?.filterIsInstance<FoldingFeature>()
        ?.any { it.isSeparating }
        ?: false

    if (isSeparating) {
        TwoPageLayout()
    } else {
        SinglePageLayout()
    }
}
```

Dependency: `androidx.window:window:<version>`

---

## Adaptive UI Rules Summary

| Scenario | Tool |
|---|---|
| Screen-level layout switching | `WindowSizeClass` |
| Navigation chrome (bottom bar / rail / drawer) | `WindowSizeClass` |
| List-detail two-pane layout | `ListDetailPaneScaffold` |
| Component adapts to parent-given space | `BoxWithConstraints` |
| Grid column count | `GridCells.Adaptive` or `GridCells.Fixed` + `WindowSizeClass` |
| Foldable hinge detection | `WindowInfoTracker` + `FoldingFeature` |

### Rules

- Compute `WindowSizeClass` once at Activity level; pass it as a parameter.
- Never re-compute `WindowSizeClass` in deeply nested composables.
- Isolate layout variants into named composables (`HomeCompactLayout`, `HomeExpandedLayout`) — do not inline all branches.
- Test all size classes: use the Resizable Emulator or `@Preview(widthDp = 840)` annotations.
- `BoxWithConstraints` always warrants a comment explaining why `WindowSizeClass` is insufficient for this case.

---

## Previews for Adaptive Layouts

Always preview all breakpoints:

```kotlin
@Preview(name = "Compact", widthDp = 360, heightDp = 800)
@Preview(name = "Medium", widthDp = 700, heightDp = 800)
@Preview(name = "Expanded", widthDp = 1200, heightDp = 800)
@Composable
fun HomeScreenPreview() {
    val widthClass = when {
        LocalConfiguration.current.screenWidthDp < 600 -> WindowWidthSizeClass.Compact
        LocalConfiguration.current.screenWidthDp < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
    HomeScreen(widthClass = widthClass)
}
```

---

# Code Generation Rules

When generating Compose code:

- Prefer stateless composables; hoist state to ViewModel.
- Always optimize recomposition — check stability of parameters.
- Use immutable models; annotate with `@Immutable` or `@Stable` as appropriate.
- Avoid `Modifier.composed` — use `Modifier.Node` for custom stateful modifiers.
- Follow Material3.
- Use `collectAsStateWithLifecycle()` for lifecycle-aware state collection.
- Use type-safe Navigation routes.
- Use `Channel` + `receiveAsFlow()` for one-shot UI effects.
- Generate production-ready, testable code.
- Never put business logic inside composables.
- Flag `SubcomposeLayout`/`BoxWithConstraints` usage with a comment explaining the cost and why `WindowSizeClass` is insufficient.
- Use `WindowSizeClass` for screen-level adaptation; `BoxWithConstraints` only for component-level adaptation.
- Use `ListDetailPaneScaffold` for list-detail patterns on expanded screens.
- Use `GridCells.Adaptive` for responsive grids instead of `BoxWithConstraints`.

---

# Senior-Level Compose Expectations

Generated code should:
- Minimize recomposition scope deliberately
- Use correct stability annotations (`@Immutable` vs `@Stable`)
- Handle one-time effects correctly via `Channel`
- Prefer type-safe Navigation
- Use `snapshotFlow` and `rememberUpdatedState` where appropriate
- Avoid deprecated APIs (`Modifier.composed`, string routes)
- Include Baseline Profile consideration for production
- Be modular, testable, and scalable
- Follow Compose runtime best practices
- Be performance-conscious with `derivedStateOf` only where truly beneficial
- Use `WindowSizeClass` for screen-level adaptation and `BoxWithConstraints` only for justified component-level cases
- Support foldable and large-screen form factors via `ListDetailPaneScaffold` and `WindowInfoTracker`
