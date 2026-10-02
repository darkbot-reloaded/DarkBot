---
name: darkbot-native-api
description: "Guide for DarkBot's native API layer (JNI). Use when working with DarkMem, DarkHook, KekkaPlayer, game memory reading, offsets, capabilities, native Flash interaction, or understanding how the bot reads game state. Triggers on: 'native API', 'JNI', 'DarkMem', 'DarkHook', 'KekkaPlayer', 'memory reading', 'game offsets', 'Capability', 'native library', 'Flash hook', 'memory address'."
---

# DarkBot Native API Layer

## Overview

DarkBot reads game memory directly through JNI native libraries. Three implementations exist:

| Implementation | Native Lib | Purpose |
|----------------|-----------|---------|
| `DarkMem` | DarkMemAPI.dll | Memory read/write via shared memory |
| `DarkHook` | DarkHookAPI.dll | Flash method hooking + callbacks |
| `KekkaPlayer` | KekkaPlayer.dll | All-in-one: window, memory, input, interaction |

## Architecture

```
Native DLLs (.dll)
    ↓ JNI
Java Bridge Classes (DarkMem.java, DarkHook.java, KekkaPlayer.java)
    ↓ implements
GameAPI Interface (Memory, Window, Handler, Interaction)
    ↓ used by
BotInstaller → Entity objects → EntityList → Managers
```

## Capability System

Each API implementation reports supported features via `Capability` enum:

```java
public enum Capability {
    // Memory operations
    DIRECT_USE_ITEM,
    DIRECT_MOVE_SHIP,
    DIRECT_COLLECT_BOX,
    DIRECT_REFINE,

    // Window management
    CREATE_WINDOW_THREAD,
    BACKGROUND_ONLY,
    WINDOW_POSITION,

    // Handler features
    CLEAR_CACHE,
    GAME_QUALITY,
    VOLUME,
    TRANSPARENCY,

    // Input
    DIRECT_SELECT,
    DIRECT_LOCK_TARGET
}
```

Check before using:
```java
if (API.hasCapability(Capability.DIRECT_COLLECT_BOX)) {
    API.collectBox(box);
}
```

## Memory Reading Pattern

Entities read their state from hardcoded memory offsets:

```java
// Entity.java - base class
public void update(long address) {
    this.locationInfo.update(API.readLong(address + 64));
    this.traits.update(API.readLong(address + 48));
    this.clickable.update(findInTraits(TraitPattern::ofClickable));
}

// Ship.java - reads health, shipInfo, playerInfo
public void update(long address) {
    super.update(address);
    playerInfo.update(API.readLong(address + 248));
    health.update(API.readLong(address + 184));
    shipInfo.update(API.readLong(address + 232));
}

// In update() (tick-based reads):
formationId = readBindableInt(280, 40);
invisible = readBoolean(160, 32);
shipId = readInt(192, 76);
```

## Key API Methods

```java
// Reading primitives
int val = API.readInt(address + offset);
long val = API.readLong(address + offset);
boolean val = API.readBoolean(address + offset);
String val = API.readString(address, offset1, offset2);

// Reading nested objects
long nestedPtr = API.readLong(address + offset);
int nestedVal = API.readInt(nestedPtr + nestedOffset);

// Bindable reads (cached/observed values)
int val = API.readBindableInt(address, offset);

// Entity operations
API.selectEntity(entity);        // Click on entity
API.lockTarget(entity);          // Lock attack target
API.collectBox(box);             // Collect resource box

// Movement
API.moveShip(x, y);             // Direct ship movement

// Items
API.useItem(item);               // Use an item (ammo, repair, etc.)
```

## BotInstaller Initialization

`BotInstaller` discovers key memory addresses by pattern matching:

```java
// Installed managers read memory at startup:
settingsManager.install(bi);   // Game settings
facadeManager.install(bi);     // Flash facade proxies
effectManager.install(bi);     // Effect system
mapManager.install(bi);        // Map data
hero.install(bi);              // Hero ship
statsManager.install(bi);      // Statistics
pingManager.install(bi);       // Network latency
repairManager.install(bi);     // Auto-repair
performanceManager.install(bi);// Tick/FPS management
```

## Flash Facade System

`FacadeManager` proxies Flash display objects:

```java
// Access Flash objects via facades:
long facadeAddress = facadeManager.getAddress("hero");

// Read Flash display properties:
int width = API.readInt(facadeAddress + 0x20);
int height = API.readInt(facadeAddress + 0x24);
```

## replacements.txt

Game updates change memory offsets. `replacements.txt` contains version-specific patches:

```
# Old offset → New offset mappings
# Applied at startup by BotInstaller
```

When the game updates and the bot breaks, check/update offsets in this file.

## Safe Native API Usage from Plugins

Plugins cannot access native APIs directly (blocked by `PluginClassLoader`). Use the API interfaces:

```java
// Via PluginAPI:
HeroAPI hero = pluginApi.requireInstance(HeroAPI.class);
hero.getLocation();              // Safe wrapper
hero.getHealth().getHp();        // Safe wrapper

// Via Managers:
OreAPI ores = pluginApi.requireInstance(OreAPI.class);
int prometium = ores.getAmount(OreAPI.Ore.PROMETIUM);
```

## Common Patterns

### Reading Entity List

```java
// MapManager maintains entity lists:
List<Npc> npcs = main.mapManager.entities.npcs;
List<Box> boxes = main.mapManager.entities.boxes;
List<Ship> ships = main.mapManager.entities.ships;
List<Portal> portals = main.mapManager.entities.portals;
```

### Checking Entity Validity

```java
// Entity is invalid if removed from game:
if (entity.isInvalid(mapManager.entities.address)) {
    // Entity no longer exists
}

// Entity removed flag:
if (entity.removed) { ... }
```

### Trait Pattern Matching

Entities have trait lists read from Flash. Use `TraitPattern` to find specific traits:

```java
long clickablePtr = findInTraits(TraitPattern::ofClickable);
long lockPtr = findInTraits(TraitPattern::ofLockType);
long attackLaserPtr = findInTraits(
    ptr -> API.readString(ptr, 48, 32).equals("attackLaser"));
```
