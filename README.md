# DSquare SDK

A modular Android SDK that provides **phone-based authentication** and a **loyalty coupons** browsing experience, packaged as a standalone library module (`:dsquares`) with a sample host application (`:app`).

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Host App (:app)                   │
│  MyApp.onCreate() → DSquareSDK.init(app, apiKey)    │
│  Activity → DSquareSDK.showCoupons { result → … }   │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                   SDK (:dsquares)                    │
│                                                     │
│  ┌─────────────────────────────────────────────┐    │
│  │  Public API  (DSquareSDK.kt / SdkResult.kt) │    │
│  └──────────────────────┬──────────────────────┘    │
│                         │                           │
│  ┌──────────────────────▼──────────────────────┐    │
│  │  UI Layer (Jetpack Compose + Navigation)    │    │
│  │  Screens → ViewModels → StateFlow           │    │
│  └──────────────────────┬──────────────────────┘    │
│                         │                           │
│  ┌──────────────────────▼──────────────────────┐    │
│  │  Domain Layer                               │    │
│  │  UseCases · Repository interfaces · Models  │    │
│  └──────────────────────┬──────────────────────┘    │
│                         │                           │
│  ┌──────────────────────▼──────────────────────┐    │
│  │  Data Layer                                 │    │
│  │  Retrofit · OkHttp interceptors · Paging 3  │    │
│  │  DataStore · TokenManager · CryptoManager   │    │
│  └─────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

| Layer | Responsibility |
|-------|---------------|
| **Public API** | Single entry-point object (`DSquareSDK`) exposing `init`, `logIn`, `logout`, and `showCoupons`. |
| **UI** | Jetpack Compose screens (Login, Coupons) with MVVM. State is hoisted to the screen level; child composables are stateless. Material 3 theming throughout. |
| **Domain** | Use cases (`LoginUseCase`, `GetCouponsUseCase`, `ValidatePhoneUseCase`, `IsUserLoggedInUseCase`), repository interfaces, and domain models. |
| **Data** | Retrofit service, OkHttp interceptor chain (connectivity, headers, auth, token refresh), Paging 3 `PagingSource`, encrypted token storage via Android Keystore + AES/GCM. |
| **DI** | `ServiceLocator` singleton — lazily provides all internal dependencies. |

**Key patterns:** MVVM, Clean Architecture, Repository pattern, sealed `Result` types for error handling, Jetpack Paging 3 for infinite scroll, encrypted DataStore for secure token persistence.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Android Studio | Ladybug or newer |
| JDK | 11+ |
| Android Gradle Plugin | 9.0.1 |
| Kotlin | 2.3.10 |
| Min SDK | 26 (Android 8.0) |
| Compile / Target SDK | 36 |

---

## Setup, Build & Run

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd DsquareTask
   ```

2. **Open in Android Studio** — File > Open, select the project root.

3. **Sync Gradle** — Android Studio will auto-sync. If not, click *Sync Now* in the notification bar.

4. **Run the app**
   - Select the `:app` run configuration and a device/emulator (API 26+).
   - Click **Run**.

5. **Run tests**
   ```bash
   ./gradlew :dsquares:testDebugUnitTest
   ```

---

## SDK Public Interface

All public API is accessed through the **`DSquareSDK`** object.

### 1. Initialization

Call **once** in your `Application.onCreate()`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DSquareSDK.init(this, apiKey = "YOUR_API_KEY")
    }
}
```

> Register the `Application` subclass in `AndroidManifest.xml`:
> ```xml
> <application android:name=".MyApp" ... />
> ```

### 2. Login

Authenticate a user with an Egyptian mobile number (11 digits, prefix `010`/`011`/`012`/`015`).

**Kotlin (coroutines):**
```kotlin
val result = DSquareSDK.logIn("01012345678")
when (result) {
    is LoginResult.Success -> { /* authenticated */ }
    is LoginResult.Error  -> {
        Log.e("Login", "${result.code}: ${result.message}")
    }
}
```

**Java (blocking — must be called off the main thread):**
```java
LoginResult result = DSquareSDK.logInBlocking("01012345678");
```

#### `LoginResult`

| Variant | Fields | Description |
|---------|--------|-------------|
| `Success` | — | User authenticated; token stored securely. |
| `Error` | `code: ErrorCode`, `message: String` | Authentication failed. |

#### `ErrorCode`

| Value | Meaning |
|-------|---------|
| `INVALID_PHONE` | Phone number failed validation. |
| `NO_INTERNET` | Device has no internet connection. |
| `NETWORK_ERROR` | A network or HTTP error occurred. |
| `LOGIN_FAILED` | Server rejected the credentials. |
| `UNKNOWN` | Unexpected error. |

### 3. Logout

```kotlin
val success: Boolean = DSquareSDK.logout()
```

Java-friendly blocking variant:
```java
boolean success = DSquareSDK.logoutBlocking();
```

### 4. Show Coupons

Launch the built-in coupons browsing screen. Four approaches are available:

#### Option A — Auto-launch (Composable)

Launches the coupons screen immediately when the composable enters the composition:

```kotlin
DSquareSDK.ShowCoupons { result ->
    when (result) {
        is CouponsResult.Success  -> { /* result.data */ }
        is CouponsResult.Failure  -> { /* result.message */ }
        is CouponsResult.Canceled -> { /* user pressed back */ }
    }
}
```

#### Option B — On-demand launch (Composable)

Remember a launcher and trigger it on a user action (e.g. button click):

```kotlin
val launcher = DSquareSDK.rememberLauncherForDSquareSDK { result ->
    when (result) {
        is CouponsResult.Success  -> { /* result.data */ }
        is CouponsResult.Failure  -> { /* result.message */ }
        is CouponsResult.Canceled -> { /* user pressed back */ }
    }
}

Button(onClick = {
    if (launcher != null) DSquareSDK.launchCoupons(activity, launcher)
}) { Text("Show Coupons") }
```

#### Option C — Activity Result API (register + launch)

```kotlin
class HostActivity : ComponentActivity() {
    private lateinit var couponsLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        couponsLauncher = DSquareSDK.registerCouponsLauncher(this) { result ->
            // handle CouponsResult
        }
    }

    fun openCoupons() {
        DSquareSDK.launchCoupons(this, couponsLauncher)
    }
}
```

#### Option D — Fire-and-forget (no result callback)

```kotlin
DSquareSDK.launchCoupons(activity)
```

#### `CouponsResult`

| Variant | Fields | Description |
|---------|--------|-------------|
| `Success` | `data: String` | Coupon data returned. |
| `Failure` | `message: String?` | An error occurred. |
| `Canceled` | — | User dismissed the screen. |

---

## Integration into a New Host Application

### Step 1 — Add the SDK module

Copy the `dsquares/` directory into your project root, then include it in `settings.gradle.kts`:

```kotlin
include(":dsquares")
```

### Step 2 — Add the dependency

In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":dsquares"))
}
```

### Step 3 — Initialize

In your `Application` subclass:

```kotlin
DSquareSDK.init(this, apiKey = "YOUR_API_KEY")
```

### Step 4 — Use the API

Call `logIn` / `logout` / `showCoupons` as shown in the [SDK Public Interface](#sdk-public-interface) section above.

### Permissions

The SDK declares the following permissions in its own manifest (merged automatically):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

No runtime permissions are required.

---

## Project Structure

```
DsquareTask/
├── app/                          # Host application module
│   └── src/main/
│       ├── java/com/example/app/
│       │   ├── MyApp.kt          # SDK initialization
│       │   └── MainActivity.kt   # Launches SDK UI
│       └── AndroidManifest.xml
│
├── dsquares/                     # SDK library module
│   └── src/
│       ├── main/java/com/dsquares/library/
│       │   ├── DSquareSDK.kt     # Public API entry point
│       │   ├── SdkResult.kt      # LoginResult, CouponsResult, ErrorCode
│       │   ├── domain/           # Use cases, repository interfaces, models
│       │   ├── data/             # Network, local storage, repositories
│       │   ├── security/         # AES/GCM encryption, Android Keystore
│       │   ├── di/               # ServiceLocator
│       │   └── ui/               # Compose screens, components, theme, navigation
│       ├── debug/                # Mock data source for development
│       └── test/                 # Unit tests (UseCases, Repos, ViewModels, etc.)
│
├── build.gradle.kts              # Root build config
├── settings.gradle.kts           # Module includes
└── README.md
```

---

## Tech Stack

| Category | Libraries |
|----------|-----------|
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Networking | Retrofit 3.0.0, OkHttp 5.3.2, Gson |
| Pagination | Paging 3 (runtime + compose) |
| Image Loading | Coil 3.3.0 |
| Local Storage | DataStore Preferences |
| Security | Android Keystore, AES/GCM/NoPadding |
| Testing | JUnit, MockK, Coroutines Test, OkHttp MockWebServer, Paging Testing |
