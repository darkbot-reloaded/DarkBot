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

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SafetyFinder {

    private final MapManager mapManager;
    private final Config.General.Safety SAFETY;
    private final Config.General.Running RUNNING;

    private List<Ship> ships;
    private HeroManager hero;
    private Drive drive;
    private MapTraveler mapTraveler;
    private PortalJumper jumper;
    private Consumer<Map> listener = this::onMapChange;

    private SafetyInfo safety;
    private Escaping escape = Escaping.NONE;
    private boolean refreshing;
    private long escapingSince = -1;
    private long lastTick;

    public enum Escaping {
        ENEMY, SIGHT, REPAIR, REFRESH, WAITING, NONE;
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
        this.mapManager = main.mapManager;
        this.SAFETY = main.config.GENERAL.SAFETY;
        this.RUNNING = main.config.GENERAL.RUNNING;

        this.ships = main.mapManager.entities.ships;
        this.hero = main.hero;
        this.drive = main.hero.drive;
        this.mapTraveler = new MapTraveler(main);
        this.jumper = new PortalJumper(hero);
        mapManager.mapChange.add(listener);
    }

    public void uninstall() {
        mapTraveler.uninstall();
        mapManager.mapChange.remove(listener);
    }

    private void onMapChange(Map map) {
        if (safety != null && safety.type == SafetyInfo.Type.PORTAL) {
            if (jumpState == JumpState.JUMPING) jumpState = JumpState.JUMPED;
            else if (jumpState == JumpState.RETURNING && map == prevMap) jumpState = JumpState.RETURNED;
        }
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public Escaping state() {
        return jumpState == JumpState.JUMPED && safety.jumpMode == SafetyInfo.JumpMode.ALWAYS_OTHER_SIDE ?
                Escaping.WAITING : escape;
    }

    public String status() {
        return "Escaping " + simplify(escape) + (safety == null ? "" : " " + safety +
                (safety.type == SafetyInfo.Type.PORTAL ? " " + simplify(jumpState) + (jumpState == JumpState.RETURNING ? " " + prevMap : "") : ""));
    }

    private String simplify(Object obj) {
        return obj.toString().toLowerCase().replace("_", " ");
    }

    /**
     * @return True if it's safe to keep working, false if the safety is working.
     */
    public boolean tick() {
        if (escape == Escaping.WAITING) {
            if (escapingSince == -1) escapingSince = System.currentTimeMillis();

            long escapeTime = System.currentTimeMillis() - escapingSince;
            if (escapeTime > 121_000) escapingSince = -1;
            if (escapeTime > 120_000) return false; // Over 2 min waiting? Try ticking module a bit to move.
        }
        // If no tick occurred for a while, means safety finder should have reset (Probably died)
        if (System.currentTimeMillis() - lastTick > 2500) {
            escape = Escaping.NONE;
            jumpState = JumpState.CURRENT_MAP;
        }
        lastTick = System.currentTimeMillis();

        if (jumpState == JumpState.CURRENT_MAP || jumpState == JumpState.JUMPING) {
            activeTick();

            if (escape == Escaping.NONE || safety == null) return true;

            if (hero.locationInfo.distance(safety.x, safety.y) > safety.radius()) {
                moveToSafety();
                hero.runMode();
                return false;
            }
        }

        switch (jumpState) {
            case CURRENT_MAP:
                if (escape.shouldJump(safety)
                        // Also jump if taking damage & you would jump away from enemy.
                        || (hero.health.hpDecreasedIn(200) && Escaping.ENEMY.shouldJump(safety))) {
                    jumpState = JumpState.JUMPING;
                    drive.stop(false);
                }
                break;
            case JUMPING:
                prevMap = hero.map;
                jumper.jump((Portal) safety.entity);
                return false;
            case JUMPED:
                if (hero.health.hpDecreasedIn(100) || safety.jumpMode != SafetyInfo.JumpMode.ALWAYS_OTHER_SIDE
                        || (!refreshing && doneRepairing())) {
                    jumpState = JumpState.RETURNING;
                    mapTraveler.setTarget(prevMap);
                }
                break;
            case RETURNING:
                if (mapTraveler.isDone()) mapTraveler.setTarget(prevMap);
                mapTraveler.tick();
                return false;
        }

        if (jumpState == JumpState.CURRENT_MAP || jumpState == JumpState.RETURNED) {
            escape = Escaping.WAITING;

            if (!refreshing && doneRepairing() && !hasEnemy()) {
                escape = Escaping.NONE;
                jumpState = JumpState.CURRENT_MAP;
                return true;
            }
        }
        return false;
    }

    private void activeTick() {
        Escaping oldEscape = escape;
        escape = getEscape();
        if (escape == Escaping.NONE || escape == Escaping.WAITING) return;

        if (jumpState == JumpState.CURRENT_MAP &&
                (escape != oldEscape || safety == null || safety.entity == null || safety.entity.removed)) {
            safety = getSafety();
        }
        if (safety == null) {
            escape = Escaping.NONE;
            return;
        }

        if (oldEscape != escape && escape == Escaping.ENEMY) castDefensiveAbility();
    }

    private Escaping getEscape() {
        if (escape == Escaping.ENEMY || isUnderAttack()) return Escaping.ENEMY;
        if (escape == Escaping.WAITING) return Escaping.WAITING;
        if ((escape == Escaping.SIGHT && !RUNNING.STOP_RUNNING_NO_SIGHT) || hasEnemy()) return Escaping.SIGHT;
        if (escape == Escaping.REPAIR || hero.health.hpPercent() < SAFETY.REPAIR_HP_RANGE.min ||
                (hero.health.hpPercent() < this.SAFETY.REPAIR_HP_NO_NPC &&
                        (!hero.hasTarget() || hero.target.health.hpPercent() > 0.9))) return Escaping.REPAIR;
        return refreshing ? Escaping.REFRESH : Escaping.NONE;
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

        if (escape == Escaping.REPAIR || escape == Escaping.REFRESH ||
                RUNNING.RUN_FURTHEST_PORT == 0 || best.distance < RUNNING.RUN_FURTHEST_PORT) return best;

        List<Ship> enemies = ships.stream().filter(this::runFrom).collect(Collectors.toList());

        return safeties.stream()
                .filter(s -> s.distance < enemies.stream()
                        .mapToDouble(enemy -> drive.distanceBetween(enemy.locationInfo.now, s.x, s.y))
                        .min().orElse(Double.POSITIVE_INFINITY))
                .findFirst()
                .orElse(best);
    }

    private void moveToSafety() {
        if ((jumpState != JumpState.CURRENT_MAP && jumpState != JumpState.JUMPING)
                || drive.movingTo().distance(safety.x, safety.y) < safety.radius()
                || safety.entity.removed) return;
        Location safeLoc = new Location(safety.x, safety.y);

        double angle = safeLoc.angle(hero.locationInfo.now) + Math.random() * 0.2 - 0.1;
        drive.move(Location.of(safeLoc, angle, -safety.radius() * (0.3 + (0.60 * Math.random())))); // 30%-90% radius
    }

    private void castDefensiveAbility() {
        Location to = drive.movingTo();
        if (drive.distanceBetween(hero.locationInfo.now, (int) to.x, (int) to.y) >= RUNNING.SHIP_ABILITY_MIN) {
            Main.API.keyboardClick(RUNNING.SHIP_ABILITY);
        }
    }

    private boolean doneRepairing() {
        if (!hero.isInMode(SAFETY.REPAIR)
                && (hero.health.hpIncreasedIn(1000) || hero.health.hpPercent() == 1)
                && (hero.health.shDecreasedIn(1000) || hero.health.shieldPercent() == 0)) hero.setMode(SAFETY.REPAIR);
        return this.hero.health.shieldPercent() >= SAFETY.REPAIR_TO_SHIELD &&
                hero.setMode(SAFETY.REPAIR) && this.hero.health.hpPercent() >= SAFETY.REPAIR_HP_RANGE.max;
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
        if (ship.isAttacking(hero)) ship.setTimerTo(RUNNING.REMEMBER_ENEMIES_FOR * 1000);
        return ship.isInTimer();
    }

}
