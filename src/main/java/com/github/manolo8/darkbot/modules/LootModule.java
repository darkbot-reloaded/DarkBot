package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.Comparator;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Math.random;

public class LootModule implements Module {

    private Main main;

    private List<Ship> ships;
    private List<Npc> npcs;

    private HeroManager hero;
    private Drive drive;

    private Config config;

    public Npc target;
    private Long ability;

    private int radiusFix;

    private long laserTime;
    private long clickDelay;
    private boolean sab;

    private boolean repairing;
    private boolean jump;
    private Portal escaping;

    @Override
    public void install(Main main) {
        this.main = main;

        this.hero = main.hero;
        this.drive = main.hero.drive;

        this.ships = main.mapManager.entities.ships;
        this.npcs = main.mapManager.entities.npcs;
        this.config = main.config;
    }

    @Override
    public String status() {
        return jump ? "Jumping port" : repairing ? "Repairing" : escaping != null ? "Avoiding enemy" :
                target != null ? "Killing npc" + (hero.isAttacking(target) ? " S" : "") + (ability != null ? " A" : "") + (sab ? " SAB" : "")
                        : "Roaming";
    }

    @Override
    public boolean canRefresh() {
        return target == null;
    }

    @Override
    public void tick() {
        if (checkDangerousAndCurrentMap()) {
            main.guiManager.pet.setEnabled(true);

            if (findTarget()) {
                moveToAnSafePosition();
                doKillTargetTick();
            } else if (!drive.isMoving()) {
                drive.moveRandom();
            }
        }
    }

    boolean checkDangerousAndCurrentMap() {
        if (this.config.GENERAL.WORKING_MAP != this.hero.map.id && !main.mapManager.entities.portals.isEmpty()) {
            this.hero.runMode();
            repairing = true;
            jump = false;
            this.main.setModule(new MapModule()).setTarget(this.main.starManager.byId(this.main.config.GENERAL.WORKING_MAP));
            return false;
        }

        if (jump && escaping != null) {
            this.hero.runMode();
            if (escaping.locationInfo.distance(this.hero) < 250.0) hero.jumpPortal(escaping);
            else moveToSafety(escaping);
            return false;
        }

        boolean underAttack = this.isUnderAttack();
        boolean lowHp = this.hero.health.hpPercent() < this.config.GENERAL.SAFETY.REPAIR_HP ||
                (this.hero.health.hpPercent() < this.config.GENERAL.SAFETY.REPAIR_HP_NO_NPC &&
                        (this.target == null || this.target.removed || this.target.health.hp == 0 || this.target.health.hpPercent() > 0.8));

        if (lowHp || hasEnemies()) {
            escaping = this.main.starManager.next(this.hero.map, this.hero.locationInfo, this.hero.map);
            if (escaping == null) return true; // No place to run, don't even try.

            this.hero.runMode();
            if (underAttack && !repairing) Main.API.keyboardClick(config.GENERAL.SAFETY.SHIP_ABILITY);

            jump |= config.LOOT.SAFETY.JUMP_PORTALS && underAttack;
            repairing |= underAttack || lowHp || !this.config.LOOT.SAFETY.STOP_RUNNING_NO_SIGHT;

            if (escaping.locationInfo.distance(this.hero) > 250.0) moveToSafety(escaping);
            else if (lowHp && hero.health.hpDecreasedIn(100)) jump |= config.LOOT.SAFETY.JUMP_PORTALS;
            return false;
        }
        repairing &= this.hero.health.hpPercent() < this.config.GENERAL.SAFETY.REPAIR_TO_HP;
        if (repairing) return false;
        escaping = null;
        return true;
    }

    private void moveToSafety(Entity safety) {
        if (drive.movingTo().distance(safety.locationInfo.now) < 200) return;
        double angle = safety.locationInfo.now.angle(hero.locationInfo.now) + Math.random() * 0.1 - 0.05;
        drive.move(Location.of(escaping.locationInfo.now, angle, 20 + Math.random() * 200));
    }

    void doKillTargetTick() {
        if (!main.mapManager.isTarget(target)) {
            lockAndSetTarget();
            return;
        }

        if (!(target.npcInfo.ignoreAttacked || main.mapManager.isCurrentTargetOwned()) ||
                (drive.closestDistance(target.locationInfo.now) > 400 && !target.locationInfo.isMoving()
                        && (target.health.shIncreasedIn(1000) || target.health.shieldPercent() > 0.95))) {
            target.setTimerTo(5000);
            hero.setTarget(target = null);
            return;
        }

        if (ability != null && (target.health.maxHp > 0 || ability < System.currentTimeMillis())) {
            if (target.health.maxHp < config.LOOT.SHIP_ABILITY_MIN) ability = null;
            else if (hero.locationInfo.distance(target) < 575) {
                API.keyboardClick(config.LOOT.SHIP_ABILITY);
                ability = null;
            }
        }

        tryAttackOrFix();
    }

    boolean findTarget() {
        if (target == null || target.removed || hero.target != target || (!hero.isAttacking(target) && hero.locationInfo.distance(target) > 600))
            target = closestNpc(hero.locationInfo.now);
        return target != null;
    }

    private void tryAttackOrFix() {
        boolean bugged = (!target.health.hpDecreasedIn(3000) && hero.locationInfo.distance(target) < 550);
        if ((shouldSab() != sab || !hero.isAttacking(target) || bugged) && System.currentTimeMillis() > laserTime) {
            laserTime = System.currentTimeMillis() + 750;
            API.keyboardClick(getAttackKey());
        }
    }

    private boolean shouldSab() {
        return config.LOOT.SAB.ENABLED && hero.health.shieldPercent() < config.LOOT.SAB.PERCENT
                && target.health.shield > config.LOOT.SAB.NPC_AMOUNT;
    }

    private char getAttackKey() {
        if (sab = shouldSab()) return this.config.LOOT.SAB.KEY;
        return this.target == null || this.target.npcInfo.attackKey == null ?
                this.config.LOOT.AMMO_KEY : this.target.npcInfo.attackKey;
    }

    private void lockAndSetTarget() {
        if (hero.locationInfo.distance(target) > 750 || System.currentTimeMillis() - clickDelay < 400) return;
        hero.setTarget(target);
        setRadiusAndClick();
        clickDelay = System.currentTimeMillis();
        laserTime = clickDelay + 200;
        ability = clickDelay + 5000;
    }

    private void setRadiusAndClick() {
        target.clickable.setRadius(800);
        drive.clickCenter(true, target.locationInfo.now);
        target.clickable.setRadius(0);
    }

    void moveToAnSafePosition() {
        Location direction = hero.drive.movingTo();
        Location heroLoc = hero.locationInfo.now;
        Location targetLoc = target.locationInfo.destinationInTime(400);

        double distance = heroLoc.distance(target.locationInfo.now);
        double angle = targetLoc.angle(heroLoc);
        double radius = target.npcInfo.radius;

        if (target != hero.target || ability != null) radius = Math.min(500, radius);
        if (!target.locationInfo.isMoving() && target.health.hpPercent() < 0.25) radius = Math.min(radius, 600);

        if (target.npcInfo.noCircle) {
            if (targetLoc.distance(direction) <= radius) return;
            distance = 100 + random() * (radius - 110);
            angle += (random() * 0.1) - 0.05;
        } else {
            if (distance > radius) {
                radiusFix -= (distance - radius) / 2;
                radiusFix = (int) max(radiusFix, -target.npcInfo.radius / 2);
            } else {
                radiusFix += (radius - distance) / 6;
                radiusFix = (int) min(radiusFix, target.npcInfo.radius / 2);
            }
            distance = (radius += radiusFix);
            // Moved distance + speed - distance to chosen radius same angle, divided by radius
            angle += Math.max((hero.shipInfo.speed * 0.625) + (min(200, target.locationInfo.speed) * 0.625)
                    - heroLoc.distance(Location.of(targetLoc, angle, radius)), 0) / radius;
        }
        direction = Location.of(targetLoc, angle, distance);

        while (!drive.canMove(direction) && distance < 10000)
            direction.toAngle(targetLoc, angle += 0.3, distance += 2);
        if (distance >= 10000) direction.toAngle(targetLoc, angle, 500);

        if (config.LOOT.RUN_CONFIG_IN_CIRCLE && target.health.hpPercent() < 0.25 &&
                heroLoc.distance(direction) > target.npcInfo.radius * 2) {
            hero.runMode();
        } else {
            hero.attackMode();
        }

        drive.move(direction);
    }

    private boolean isUnderAttack() {
        if (!config.LOOT.SAFETY.RUN_FROM_ENEMIES && !config.LOOT.SAFETY.RUN_FROM_ENEMIES_SIGHT) return false;
        for (Ship ship : ships) {
            if (ship.playerInfo.isEnemy() && ship.isAttacking(hero)) return true;
        }
        return false;
    }

    private boolean hasEnemies() {
        if (!config.LOOT.SAFETY.RUN_FROM_ENEMIES && !config.LOOT.SAFETY.RUN_FROM_ENEMIES_SIGHT) return false;

        for (Ship ship : ships) {
            if (!ship.playerInfo.isEnemy()) continue;

            if (config.LOOT.SAFETY.RUN_FROM_ENEMIES_SIGHT && ship.locationInfo.distance(hero) < config.LOOT.SAFETY.MAX_SIGHT_DISTANCE)
                return true;

            if (ship.isAttacking(hero)) {
                ship.setTimerTo(400_000);
                return true;
            } else if (ship.isInTimer()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttackedByOthers(Npc npc) {
        if (npc.npcInfo.ignoreAttacked) return false;
        if (npc.isInTimer()) return true;
        for (Ship ship : this.ships) {
            if (!ship.isAttacking(npc)) continue;
            npc.setTimerTo(5000);
            return true;
        }
        return false;
    }

    private Npc closestNpc(Location location) {
        return this.npcs.stream()
                .filter(n -> n.npcInfo.kill)
                .filter(n -> !this.isAttackedByOthers(n))
                .filter(n -> drive.closestDistance(location) < 200)
                .min(Comparator.<Npc>comparingInt(n -> n.npcInfo.priority)
                        .thenComparing(n -> n.health.hpPercent())
                        .thenComparing(n -> n.locationInfo.now.distance(location))).orElse(null);
    }
}
