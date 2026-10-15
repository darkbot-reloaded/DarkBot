# AGENTS.md

## Project Overview

DarkBot is a memory-bot for a flash browser-game. It reads game memory via native APIs (JNI) and controls the player's ship automatically.

- **Language:** Java 11
- **Build system:** Gradle 8.6 (Kotlin DSL)
- **GUI:** Java Swing with FlatLaf dark theme
- **Package manager:** Gradle (Maven Central + JitPack)

## Setup Commands

- Install dependencies: `gradle build` (or `./gradlew build` on Linux/Mac)
- Start development: `gradle run`
- Build distribution JAR: `gradle proguard`
- Run tests: `gradle test`

## Project Structure

```
src/main/java/
  com/github/manolo8/darkbot/   # Core bot code
    Bot.java                     # Entry point (main class)
    Main.java                    # Core tick loop, module management
    core/                        # Engine: API, entities, managers, utils
    config/                      # Configuration system (nested Config.java)
    modules/                     # Bot behavior modules (Loot, Collector, etc.)
    extensions/                  # Plugin system & feature framework
    gui/                         # Swing UI components
    backpage/                    # Web backend interactions
    utils/                       # HTTP, i18n, encryption, login
  eu/darkbot/                    # Newer API interfaces (DarkMem, DarkHook, etc.)
src/main/resources/lang/         # i18n files (17 languages)
src/test/java/                   # Tests (JUnit 5)
```

## Development Workflow

- The main entry point is `com.github.manolo8.darkbot.Bot`
- The bot runs an infinite tick loop (~100ms active, 250ms idle) in `Main.java`
- Each tick: validates state → ticks managers → ticks active module → ticks behaviors → processes queued tasks
- Managers are singletons installed via `BotInstaller` (HeroManager, MapManager, StatsManager, etc.)
- Plugins load from `plugins/` directory as isolated JAR files with restricted security

## Testing

- Framework: JUnit Jupiter 5.9.0 + Mockito 4.10.0
- Test location: `src/test/java/`
- Run all tests: `gradle test`
- Run single test: `gradle test --tests "eu.darkbot.util.RecyclingQueueTest"`

## Code Style

- **No enforced formatter or linter** — follow the existing code style in surrounding files
- Use Lombok annotations (`@Getter`, `@Setter`, `@Data`, etc.) to reduce boilerplate
- Config classes use deep nesting with annotation-based GUI generation (`@Option`, `@Dropdown`, `@Number`)
- Entity models follow a factory/registry pattern (`EntityFactory`, `EntityRegistry`)
- Managers use singleton pattern with `@Singleton` and are installed in `BotInstaller`

## Build and Deployment

- CI: GitHub Actions (`.github/workflows/gradle.yml`) — triggers on push/PR to `master`
- CI runs: `gradle proguard` on ubuntu-latest with OpenJDK 11
- Distribution JAR output: `build/DarkBot.jar`
- ProGuard strips unused fastutil packages for smaller JAR

## Pull Request Guidelines

- Always verify `gradle build` passes before submitting
- Follow existing code patterns — don't introduce new frameworks without discussion
- Keep PRs focused on a single feature or fix

## Sensitive Areas

These areas are critical and changes require extra care:

1. **Plugin System** (`extensions/plugins/`) — Security-sensitive; plugins run with restricted permissions. Don't weaken the security model.
2. **Native API Layer** (`eu/darkbot/api/`, `lib/`) — JNI bindings that interact with game memory. Changes can break the entire bot.
3. **Core Config** (`config/Config.java`) — Deeply nested config tree. Changes affect all modules and plugins. Backward compatibility matters.
4. **Main Tick Loop** (`Main.java`) — Performance-critical. Don't add blocking operations.
5. **Entity Models** (`core/entities/`) — Many components depend on these. Breaking changes propagate widely.

## Key Architecture Notes

- **Manager Pattern:** Each game subsystem has a singleton manager. New features should create or extend managers rather than adding global state.
- **Plugin Isolation:** Plugins cannot load native libraries or create class loaders. The `PluginClassLoader` provides isolation.
- **Feature/Module System:** Gameplay behaviors are `Module` implementations. Auxiliary behaviors are `Behavior` implementations. Use `FeatureRegistry` for registration.
- **Pathfinding:** Custom A\* implementation in `core/utils/pathfinder/` with obstacle avoidance (rectangles, circles, polygons) and TSP optimization.
- **i18n:** 17 languages supported. New user-facing strings must go in `src/main/resources/lang/messages_en.properties` and be translatable.

## Plugin API (DarkBotAPI)

The project uses a separate public API for plugin development: [DarkBotAPI](https://github.com/darkbot-reloaded/DarkBotAPI)

- **Repository:** `eu.darkbot.DarkBotAPI:darkbot-impl:0.9.9` (via JitPack)
- **License:** LGPL-3.0
- **Package:** `eu.darkbot.api.*`

### API Structure

```
eu.darkbot.api/
  API.java              # Core API interface
  PluginAPI.java        # Plugin-facing API interface
  config/               # Configuration API for plugins
  events/               # Event system (subscribe/publish game events)
  exceptions/           # Custom exception types
  extensions/           # Plugin, Module, Behavior extension points
  future/               # Async/future utilities
  game/                 # Game entity abstractions (Ship, Npc, Box, Portal, etc.)
  managers/             # Manager interfaces (HeroManager, MapManager, etc.)
  utils/                # Utility classes
```

### Key Interfaces for Plugin Development

- `Plugin` — Entry point for plugin lifecycle (load, unload, config)
- `Module` — Primary gameplay behavior implementation
- `Behavior` — Auxiliary behavior that runs alongside active module
- `Feature` — Base interface for registered features
- `HeroAPI` — Access to hero ship state and actions
- `MapAPI` — Map data and entity access
- `Configurable<T>` — Type-safe config binding for plugins

### Plugin Development Notes

- Plugins must use only DarkBotAPI interfaces — direct access to bot internals is restricted
- The `PluginClassLoader` isolates plugins; they cannot load native libraries or create class loaders
- Plugin config is stored via `Configurable<T>` and managed by the plugin system
- Events can be subscribed via `Eventable` interface or event bus
- Plugins are distributed as JARs in the `plugins/` directory

## Common Gotchas

- `gson` is pinned to 2.8.9 specifically for enum serialization behavior — do not upgrade without testing enum config handling
- The bot reads game memory directly — game updates can break memory offsets (check `replacements.txt`)
- `fastutil` is used heavily for performance — use primitive collections (`IntObjectMap`, etc.) over boxed equivalents
