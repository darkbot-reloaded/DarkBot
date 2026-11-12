---
name: darkbot-module-dev
description: "Guide for developing DarkBot modules, behaviors, and tasks. Use when creating gameplay modules, behaviors, background tasks, understanding the tick loop, module lifecycle, feature registration, NpcAttacker, SafetyFinder, or the Drive movement system. Triggers on: 'create module', 'module dev', 'behavior dev', 'task dev', 'tick loop', 'NpcAttacker', 'SafetyFinder', 'Drive movement', 'Module interface', 'Behavior interface', 'Task interface', 'FeatureRegistry', 'module lifecycle'."
---

# DarkBot Module, Behavior & Task Development

## Overview

- **Modules** (`Module`): Primary gameplay behaviors (active one controls the ship)
- **Behaviors** (`Behavior`): Auxiliary behaviors running alongside the active module
- **Tasks** (`Task`): Background operations (HTTP requests, data processing) that may block
- All three are registered via `@Feature` annotation and `FeatureRegistry`

## Module Interface

```java
public interface Module extends Installable, Tickable, RefreshHandler {
    void install(Main main);          // Called once when module is activated
    boolean canRefresh();             // Can the bot refresh/reconnect?
    default void tickModule() {}      // Main logic, called every ~100ms
    default String status() {}        // Status text shown in GUI
    default String stoppedStatus() {} // Status when module is stopped
    default void uninstall() {}       // Cleanup when switching modules
}
```

## Behavior Interface

```java
public interface Behavior extends Tickable {
    default void onTickBehavior() {}  // Runs every tick, regardless of active module
}
```

## Task Interface

```java
public interface Task extends Installable, Tickable {
    default void tickTask() {}          // Called when SID is valid (game backpage accessible)
    default void backgroundTick() {}    // Called every tick regardless of login state
}
```

### Threading Model

Tasks run on the **background thread** (`BackpageManager`), NOT the main tick thread:

- `tickTask()` — Only called when the player is logged in and SID is valid. Use for game backpage requests (HTTP to servers).
- `backgroundTick()` — Called every tick (~100ms) regardless of login state. Use for external API calls or non-game-network operations.
- Multiple tasks share the same background thread cooperatively. **Blocking is allowed** (unlike Module/Behavior).
- Tasks must NOT reference main-thread data without synchronization. If you need both main + background logic, implement `Behavior` and `Task` on the same class.

### Module vs Behavior vs Task

| Feature       | Module          | Behavior           | Task                              |
| ------------- | --------------- | ------------------ | --------------------------------- |
| Thread        | Main            | Main               | Background                        |
| Concurrent    | One at a time   | Multiple           | Multiple                          |
| Blocking      | No              | No                 | Yes                               |
| Controls ship | Yes             | No                 | No                                |
| When active   | Selected module | Always             | Always                            |
| Main method   | `tickModule()`  | `onTickBehavior()` | `tickTask()` / `backgroundTick()` |

## Creating a Task

```java
package com.example.myplugin;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Task;
import com.github.manolo8.darkbot.extensions.features.Feature;

@Feature(name = "Data Fetcher", description = "Fetches external data in background")
public class DataFetcher implements Task {

    private Main main;

    @Override
    public void install(Main main) {
        this.main = main;
    }

    @Override
    public void tickTask() {
        // Called only when SID is valid — safe to make game backpage requests
        // Example: fetch player data, parse HTML, update configs
    }

    @Override
    public void backgroundTick() {
        // Called every tick regardless of login — use for non-game HTTP calls
        // Example: query external API, update local cache
    }
}
```

### Registration

Tasks are registered like Modules/Behaviors via `@Feature` annotation. The `TaskHandler` collects all registered `Task` features and passes them to `BackpageManager.setTasks()`. Native tasks (built-in): `UsernameUpdater`, `FlashResManager`.

## Module Lifecycle

```
User selects module in GUI (or config changes)
  → Main.checkModule() detects change
  → FeatureRegistry.getFeature(moduleClass) → FeatureDefinition
  → FeatureInstanceLoader.loadFeature(fd) creates instance
  → module.install(Main) called
  → module.tickModule() called every tick while active
  → On switch: old module.uninstall() called
```

## Creating a Module

```java
package com.example.myplugin;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.utils.NpcAttacker;
import com.github.manolo8.darkbot.modules.utils.SafetyFinder;

import java.util.List;

@Feature(name = "My NPC Killer", description = "Kills NPCs with custom strategy")
public class MyNpcKiller implements Module {

    private Main main;
    private HeroManager hero;
    private Drive drive;
    private Config config;
    private List<Npc> npcs;

    protected NpcAttacker attack;
    protected SafetyFinder safety;

    @Override
    public void install(Main main) {
        this.main = main;
        this.hero = main.hero;
        this.drive = main.hero.drive;
        this.config = main.config;
        this.npcs = main.mapManager.entities.npcs;

        this.attack = new NpcAttacker(main);
        this.safety = new SafetyFinder(main);
    }

    @Override
    public void uninstall() {
        safety.uninstall();
    }

    @Override
    public String status() {
        if (safety.state() != SafetyFinder.Escaping.NONE) return safety.status();
        if (attack.hasTarget()) return attack.status();
        return "Roaming";
    }

    @Override
    public boolean canRefresh() {
        return !attack.hasTarget() && safety.state() == SafetyFinder.Escaping.WAITING;
    }

    @Override
    public void tickModule() {
        // 1. Safety check (repairs, running from enemies)
        if (!safety.tick()) return;

        // 2. Map check
        if (config.GENERAL.WORKING_MAP != hero.map.id) {
            main.setModule(new MapModule())
                    .setTarget(main.starManager.byId(config.GENERAL.WORKING_MAP));
            return;
        }

        // 3. Pet control
        main.guiManager.pet.setEnabled(true);

        // 4. Find and attack target
        if (findTarget()) {
            attack.doKillTargetTick();
        } else {
            // No target: roam randomly
            hero.roamMode();
            if (!drive.isMoving()) drive.moveRandom();
        }
    }

    protected boolean findTarget() {
        return (attack.target = closestNpc()) != null;
    }

    protected Npc closestNpc() {
        return npcs.stream()
                .filter(n -> n.npcInfo.kill && !n.isInTimer())
                .min(java.util.Comparator
                        .comparingInt((Npc n) -> n.npcInfo.priority)
                        .thenComparing(n -> n.health.hpPercent())
                        .thenComparing(n -> n.locationInfo.now.distance(hero.locationInfo.now)))
                .orElse(null);
    }
}
```

## Creating a Behavior

```java
@Feature(name = "Auto Chat", description = "Sends periodic chat messages")
public class AutoChat implements Behavior {

    private final Main main;
    private final Timer timer = Timer.get(60_000); // 60 second timer

    public AutoChat(Main main) {
        this.main = main;
    }

    @Override
    public void onTickBehavior() {
        if (timer.tryActivate()) {
            // Send chat message via backpage API
            main.backpage.sendPublicChatMessage("Hello!");
        }
    }
}
```

## Key Utilities

### NpcAttacker

Handles NPC combat logic: circling, distance management, ammo switching, SAB/RSB, ship ability usage.

```java
protected NpcAttacker attack;

// In install():
this.attack = new NpcAttacker(main);

// In tickModule():
if (attack.target != null) {
    attack.doKillTargetTick();
}

// Target selection:
attack.target = someNpc;
```

### SafetyFinder

Handles repairs, running from enemies, revive logic, and safety checks.

```java
protected SafetyFinder safety;

// In install():
this.safety = new SafetyFinder(main);

// In tickModule():
if (!safety.tick()) return; // Returns false if handling emergency

// State checking:
if (safety.state() == SafetyFinder.Escaping.NONE) { ... }
```

### Drive (Movement)

```java
Drive drive = main.hero.drive;

// Move to specific location:
drive.move(targetLocation);

// Random movement:
drive.moveRandom();

// Check if currently moving:
if (drive.isMoving()) { ... }

// Get closest distance to a path point:
double dist = drive.getClosestDistance(targetLocation);

// Check if a location is reachable:
boolean canReach = drive.canMove(targetLocation);
```

## Tick Loop Order

### Main Thread (Main.java)

```
1. status.tick()              — Check running/paused
2. checkModule()              — Load new module if config changed
3. validTick()                — Tick all managers (hero, map, stats, effects, etc.)
4. tickRunning()              — If running:
   a. pet.tick()              — Pet management
   b. group.tick()            — Group management
   c. checkRefresh()          — Periodic refresh check
   d. module.tickModule()     — Active module logic
   e. behaviors.forEach()     — All enabled behaviors
5. form.tick()                — Update GUI
6. configManager.saveChangedConfig()
7. processTasks()             — Queued tasks (NOT Task features — internal queued runnables)
8. Sleep (100ms active, 250ms idle)
```

### Background Thread (BackpageManager.java)

```
Every ~100ms:
1. backgroundTick() on all Tasks  — Always (no SID required)
2. tickTask() on all Tasks        — Only if SID is valid
```

## Registering Features

Native features are registered in `FeatureRegisterHandler`. Plugin features are auto-discovered from `@Feature` annotations in loaded plugin JARs.

```java
// To query registered features:
Collection<FeatureDefinition<?>> features = featureRegistry.getFeatures();

// Get specific feature:
Optional<MyModule> module = featureRegistry.getFeature(MyModule.class);

// Check if enabled:
FeatureDefinition<?> fd = featureRegistry.getFeatureDefinition(MyModule.class);
boolean enabled = fd.isEnabled();
```
