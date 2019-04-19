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
import com.github.manolo8.darkbot.modules.utils.NpcAttacker;

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

    private int radiusFix;

    NpcAttacker attack;

    private boolean repairing;
    private boolean jump;
    private Portal escaping;

    @Override
    public void install(Main main) {
        this.main = main;
        this.attack = new NpcAttacker(main);

        this.hero = main.hero;
        this.drive = main.hero.drive;

        this.ships = main.mapManager.entities.ships;
        this.npcs = main.mapManager.entities.npcs;
        this.config = main.config;
    }

    @Override
    public String status() {
        return jump ? "Jumping port" : repairing ? "Repairing" : escaping != null ? "Avoiding enemy" :
                attack.hasTarget() ? attack.status() : "Roaming";
    }

    @Override
    public boolean canRefresh() {
        return attack.target == null;
    }

    @Override
    public void tick() {
        if (checkDangerousAndCurrentMap()) {
            main.guiManager.pet.setEnabled(true);

            if (findTarget()) {
                moveToAnSafePosition();
                attack.doKillTargetTick();
                ignoreInvalidTarget();
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
                        (!attack.hasTarget() || attack.target.health.hp == 0 || attack.target.health.hpPercent() > 0.8));

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

    boolean findTarget() {
        return (attack.target = closestNpc(hero.locationInfo.now)) != null;
    }

    private void ignoreInvalidTarget() {
        if (!(attack.target.npcInfo.ignoreAttacked || main.mapManager.isCurrentTargetOwned()) ||
                (drive.closestDistance(attack.target.locationInfo.now) > 400 && !attack.target.locationInfo.isMoving()
                        && (attack.target.health.shIncreasedIn(1000) || attack.target.health.shieldPercent() > 0.95))) {
            attack.target.setTimerTo(5000);
            hero.setTarget(attack.target = null);
        }
    }

    void moveToAnSafePosition() {
        Npc target = attack.target;
        Location direction = drive.movingTo();
        Location heroLoc = hero.locationInfo.now;
        Location targetLoc = target.locationInfo.destinationInTime(400);

        double distance = heroLoc.distance(target.locationInfo.now);
        double angle = targetLoc.angle(heroLoc);
        double radius = target.npcInfo.radius;

        if (target != hero.target || attack.castingAbility()) radius = Math.min(500, radius);
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
            if (ship.address == hero.address || ship.address == hero.pet.address) continue;
            if (!ship.isAttacking(npc)) continue;
            npc.setTimerTo(30000);
            return true;
        }
        return false;
    }

    private Npc closestNpc(Location location) {
        int extraPriority = attack.hasTarget() &&
                (hero.target == attack.target || hero.locationInfo.distance(attack.target) < 600)
                ? 20 - (int)(attack.target.health.hpPercent() * 10) : 0;
        return this.npcs.stream()
                .filter(n -> n == attack.target && hero.isAttacking(attack.target) || (n.npcInfo.kill &&
                        !this.isAttackedByOthers(n) && drive.closestDistance(location) < 200))
                .min(Comparator.<Npc>comparingInt(n -> n.npcInfo.priority - (n == attack.target ? extraPriority : 0))
                        .thenComparing(n -> n.locationInfo.now.distance(location))).orElse(null);
    }
}
