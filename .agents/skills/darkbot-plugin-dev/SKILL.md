---
name: darkbot-plugin-dev
description: "Guide for developing DarkBot plugins. Use when creating plugins, writing plugin.json, implementing @Feature classes (Module, Behavior, Task), understanding plugin lifecycle, PluginClassLoader security restrictions, feature registration, plugin signatures, or troubleshooting plugin loading issues. Triggers on: 'create plugin', 'plugin dev', 'plugin system', 'plugin.json', 'PluginClassLoader', 'feature annotation', 'plugin lifecycle', 'plugin security', 'plugin loading', 'task dev'."
---

# DarkBot Plugin Development

## Overview

DarkBot plugins are JAR files placed in the `plugins/` directory. They are loaded at startup by `PluginHandler`, isolated by `PluginClassLoader`, and registered via `FeatureRegistry`.

## Plugin Lifecycle

```
plugins/ scanned for .jar files
  → plugin.json read from JAR root
  → uniqueness test (no duplicate names)
  → compatibility test (minVersion ≤ Main.VERSION ≤ supportedVersion)
  → signature test (AuthAPI.checkPluginJarSignature)
  → PluginClassLoader created (URLClassLoader with all plugin JARs)
  → @Feature classes discovered via reflection
  → FeatureRegistry registers each feature
  → ConfigHandler binds config to features
  → FeatureInstanceLoader creates instances via DI
```

Events fired: `BEFORE_LOAD → AFTER_LOAD → AFTER_LOAD_COMPLETE → AFTER_LOAD_COMPLETE_UI`

## plugin.json Format

Place at JAR root. Required fields:

```json
{
  "name": "My Plugin",
  "version": "1.0.0",
  "minVersion": "1.0.0",
  "supportedVersion": "1.9.9",
  "description": "What my plugin does",
  "author": "YourName",
  "update": {
    "url": "https://example.com/plugin.json"
  },
  "download": "https://example.com/plugin-latest.jar",
  "basePackage": "com.example.myplugin",
  "features": []
}
```

- `basePackage`: Used to auto-discover `@Feature` classes if `features` array is empty
- `features`: Explicitly list feature class FQCNs (alternative to basePackage scanning)
- `update`/`download`: Optional, enables in-bot update notifications

## @Feature Annotation

Two versions exist (both work):

```java
// Internal (com.github.manolo8.darkbot.extensions.features.Feature)
@Feature(name = "My Feature", description = "Does something useful")

// API (eu.darkbot.api.extensions.Feature) - preferred for plugins
@Feature(name = "My Feature", description = "Does something useful", enabledByDefault = true)
```

`enabledByDefault` controls whether the feature starts enabled when the plugin is first loaded.

## Creating a Module Plugin

```java
package com.example.myplugin;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Module;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.managers.HeroAPI;

@Feature(name = "My Custom Module", description = "A custom gameplay module")
public class MyModule implements Module {

    private Main main;
    private HeroAPI hero;

    @Override
    public void install(Main main) {
        this.main = main;
        this.hero = main.hero;
    }

    @Override
    public void tickModule() {
        // Your logic here, called every ~100ms when active
    }

    @Override
    public String status() {
        return "My status text";
    }
}
```

## Creating a Behavior Plugin

Behaviors run alongside the active module (auxiliary actions):

```java
@Feature(name = "My Behavior", description = "Runs alongside the active module")
public class MyBehavior implements Behavior {

    private final Main main;

    // DI injection via constructor
    public MyBehavior(Main main) {
        this.main = main;
    }

    @Override
    public void onTickBehavior() {
        // Runs every tick regardless of active module
    }
}
```

## Creating a Task Plugin

Tasks run on the **background thread** (`BackpageManager`), not the main tick thread. They are ideal for HTTP requests, data processing, or any operation that may block:

```java
@Feature(name = "My Task", description = "Background data processor")
public class MyTask implements Task {

    private final Main main;

    public MyTask(Main main) {
        this.main = main;
    }

    @Override
    public void tickTask() {
        // Called ONLY when SID is valid (player logged in)
        // Safe for game backpage HTTP requests
    }

    @Override
    public void backgroundTick() {
        // Called every tick (~100ms) regardless of login state
        // Use for external API calls, non-game-network operations
    }
}
```

### Task vs Module vs Behavior

|               | Module          | Behavior | Task       |
| ------------- | --------------- | -------- | ---------- |
| Thread        | Main            | Main     | Background |
| Blocking      | No              | No       | **Yes**    |
| Concurrent    | One at a time   | Multiple | Multiple   |
| Controls ship | Yes             | No       | No         |
| When active   | Selected module | Always   | Always     |

**Key points:**

- `tickTask()` requires valid SID — use for backpage requests
- `backgroundTick()` runs always — use for external APIs
- Tasks share one background thread cooperatively; blocking is fine
- Do NOT access main-thread data without synchronization. If you need both, implement `Behavior` + `Task` on the same class

## Creating a Configurable Feature

Use `Configurable<T>` for features with settings:

```java
@Feature(name = "Configurable Feature", description = "Has settings")
public class MyFeature implements Module, Configurable<MyFeature.Config> {

    private Config config;

    @Override
    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    public static class Config {
        public @Option boolean ENABLED = true;
        public @Option @Number(min = 1, max = 100) int THRESHOLD = 50;
        public @Option @Dropdown(options = MySupplier.class) String MODE = "FAST";
    }
}
```

## PluginClassLoader Security

The `PluginClassLoader` blocks these from plugins:

| Blocked              | Pattern                                    |
| -------------------- | ------------------------------------------ |
| Reflection           | `java.lang.reflect.*`                      |
| Threads              | `java.lang.Thread`                         |
| System tray          | `java.awt.TrayIcon`, `java.awt.SystemTray` |
| Runtime exec         | `java.lang.Runtime`                        |
| Process builder      | `java.lang.ProcessBuilder`                 |
| Native libraries     | `findLibrary()` throws SecurityException   |
| ClassLoader creation | Blocked at classloader level               |

Plugins CANNOT:

- Load native libraries (.dll, .so)
- Create custom classloaders
- Use reflection to access blocked classes
- Access bot internals directly (use DarkBotAPI interfaces)

## Plugin API (Dependency Injection)

Access bot services via the `PluginAPI`:

```java
// In constructor or install method:
HeroAPI hero = pluginApi.requireInstance(HeroAPI.class);
MapAPI map = pluginApi.requireInstance(MapAPI.class);
OreAPI ores = pluginApi.requireInstance(OreAPI.class);

// Register custom instances:
pluginApi.addInstance(MyService.class, new MyService());

// Register lazy singletons:
pluginApi.addImplementations(MyService.class);
```

## Plugin Directory Structure

```
plugins/
  my-plugin.jar
    ├── plugin.json          # Required
    ├── com/example/myplugin/
    │   ├── MyModule.class
    │   └── MyBehavior.class
    └── META-INF/
        └── MANIFEST.MF
```

## Hot Reload

Call `PluginHandler.updatePlugins()` or use the bot GUI reload button. Plugins are unloaded and reloaded. The `PluginClassLoader` is closed and recreated.

## Common Issues

| Issue                    | Cause                        | Fix                                 |
| ------------------------ | ---------------------------- | ----------------------------------- |
| `missing plugin.json`    | No plugin.json at JAR root   | Add plugin.json to JAR              |
| `LOADED_TWICE`           | Duplicate plugin name        | Change name in plugin.json          |
| `BOT_UPDATE`             | Bot version < minVersion     | Update bot or lower minVersion      |
| `PLUGIN_NOT_SIGNED`      | JAR not signed               | Sign with AuthAPI or accept warning |
| `ClassNotFoundException` | Blocked by PluginClassLoader | Use allowed APIs only               |
| Feature not loading      | Not annotated with @Feature  | Add @Feature annotation             |
