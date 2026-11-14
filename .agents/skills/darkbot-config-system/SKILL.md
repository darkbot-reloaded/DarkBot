---
name: darkbot-config-system
description: "Guide for DarkBot's configuration system. Use when creating config classes, adding config options, working with nested config, annotation-driven GUI generation, ConfigManager persistence, ConfigBuilder tree, or plugin config. Triggers on: 'config system', 'config class', '@Option', '@Dropdown', '@Number', '@Percentage', 'ConfigManager', 'ConfigBuilder', 'config tree', 'nested config', 'plugin config', 'config annotations'."
---

# DarkBot Configuration System

## Overview

DarkBot uses deeply nested static inner classes with annotation-driven GUI generation. `ConfigBuilder` scans annotations via reflection to build a `ConfigSetting.Parent` tree rendered in Swing.

## Config.java Structure

Located at `config/Config.java`. Root class with nested static inner classes:

```java
public class Config implements eu.darkbot.api.config.legacy.Config {
    // Map-level data
    public Map<Integer, ZoneInfo> AVOIDED;
    public Map<Integer, ZoneInfo> PREFERRED;
    public Map<Integer, Set<SafetyInfo>> SAFETY;
    public Map<Integer, PlayerInfo> PLAYER_INFOS;
    public Map<String, Object> CUSTOM_CONFIGS;  // Plugin configs

    // Nested config sections (annotated with @Option)
    public @Option General GENERAL = new General();
    public @Option Collect COLLECT = new Collect();
    public @Option Loot LOOT = new Loot();
    public @Option PetSettings PET = new PetSettings();
    public @Option GroupSettings GROUP = new GroupSettings();
    public @Option Miscellaneous MISCELLANEOUS = new Miscellaneous();
    public @Option BotSettings BOT_SETTINGS = new BotSettings();

    public static class General {
        public @Option @Dropdown(options = ModuleSupplier.class)
        String CURRENT_MODULE = LootCollectorModule.class.getCanonicalName();
        public @Option @Dropdown(options = StarManager.MapOptions.class) int WORKING_MAP = 26;
        public @Option ShipConfig OFFENSIVE = new ShipConfig(1, '8');
        // ... nested Safety, Running, Roaming classes
    }
}
```

## Available Annotations

### `@Option` — Marks a field for config GUI
```java
public @Option boolean ENABLED = true;           // Simple toggle
public @Option("config.key.path") String NAME;   // Custom i18n key
```

### `@Dropdown` — Renders as dropdown selector
```java
public @Option @Dropdown(options = PetGears.class) PetGear GEAR = PetGear.PASSIVE;
public @Option @Dropdown(multi = true) Set<DisplayFlag> FLAGS;  // Multi-select
```

### `@Number` — Renders as number spinner
```java
public @Option @Number(min = 1, max = 9999, step = 10) int VALUE = 100;
public @Option @Number(min = 500, max = 20000, step = 500)
@Number.Disabled(value = -1, def = 10) int SPECIAL = -1;
```

### `@Percentage` — Displays as percentage
```java
public @Option @Percentage double HP_RANGE = 0.95;  // Shows as 95%
```

### `@Table` — Renders as editable table
```java
public @Option @Table(decorator = TableHelpers.NpcTableDecorator.class)
Map<String, NpcInfo> NPC_INFOS = new HashMap<>();
```

### `@Tag` — Player tag selection
```java
public @Option @Tag(Tag.Default.NONE) PlayerTag ENEMIES_TAG = null;
```

### `@Configuration` — i18n key prefix for a class
```java
@Configuration("config.bot_settings.api_config")
public static class PatternInfo { ... }
```

### `@Visibility` — Controls complexity level
```java
public @Option @Visibility(Level.BASIC) boolean SIMPLE;
public @Option @Visibility(Level.INTERMEDIATE) boolean MEDIUM;
public @Option @Visibility(Level.ADVANCED) boolean COMPLEX;
public @Option @Visibility(Level.DEVELOPER) boolean DEV;
```

### `@Option.Ignore` — Excludes field from config tree
```java
@Option.Ignore public transient boolean changed;
```

## Creating a New Config Section

```java
// 1. Add to Config.java
public @Option MySection MY_SECTION = new MySection();

public static class MySection {
    public @Option boolean ENABLED = false;
    public @Option @Number(min = 1, max = 100) int THRESHOLD = 50;
    public @Option @Dropdown(options = ModeSupplier.class) String MODE = "SAFE";
    public @Option @Visibility(Level.INTERMEDIATE) Nested NESTED = new Nested();

    public static class Nested {
        public @Option @Percentage double PERCENT = 0.8;
    }
}
```

## Creating a Dropdown Supplier

```java
public class ModeSupplier extends Supplier<String> {
    @Override
    public List<String> getOptions() {
        return Arrays.asList("SAFE", "AGGRESSIVE", "BALANCED");
    }

    @Override
    public String getValue(String option) {
        return option;
    }

    @Override
    public String getDisplay(String option) {
        return I18n.get("mymodule.mode." + option.toLowerCase(), option);
    }
}
```

## Config Persistence (ConfigManager)

- **Location**: Default config = `config.json`, named configs = `configs/{name}.json`
- **Backup**: Before each save, old file moved to `{name}_old.json`
- **Auto-save**: `saveChangedConfig()` called each tick, saves if 5+ seconds since last change
- **Gson**: Custom `TypeAdapterFactory` chain handles Color, Font, File, Condition, PlayerTag, ShipMode, PercentRange
- **Multiple profiles**: `getAvailableConfigs()` lists all, `loadConfig(name)` switches

### Accessing Config in Code

```java
// In a Module:
Config config = main.config;
int workingMap = config.GENERAL.WORKING_MAP;
boolean autoRefine = config.MISCELLANEOUS.AUTO_REFINE;

// In a Plugin (via API):
ConfigAPI configApi = pluginApi.requireAPI(ConfigAPI.class);
ConfigSetting<Integer> mapSetting = configApi.requireConfig("general.working_map");
```

## Plugin Config

Plugins store custom config in `Config.CUSTOM_CONFIGS`:

```java
// Store
config.CUSTOM_CONFIGS.put("my-plugin-settings", myConfigObject);

// Retrieve
Object stored = config.CUSTOM_CONFIGS.get("my-plugin-settings");
```

Or use `Configurable<T>` interface for automatic binding via `FeatureDefinition`.

## ConfigBuilder Tree Generation

`ConfigBuilder.of(Class, rootName, pluginInfo)` scans a class recursively:
- Public non-static fields with `@Option` → included
- `@Option.Ignore` → excluded
- `transient` fields → excluded
- Nested classes → intermediate nodes
- Primitives/interfaces/handled types → leaf nodes
- i18n keys: `{baseKey}.{fieldName.lowercase}` resolved via `I18nAPI`
