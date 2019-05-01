package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.core.entities.BattleStation;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.modules.MapModule;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SafetyFinder {

    private final Main main;
    private final MapManager mapManager;
    private final Config.General.Safety SAFETY;
    private final Config.General.Running RUNNING;

    private List<Ship> ships;
    private HeroManager hero;
    private Drive drive;

    private SafetyInfo safety;

    private Escaping escape = Escaping.NONE;
    public enum Escaping {
        ENEMY, SIGHT, REPAIR, NONE;
        boolean canUse(SafetyInfo safety) {
            if (safety.type == SafetyInfo.Type.CBS) {
                BattleStation cbs = ((BattleStation) safety.entity);
                // Ignore enemy CBS, and if set to ALLY only, ignore empty meteorites (hull = 0)
                if (cbs.info.isEnemy() || (cbs.hullId == 0 && safety.cbsMode == SafetyInfo.CbsMode.ALLY)) return false;
            }
            return safety.runMode.ordinal() <= this.ordinal();
        }
        boolean shouldJump(SafetyInfo safety) {
            if (safety.type != SafetyInfo.Type.PORTAL) return false;
            return safety.jumpMode.ordinal() > this.ordinal();
        }
    }

    private JumpState jumpState = JumpState.CURRENT_MAP;
    private enum JumpState {CURRENT_MAP, JUMPING, JUMPED, RETURNING, RETURNED}
    private Map prevMap;

    public SafetyFinder(Main main) {
        this.main = main;
        this.mapManager = main.mapManager;
        this.SAFETY = main.config.GENERAL.SAFETY;
        this.RUNNING = main.config.GENERAL.RUNNING;

        this.ships = main.mapManager.entities.ships;
        this.hero = main.hero;
        this.drive = main.hero.drive;
        mapManager.mapChange.add(m -> {
            if (safety != null && safety.type == SafetyInfo.Type.PORTAL) {
                if (jumpState == JumpState.JUMPING) jumpState = JumpState.JUMPED;
                else if (jumpState == JumpState.RETURNING && m == prevMap) jumpState = JumpState.RETURNED;
            }
        });
    }

    public Escaping state() {
        return escape;
    }

    public String status() {
        return "Escaping " + escape + (safety == null ? "" : " " + safety +
                (safety.type == SafetyInfo.Type.PORTAL ? " - " + jumpState : ""));
    }

    /**
     * @return True if it's safe to keep working, false if the safety is working.
     */
    public boolean tick() {
        if (jumpState == JumpState.CURRENT_MAP || jumpState == JumpState.JUMPING) {
            Escaping oldEscape = escape;
            escape = getEscape();
            if (escape == Escaping.NONE) return true;

            if (escape != oldEscape || safety == null || safety.entity == null || safety.entity.removed) {
                safety = getSafety();
            }
            if (safety == null) {
                escape = Escaping.NONE;
                return true;
            }

            runToSafety();
            if (oldEscape != escape && escape == Escaping.ENEMY) {
                Location to = drive.movingTo();
                if (drive.distanceBetween(hero.locationInfo.now, (int) to.x, (int) to.y) >= RUNNING.SHIP_ABILITY_MIN) {
                    Main.API.keyboardClick(RUNNING.SHIP_ABILITY);
                }
            }

            if (hero.locationInfo.distance(safety.x, safety.y) > safety.radius()) {
                hero.runMode();
                return false;
            }
        }

        switch (jumpState) {
            case CURRENT_MAP:
            case JUMPING:
                if (escape.shouldJump(safety)
                        // Also jump if taking damage & you would jump away from enemy.
                        || (hero.health.hpDecreasedIn(200) && Escaping.ENEMY.shouldJump(safety))) {
                    prevMap = hero.map;
                    drive.stop(false);
                    hero.jumpPortal((Portal) safety.entity);
                    jumpState = JumpState.JUMPING;
                    return false;
                }
                break;
            case JUMPED:
            case RETURNING:
                if (safety.jumpMode != SafetyInfo.JumpMode.ALWAYS_OTHER_SIDE || doneRepairing() || hero.health.hpDecreasedIn(100)) {
                    main.setModule(new MapModule()).setTarget(prevMap);
                    jumpState = JumpState.RETURNING;
                    return false;
                }
                break;
        }

        if ((jumpState == JumpState.RETURNED || (!escape.shouldJump(safety) && jumpState == JumpState.CURRENT_MAP))
                && doneRepairing() && !hasEnemy()) {
            escape = Escaping.NONE;
            jumpState = JumpState.CURRENT_MAP;
            return true;
        }
        return false;
    }

    private Escaping getEscape() {
        if (escape == Escaping.ENEMY || isUnderAttack()) return Escaping.ENEMY;
        if ((escape == Escaping.SIGHT && !RUNNING.STOP_RUNNING_NO_SIGHT) || hasEnemy()) return Escaping.SIGHT;
        if (escape == Escaping.REPAIR || hero.health.hpPercent() < SAFETY.REPAIR_HP ||
                (hero.health.hpPercent() < this.SAFETY.REPAIR_HP_NO_NPC &&
                        (!hero.hasTarget() || hero.target.health.hpPercent() > 0.9))) return Escaping.REPAIR;
        return Escaping.NONE;
    }

    private SafetyInfo getSafety() {
        List<SafetyInfo> safeties = mapManager.safeties.stream()
                .filter(s -> s.entity != null && !s.entity.removed)
                .filter(escape::canUse)
                .peek(s -> s.distance = Math.max(0, drive.distanceBetween(hero.locationInfo.now, s.x, s.y) - s.radius()))
                .sorted(Comparator.comparingDouble(s -> s.distance))
                .collect(Collectors.toList());
        if (safeties.isEmpty()) return null;

        SafetyInfo best = safeties.get(0);

        if (escape == Escaping.REPAIR ||
                RUNNING.RUN_FURTHEST_PORT == 0 || best.distance < RUNNING.RUN_FURTHEST_PORT) return best;

        List<Ship> enemies = ships.stream().filter(this::runFrom).collect(Collectors.toList());

        return safeties.stream()
                .filter(s -> s.distance < enemies.stream()
                        .mapToDouble(enemy -> drive.distanceBetween(enemy.locationInfo.now, s.x, s.y))
                        .min().orElse(Double.POSITIVE_INFINITY))
                .findFirst()
                .orElse(best);
    }

    private void runToSafety() {
        if ((jumpState != JumpState.CURRENT_MAP && jumpState != JumpState.JUMPING)
                || drive.movingTo().distance(safety.x, safety.y) < safety.radius()
                || safety.entity.removed) return;
        Location safeLoc = new Location(safety.x, safety.y);

        double angle = safeLoc.angle(hero.locationInfo.now) + Math.random() * 0.2 - 0.1;
        drive.move(Location.of(safeLoc, angle, -safety.radius() * (0.3 + (0.65 * Math.random())))); // 30%-95% radius
    }

    private boolean doneRepairing() {
        return this.hero.health.hpPercent() >= SAFETY.REPAIR_TO_HP;
    }

    private boolean isUnderAttack() {
        if (!RUNNING.RUN_FROM_ENEMIES && !RUNNING.RUN_FROM_ENEMIES_SIGHT) return false;
        return ships.stream().anyMatch(s -> s.playerInfo.isEnemy() && isAttackingOrTimer(s));
    }

    private boolean hasEnemy() {
        if (!RUNNING.RUN_FROM_ENEMIES && !RUNNING.RUN_FROM_ENEMIES_SIGHT) return false;
        return ships.stream().anyMatch(this::runFrom);
    }

    private boolean runFrom(Ship ship) {
        return ship.playerInfo.isEnemy() && (isAttackingOrTimer(ship) ||
                (RUNNING.RUN_FROM_ENEMIES_SIGHT && ship.locationInfo.distance(hero) < RUNNING.MAX_SIGHT_DISTANCE));
    }

    private boolean isAttackingOrTimer(Ship ship) {
        if (ship.isAttacking(hero)) ship.setTimerTo(400_000);
        return ship.isInTimer();
    }

}
