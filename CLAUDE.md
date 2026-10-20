# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Kotlin Multiplatform (Android + iOS) app for on-device LLM inference with Google LiteRT / LiteRT-LM. Package root: `org.hinsun.edge`. Modules: `:shared` (KMP + Compose Multiplatform UI), `:androidApp` (thin Android host), `iosApp/` (Xcode project, mostly native SwiftUI).

## Commands

- Android build: `./gradlew :androidApp:assembleDebug`
- Shared tests (Android host / JVM): `./gradlew :shared:testAndroidHostTest`
- Shared tests (iOS simulator): `./gradlew :shared:iosSimulatorArm64Test`
- Single test: append `--tests "org.hinsun.edge.SharedCommonTest"` (or `...ClassName.methodName`) to `testAndroidHostTest`.
- iOS app: open `iosApp/iosApp.xcodeproj` in Xcode and run. The Xcode build phase runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode` to produce the static `Shared` framework (skipped if `OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED=YES`).
- Re-link SwiftPM deps into the Xcode project after changing `swiftPMDependencies` in `shared/build.gradle.kts`: `make integrate-swiftpm`.

Dependency versions live in `gradle/libs.versions.toml`. Gradle configuration cache and build cache are on (`gradle.properties`).

## Architecture

**Two UIs coexist on iOS.** The iOS app is primarily native SwiftUI (`iosApp/iosApp/Presentation/`): a `TabView` in `BottomView.swift` with Sessions / Settings / Download tabs, plus a fourth "KMM App" tab that embeds the Compose Multiplatform UI via `MainViewControllerKt.MainViewController()` (`shared/src/iosMain/.../MainViewController.kt`). Android shows only the Compose UI (`MainActivity` → `App()`).

**Shared Compose UI** (`shared/src/commonMain/kotlin/org/hinsun/edge/`): `App.kt` → `NavGraph.kt` using JetBrains Navigation 3 (`NavDisplay` + back stack). Routes are a `@Serializable sealed interface NavRoute` in `NavRoute.kt`; **every new route must also be registered in the polymorphic `SavedStateConfiguration` (`config`) in that file and handled in `NavGraph`'s `entryProvider`**. Screens live in `screens/<feature>/` as `XxxView` + `XxxViewModel`. Theme tokens (colors, spacing, radius, typography with bundled Mulish/Andika fonts) are in `theme/`.

**LLM engine bridge.** `LLMEngine` (commonMain) is a callback-based interface (`start`, `sendStream`) with a `chatFlow()` extension adapting it to `Flow<String>`. Platforms inject an implementation into the global `LLMProvider.engine` (`lateinit`):
- iOS: `iOSApp.init()` sets `LLMProvider.shared.engine = SwiftLLMEngine()` (Swift class implementing the Kotlin interface).
- Android: `AndroidLLMEngine` exists in `androidMain` but is not yet assigned anywhere — accessing `LLMProvider.engine` on Android will throw.
Both implementations are currently stubs/echoes; `engine/LiteRTEngine.kt` is an empty placeholder.

**Real iOS inference path (native, not through Kotlin):** `iosApp/iosApp/Core/LiteRTLM/LiteRTLMEngine.swift` (`@Observable`, uses the `LiteRTLM` Swift package pulled in via KMP `swiftPMDependencies`) loads models from `Application Support/Models/`. `LiteRTLMModelDownloadManager` (singleton, background `URLSession` id `org.hinsun.edge.download`, wired through `AppDelegate`) downloads models into the same directory and persists progress to SwiftData. SwiftData container is `AppDatabase.container` with `LiteRTLMEntity`, `SessionEntity`, `MessageEntity`; `SessionStore` and `LiteRTLMEngine` are injected via `.environment(...)`. iOS navigation uses its own `NavigationManager` (`NavRoute` enum in Swift — distinct from the Kotlin `NavRoute`).

**Android LiteRT deps** (`litert`, `litertlm-android`) are declared in `shared` androidMain but not yet used.

## Conventions

- iOS deployment target 17.6 (set in `swiftPMDependencies`); iOS targets are `iosArm64` + `iosSimulatorArm64` only.
- Source files carry a header comment block (`File / Author / Date`) — keep it when creating new files.
- `iosApp/Configuration/Config.xcconfig` optionally includes a git-ignored `Secrets.xcconfig`.
